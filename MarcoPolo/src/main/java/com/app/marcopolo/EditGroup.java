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
import java.util.concurrent.TimeUnit;


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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_group);

        WifiP2pManager manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(this, getMainLooper(), null);
        _peerListListener  = new PeerListGroupLoader(new FriendDeviceFactory());
        _connectionManager = new ConnectionManager(manager, channel, _peerListListener, _logSubject);

        _friendGroup = (FriendGroup) getIntent().getExtras().getSerializable(getClass().getName());
        _peerListListener.setFriendGroup(_friendGroup);

        final ArrayList<String> friendGroupNames = new ArrayList<>(_friendGroup.getFriendNames());
        final ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(this, R.layout.group_member, friendGroupNames);
        _groupMemberList = (ListView) findViewById(R.id.group_members);
        _groupMemberList.setAdapter(listViewAdapter);

        ViewObservable.clicks(findViewById(R.id.add_friends), false)
                .observeOn(Schedulers.io())
                .doOnNext(new Action1<View>() {
                    @Override
                    public void call(View view) {
                        _connectionManager.discoverPeers();
                    }
                })
                .delay(3, TimeUnit.SECONDS) // delay for 3 seconds after discovering on background thread since no way to react to change in friend group
                // TODO subscribe to change event instead of delay
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<View>() {
                    @Override
                    public void call(View view) {
                        friendGroupNames.clear();
                        friendGroupNames.addAll(_friendGroup.getFriendNames());
                        listViewAdapter.notifyDataSetChanged();
                    }
                });


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
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

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
        switch(item.getItemId())
        {
            case R.id.connect_item:
                connectToFriend(contextMenuInfo.id);
                break;
            case R.id.rename_item:
                break;
            case R.id.remove_item:
                String friendName = getFriendNameFromViewId((int)contextMenuInfo.id);
                _friendGroup.remove(friendName);
                break;
        }

        return super.onContextItemSelected(item);
    }
}
