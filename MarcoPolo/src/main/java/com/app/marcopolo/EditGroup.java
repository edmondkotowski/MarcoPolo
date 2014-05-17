package com.app.marcopolo;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.app.marcopolo.groups.FriendDeviceFactory;
import com.app.marcopolo.groups.FriendGroup;
import com.app.marcopolo.util.ConnectionManager;
import com.app.marcopolo.util.PeerListGroupLoader;
import com.app.marcopolo.util.SystemUiHider;
import rx.android.observables.ViewObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class EditGroup extends Activity {
    private PeerListGroupLoader _peerListListener;
    private FriendGroup _friendGroup;
    private final PublishSubject<String> _logSubject = PublishSubject.create();
    private ConnectionManager _connectionManager;
    private ListView _groupMemberList;
    private final IntentFilter _intentFilter = new IntentFilter();
    private ArrayAdapter<String> _listViewAdapter;
    private Button _stopDiscoveryButton;
    private Button _startDiscoveryButton;

    // register the broadcast receiver with the intent values to be matched
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(_connectionManager, _intentFilter);
    }

    // unregister the broadcast receiver
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(_connectionManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_group);
        _listViewAdapter =new ArrayAdapter<>(this, R.layout.group_member, new ArrayList<String>());

        WifiP2pManager manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(this, getMainLooper(), null);
        final Action1<String> updateCallback = new Action1<String>() {
            @Override
            public void call(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _listViewAdapter.add(result);
                    }
                });
            }
        };
        final Action1<List<String>> sourceChangedCallback = new Action1<List<String>>() {
            @Override
            public void call(final List<String> result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _listViewAdapter.clear();
                        _listViewAdapter.addAll(result);
                    }
                });
            }
        };
        _peerListListener  = new PeerListGroupLoader(new FriendDeviceFactory(), updateCallback, sourceChangedCallback);
        _connectionManager = new ConnectionManager(manager, channel, _peerListListener, _logSubject);

        _friendGroup = (FriendGroup) getIntent().getExtras().getSerializable(getClass().getName());
        _peerListListener.setFriendGroup(_friendGroup);

        _groupMemberList = (ListView) findViewById(R.id.group_members);
        _groupMemberList.setAdapter(_listViewAdapter);

        _stopDiscoveryButton = (Button) findViewById(R.id.stop_discovery);
        _startDiscoveryButton = (Button) findViewById(R.id.start_discovery);


        ViewObservable.clicks(_startDiscoveryButton, false)
                .observeOn(Schedulers.io())
                .doOnNext(new Action1<Button>() {
                    @Override
                    public void call(final Button button) {
                        _connectionManager.discoverPeers();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Button>() {
                    @Override
                    public void call(final Button button) {
                        _startDiscoveryButton.setVisibility(View.GONE);
                        _stopDiscoveryButton.setVisibility(View.VISIBLE);
                    }
                }).subscribe();

        ViewObservable.clicks(_stopDiscoveryButton, false)
                .observeOn(Schedulers.io())
                .doOnNext(new Action1<Button>() {
                    @Override
                    public void call(final Button button) {
                        _connectionManager.stopDiscovery();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Button>() {
                    @Override
                    public void call(final Button button) {
                        _stopDiscoveryButton.setVisibility(View.GONE);
                        _startDiscoveryButton.setVisibility(View.VISIBLE);
                    }
                }).subscribe();

        _groupMemberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                String friendDisplayName = (String)_groupMemberList.getItemAtPosition((int) id);
                _friendGroup.connectTo(friendDisplayName, _connectionManager);
            }
        });

        registerForContextMenu(_groupMemberList);

        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(_connectionManager, _intentFilter);
    }

    private void connectToFriend(final long id) {
        String friendDisplayName = getFriendNameFromViewId((int) id);
        _friendGroup.connectTo(friendDisplayName, _connectionManager);
    }

    private String getFriendNameFromViewId(final int id) {
        return (String)_groupMemberList.getItemAtPosition((int) id);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_member_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo contextMenuInfo=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        String friendName = getFriendNameFromViewId((int)contextMenuInfo.id);
        switch(item.getItemId())
        {
            case R.id.connect_item:
                connectToFriend(contextMenuInfo.id);
                break;
            case R.id.rename_item:
                break;
            case R.id.remove_item:
                _friendGroup.remove(friendName);
                _listViewAdapter.remove(friendName);
                _listViewAdapter.sort(String.CASE_INSENSITIVE_ORDER);
                break;
        }
        return super.onContextItemSelected(item);
    }
}