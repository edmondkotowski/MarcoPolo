package com.app.marcopolo;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.app.marcopolo.groups.FriendGroup;
import com.app.marcopolo.util.ConnectionManager;
import com.app.marcopolo.util.HostSocket;
import com.app.marcopolo.util.SystemUiHider;
import rx.android.observables.ViewObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Main extends Activity {
    private HostSocket _receiveDataTask;
    private final PublishSubject<String> _logSubject = PublishSubject.create();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().hide();

        setContentView(R.layout.activity_main);

        _logSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    // UI log subscriber
                    final TextView textValue = (TextView) findViewById(R.id.textView);
                    final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
                    final WebView webView = (WebView) findViewById(R.id.webView);

                    {
//                        textValue.setMovementMethod(ScrollingMovementMethod.getInstance());
                        textValue.setText("");
                    }

                    @Override
                    public void call(String s) {
                        textValue.append(s + "\n");
                        scrollView.fullScroll(View.FOCUS_DOWN);

                        webView.loadData("<html><body bgcolor=black><font color=red><i>" + s + "</font></i></body></html>", "text/html", "UTF-8");
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
        ViewObservable.clicks(findViewById(R.id.new_group_button), false)
                .subscribe(new Action1<View>() {
                    @Override
                    public void call(View view) {
                        Intent groupIntent = new Intent(getApplicationContext(), EditGroup.class);

                        groupIntent.putExtra(EditGroup.class.getName(), new FriendGroup(getResources().getString(R.string.default_group_name)));
                        startActivity(groupIntent);
                    }
                });

        ViewObservable.clicks(findViewById(R.id.send_button), false)
                .subscribe(new Action1<View>() {
                    @Override
                    public void call(View view) {
                    }
                });

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
