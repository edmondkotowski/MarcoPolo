package com.app.marcopolo;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
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
import rx.functions.Action1;
import rx.subjects.PublishSubject;


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


        ViewObservable.clicks(findViewById(R.id.add_friends), false)
                .subscribe(new Action1<View>() {
                    @Override
                    public void call(View view) {
                        _connectionManager.discoverPeers();
                    }
                });

        _groupMemberList = (ListView) findViewById(R.id.group_members);
        _groupMemberList.setAdapter(new ArrayAdapter<>(this, R.layout.group_member, _friendGroup.getFriendNames()));
        _groupMemberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                String friendDisplayName = (String)parent.getSelectedItem();
                _friendGroup.connectTo(friendDisplayName, _connectionManager);
            }
        });

        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }
}
