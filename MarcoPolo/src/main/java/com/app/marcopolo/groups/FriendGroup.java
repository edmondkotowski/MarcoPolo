package com.app.marcopolo.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cdockter on 5/10/2014.
 */
public class FriendGroup {
    private final String _displayName;
    private final Map<String, FriendDevice> _members = new HashMap<>();


    public FriendGroup(String displayName) {
        if(displayName == null) {
            throw new IllegalArgumentException("displayName can not be null");
        }
        _displayName = displayName;
    }

    public FriendGroup add(FriendDevice device) {
        if(device == null) {
            throw new IllegalArgumentException("device can not be null");
        }
        _members.put(device.getDisplayName(), device);
        return this;
    }

    public FriendGroup remove(String deviceDisplayName) {
        if(deviceDisplayName == null) {
            throw new IllegalArgumentException("deviceDisplayName can not be null");
        }

        _members.remove(deviceDisplayName);
        return this;
    }

    public FriendGroup renameDevice(String oldDeviceDisplayName, String newDeviceDisplayName) {
        if(oldDeviceDisplayName == null) {
            throw new IllegalArgumentException("oldDeviceDisplayName can not be null");
        }
        if(newDeviceDisplayName == null) {
            throw new IllegalArgumentException("newDeviceDisplayName can not be null");
        }
        if(!_members.containsKey(oldDeviceDisplayName)) {
            throw new IllegalArgumentException("group does not include a member " + oldDeviceDisplayName);
        }

        FriendDevice newDevice = new FriendDevice(newDeviceDisplayName,_members.remove(oldDeviceDisplayName));
        _members.put(newDeviceDisplayName, newDevice);

        return this;
    }
}
