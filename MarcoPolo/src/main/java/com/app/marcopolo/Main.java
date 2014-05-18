package com.app.marcopolo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.*;
import com.app.marcopolo.groups.FriendGroup;
import com.app.marcopolo.groups.GroupStore;
import com.app.marcopolo.util.HostSocket;
import com.app.marcopolo.util.SystemUiHider;
import rx.android.observables.ViewObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.util.ArrayList;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class Main extends Activity {
    private HostSocket _receiveDataTask;
    private final PublishSubject<String> _logSubject = PublishSubject.create();
    private final GroupStore _groupStore;
    private ListView _groupList;
    private ArrayAdapter<String> _listViewAdapter;

    public Main() {
        _groupStore = new GroupStore(this, new FileNameProvider());
    }


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
                        openGroup(new FriendGroup(getResources().getString(R.string.default_group_name)));
                    }
                });

        ViewObservable.clicks(findViewById(R.id.send_button), false)
                .subscribe(new Action1<View>() {
                    @Override
                    public void call(View view) {
                    }
                });

        _logSubject.onNext("onCreate end.");

        _groupList = (ListView) findViewById(R.id.group_list);

        _listViewAdapter = new ArrayAdapter<>(this, R.layout.group_member, new ArrayList<String>());
        _groupList.setAdapter(_listViewAdapter);
        _groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                String groupName = (String)_groupList.getItemAtPosition(position);
                try {
                    FriendGroup group = _groupStore.getGroup(groupName);
                    openGroup(group);
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "open group failed:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void openGroup(FriendGroup group) {
        Intent groupIntent = new Intent(getApplicationContext(), EditGroup.class);

        groupIntent.putExtra(EditGroup.class.getName(), group);
        startActivity(groupIntent);
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

        _listViewAdapter.addAll(_groupStore.getGroupNames());
    }

    // register the broadcast receiver with the intent values to be matched
    @Override
    protected void onResume() {
        super.onResume();
        _listViewAdapter.clear();
        _listViewAdapter.addAll(_groupStore.getGroupNames());
    }
}
