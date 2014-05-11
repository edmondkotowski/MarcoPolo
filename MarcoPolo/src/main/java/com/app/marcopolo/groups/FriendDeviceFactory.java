package com.app.marcopolo.groups;

import com.app.marcopolo.util.ConnectionManager;

import java.sql.Connection;

/**
 * Created by cdockter on 5/10/2014.
 */
public class FriendDeviceFactory implements IFriendDeviceFactory {
    @Override
    public FriendDevice create(String displayName, String deviceAddress) {
        return new FriendDevice(displayName, deviceAddress);
    }
}
