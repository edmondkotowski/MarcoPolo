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
    private final WifiP2pManager.PeerListListener _peerListListener;
    private Observer<String> _logObserver;

    public ConnectionManager(WifiP2pManager wifiP2pManager,
                             WifiP2pManager.Channel channel,
                             WifiP2pManager.PeerListListener peerListListener,
                             Observer<String> logObserver) {

        if(wifiP2pManager == null) {
            throw new IllegalArgumentException("wifiP2pManager");
        }
        if(channel == null) {
            throw new IllegalArgumentException("channel can not be null");
        }
        if(peerListListener == null) {
            throw new IllegalArgumentException("peerListListener can not be null");
        }
        if(logObserver == null) {
            throw new IllegalArgumentException("logObserver can not be null");
        }

        _wifiP2pManager = wifiP2pManager;
        _channel = channel;
        _peerListListener = peerListListener;
        _logObserver = logObserver;
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
            _logObserver.onNext("PEERS CHANGED ACTION");;

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            _wifiP2pManager.requestPeers(_channel, _peerListListener);

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

    public void connectToPeer(final String deviceName, final String deviceAddress) {
        WifiP2pConfig config = new WifiP2pConfig();
        _logObserver.onNext("[info] beginning connectToPeer for " + deviceName);
        config.deviceAddress = deviceAddress;
        _wifiP2pManager.connect(_channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                _logObserver.onNext("[info] Connected to " + deviceName);
                SendData();
            }

            @Override
            public void onFailure(int reason) {
                _logObserver.onNext("[warning] connectToPeer for " + deviceName + " failed");
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

