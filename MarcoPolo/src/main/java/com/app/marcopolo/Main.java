package com.app.marcopolo;

import android.content.*;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.widget.TextView;
import com.app.marcopolo.util.ConnectionManager;
import com.app.marcopolo.util.ReceiveDataAsyncTask;
import com.app.marcopolo.util.SendDataAsyncTask;
import com.app.marcopolo.util.SystemUiHider;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import java.util.*;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Main extends Activity {
    private TextView _textValue;
    private ConnectionManager _connectionManager;
    private IntentFilter _intentFilter;
    private Map<String, WifiP2pDevice> _devicesLookup;
    private ReceiveDataAsyncTask _receiveDataTask;

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
        unregisterReceiver(_connectionManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        _textValue = (TextView) findViewById(R.id.textView);

        _textValue.append("\nonCreate begin: " + _textValue.hashCode());

        _devicesLookup = new HashMap<>();

        findViewById(R.id.dummy_button).setOnClickListener(_connectTouchListener);
        findViewById(R.id.send_button).setOnClickListener(_sendTouchListener);

        WifiP2pManager manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(this, getMainLooper(), null);
        _connectionManager = new ConnectionManager(manager, this, channel, _textValue, _devicesLookup);

        _intentFilter = new IntentFilter();
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        _textValue.append("\nonCreate end: " + _textValue.hashCode());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        _receiveDataTask = new ReceiveDataAsyncTask(this.getApplicationContext(), _textValue);
//        _receiveDataTask.execute();
    }

    View.OnClickListener _connectTouchListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            _connectionManager.discoverPeers();
        }
    };

    View.OnClickListener _sendTouchListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if(!_devicesLookup.isEmpty()) {
                String deviceKey = (String)_devicesLookup.keySet().toArray()[0];
                SendDataAsyncTask sendDataTask = new SendDataAsyncTask(null, _textValue, _devicesLookup.get(deviceKey));
                sendDataTask.execute();
            }
        }
    };
}
