package com.app.marcopolo;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import com.app.marcopolo.util.ConnectionManager;
import com.app.marcopolo.util.HostSocket;
import com.app.marcopolo.util.SystemUiHider;
import rx.android.observables.ViewObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

import java.util.HashMap;
import java.util.Map;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Main extends Activity {
    private ConnectionManager _connectionManager;
    private IntentFilter _intentFilter;
    private HostSocket _receiveDataTask;
    private final PublishSubject<String> _logSubject = PublishSubject.create();

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

        _logSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    // UI log subscriber
                    final TextView textValue = (TextView) findViewById(R.id.textView);
                    final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);

                    {
//                        textValue.setMovementMethod(ScrollingMovementMethod.getInstance());
                        textValue.setText("");
                    }

                    @Override
                    public void call(String s) {
                        textValue.append(s + "\n");
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });

        _logSubject
                .subscribe(new Action1<String>() {
                    // alternate log subscriber
                    @Override
                    public void call(String s) {
                        Log.d("MarcoPolo", s + "\n");
                    }
                });

        _logSubject.onNext("onCreate begin. ");

        final Map<String, WifiP2pDevice> devicesLookup = new HashMap<>();

        ViewObservable.clicks(findViewById(R.id.dummy_button), false)
                .subscribe(new Action1<View>() {
                    @Override
                    public void call(View view) {
                        _connectionManager.discoverPeers();
                    }
                });

        ViewObservable.clicks(findViewById(R.id.send_button), false)
                .filter(new Func1<View, Boolean>() {
                    @Override
                    public Boolean call(View view) {
                        return !devicesLookup.isEmpty();
                    }
                })
                .subscribe(new Action1<View>() {
                    @Override
                    public void call(View view) {
                        _connectionManager.SendData();
                    }
                });

        WifiP2pManager manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(this, getMainLooper(), null);
        _connectionManager = new ConnectionManager(manager, channel, devicesLookup, _logSubject);

        _intentFilter = new IntentFilter();
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        _intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        _logSubject.onNext("onCreate end.");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        _receiveDataTask = new HostSocket();

        _receiveDataTask.getClientResponse()
                .subscribe(
                        new Action1<String>() {
                            @Override
                            public void call(final String result) {
                                _logSubject.onNext("Received data - " + result);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                _logSubject.onNext("Error receiving data - " + throwable.getMessage());
                            }
                        }
                );
    }
}
