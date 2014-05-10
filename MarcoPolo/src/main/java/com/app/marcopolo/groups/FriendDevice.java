package com.app.marcopolo.groups;

/**
 * Created by cdockter on 5/10/2014.
 */
public class FriendDevice {
    private final String _displayName;
    private final String _deviceAddress;

    public FriendDevice(String displayName, String deviceAddress) {
        if(displayName == null) {
            throw new IllegalArgumentException("displayName can not be null");
        }
        if(deviceAddress == null) {
            throw new IllegalArgumentException("deviceAddress can not be null");
        }
        _displayName = displayName;
        _deviceAddress = deviceAddress;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public FriendDevice(String displayName, FriendDevice device) {
        this(displayName, device._deviceAddress);
    }
}
