package com.app.marcopolo.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.*;
import rx.Observer;
import rx.functions.Action1;

import java.util.Collection;
import java.util.Map;

public class ConnectionManager extends BroadcastReceiver {
    private final WifiP2pManager _wifiP2pManager;
    private final WifiP2pManager.Channel _channel;
    private final Map<String, WifiP2pDevice> _devicesLookup;
    private Observer<String> _logObserver;

    public ConnectionManager(WifiP2pManager wifiP2pManager,
                             WifiP2pManager.Channel channel,
                             Map<String, WifiP2pDevice> devicesLookup,
                             rx.Observer<String> logObserver) {

        if(wifiP2pManager == null) {
            throw new IllegalArgumentException("wifiP2pManager");
        }

        _wifiP2pManager = wifiP2pManager;
        _channel = channel;
        _logObserver = logObserver;
        _devicesLookup = devicesLookup;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                _logObserver.onNext("Wifi enabled");
                _logObserver.onNext("Receive begin");
            } else {
                _logObserver.onNext("Wifi not enabled");
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            _logObserver.onNext("PEERS CHANGED ACTION");
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
        _logObserver.onNext("Discover peers");
        _wifiP2pManager.discoverPeers(_channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                _logObserver.onNext("Discover peers success");
            }

            @Override
            public void onFailure(int reasonCode) {
            }
        });
    }

    public void connectToPeer(final WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        _logObserver.onNext("connectToPeer " + device.deviceName);
        _logObserver.onNext("connectToPeer begin");
        config.deviceAddress = device.deviceAddress;
        _wifiP2pManager.connect(_channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                _logObserver.onNext("Connected to " + device.deviceName);
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
                        .subscribe(
                                new Action1<String>() {
                                    @Override
                                    public void call(final String result) {
                                        _logObserver.onNext("Sending data complete " + result);
                                    }
                                },
                                new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        _logObserver.onNext("Error sending data " + throwable.getMessage());
                                    }
                                }
                        );
            }
        });
    }
}

