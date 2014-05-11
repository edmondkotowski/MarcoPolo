package com.app.marcopolo.groups;

/**
 * Created by cdockter on 5/10/2014.
 */
public interface IFriendDeviceFactory {
    FriendDevice create(String displayName, String deviceAddress);
}
