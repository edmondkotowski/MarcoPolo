package com.app.marcopolo.util;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import com.app.marcopolo.groups.FriendDevice;
import com.app.marcopolo.groups.FriendGroup;
import com.app.marcopolo.groups.IFriendDeviceFactory;
import rx.functions.Action1;

import java.util.List;

/**
 * Created by cdockter on 5/10/2014.
 */
public class PeerListGroupLoader implements WifiP2pManager.PeerListListener {

    private FriendGroup _friendGroup;
    private final IFriendDeviceFactory _deviceFactory;
    private final Action1<String> _updateCallback;
    private final Action1<List<String>> _sourceChangedCallback;

    public PeerListGroupLoader(
            final IFriendDeviceFactory deviceFactory,
            final Action1 updateCallback,
            final Action1<List<String>> sourceChangedCallback) {
        if(deviceFactory == null) {
            throw new IllegalArgumentException("deviceFactory can not be null");
        }
        if(updateCallback == null) {
            throw new IllegalArgumentException("updateCallback can not be null");
        }
        if(sourceChangedCallback == null) {
            throw new IllegalArgumentException("sourceChangedCallback can not be null");
        }

        _deviceFactory = deviceFactory;
        _updateCallback = updateCallback;
        _sourceChangedCallback = sourceChangedCallback;
    }

    @Override
    public void onPeersAvailable(final WifiP2pDeviceList peers) {
        for (WifiP2pDevice p2pDevice : peers.getDeviceList()) {
            FriendDevice friendDevice = _deviceFactory.create(p2pDevice.deviceName, p2pDevice.deviceAddress);
            if(_friendGroup.add(friendDevice)) {
                _updateCallback.call(friendDevice.getDisplayName());
            }
        }
    }

    public void setFriendGroup(final FriendGroup friendGroup) {
        if(friendGroup == null) {
            throw new IllegalArgumentException("friendGroup can not be null");
        }
        _friendGroup = friendGroup;
        _sourceChangedCallback.call(_friendGroup.getFriendNames());

    }

}
