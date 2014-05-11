package com.app.marcopolo.util;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import com.app.marcopolo.groups.FriendDevice;
import com.app.marcopolo.groups.FriendGroup;

/**
 * Created by cdockter on 5/10/2014.
 */
public class PeerListGroupLoader implements WifiP2pManager.PeerListListener {

    private FriendGroup _friendGroup;
    @Override
    public void onPeersAvailable(final WifiP2pDeviceList peers) {
        for (WifiP2pDevice p2pDevice : peers.getDeviceList()) {
            FriendDevice friendDevice = new FriendDevice(p2pDevice.deviceName, p2pDevice.deviceAddress);
            _friendGroup.add(friendDevice);
        }
    }

    public void setFriendGroup(final FriendGroup friendGroup) {
        if(friendGroup == null) {
            throw new IllegalArgumentException("friendGroup can not be null");
        }
        _friendGroup = friendGroup;
    }
}
