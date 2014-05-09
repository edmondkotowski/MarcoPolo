package com.app.marcopolo.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.*;
import android.widget.TextView;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.Collection;
import java.util.Map;

public class ConnectionManager extends BroadcastReceiver {
    private final WifiP2pManager _wifiP2pManager;
    private final WifiP2pManager.Channel _channel;
    private final TextView _textValue;
    private final Map<String, WifiP2pDevice> _devicesLookup;
    private Activity _ownerActivity;

    public ConnectionManager(WifiP2pManager wifiP2pManager,
                             final Activity ownerActivity,
                             WifiP2pManager.Channel channel, TextView textValue,
                             Map<String, WifiP2pDevice> devicesLookup) {
        if(wifiP2pManager == null) {
            throw new IllegalArgumentException("wifiP2pManager");
        }

        _wifiP2pManager = wifiP2pManager;
        _ownerActivity = ownerActivity;
        _channel = channel;
        _textValue = textValue;
        _devicesLookup = devicesLookup;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                _textValue.append("\nWifi enabled");
                _textValue.append("\nonReceive begin: " + _textValue.hashCode());
            } else {
                _textValue.append("\nWifi not enabled");
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            _textValue.append("\nPEERS CHANGED ACTION");
            WifiP2pManager.PeerListListener myPeerListListener = new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(final WifiP2pDeviceList wifiP2pDeviceList) {
                    Collection<WifiP2pDevice> collection = wifiP2pDeviceList.getDeviceList();

                    for (WifiP2pDevice wifiP2pDevice : collection) {
                        if(!_devicesLookup.containsKey(wifiP2pDevice.deviceName)) {
                            connectToPeer(wifiP2pDevice);

                            _devicesLookup.put(wifiP2pDevice.deviceName, wifiP2pDevice);
                        }
                    }

                    if(!collection.isEmpty()) {
                        _wifiP2pManager.stopPeerDiscovery(_channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(final int i) {

                            }
                        });
                    }
                }
            };

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            _wifiP2pManager.requestPeers(_channel, myPeerListListener);

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //_wifiP2pManager.stopPeerDiscovery(_channel, null);
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    public void discoverPeers() {
        _textValue.append("\nDiscover peers");
        _wifiP2pManager.discoverPeers(_channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                _textValue.append("\nDiscover peers success");
            }

            @Override
            public void onFailure(int reasonCode) {
            }
        });
    }

    public void connectToPeer(final WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        _textValue.append("\nconnectToPeer " + device.deviceName);
        _textValue.append("\nconnectToPeer begin: " + _textValue.hashCode());
        config.deviceAddress = device.deviceAddress;
        _wifiP2pManager.connect(_channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                _textValue.append("\nConnected to " + device.deviceName);
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    public void SendData() {
        _wifiP2pManager.requestConnectionInfo(_channel, new WifiP2pManager.ConnectionInfoListener() {

            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                if(info.groupOwnerAddress == null) {
                    return;
                }

                ClientSocket sendTask = new ClientSocket(info.groupOwnerAddress);
                sendTask.getServerResponse()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            new Action1<String>() {
                                @Override
                                public void call(final String result) {
                                    _textValue.append("\nSending data complete " + result);
                                }
                            },
                            new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    _textValue.append("\nError sending data " + throwable.getMessage());
                                }
                            });
            }
        });
    }
}

