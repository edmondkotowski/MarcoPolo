package com.app.marcopolo.util;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ClientSocket {

    private final InetAddress _wifiP2pDevice;

    public ClientSocket(InetAddress deviceAddress) {
        if(deviceAddress == null) {
            throw new IllegalArgumentException("deviceAddress");
        }

        _wifiP2pDevice = deviceAddress;
    }

    public Observable<String> getServerResponse() {

        return Observable
                .create(new Observable.OnSubscribe<Socket>() {
                    // a data source that creates a connection to the host
                    @Override
                    public void call(Subscriber<? super Socket> subscriber) {
                        Socket socket = new Socket();
                        try {
                            socket.bind(null);
                            socket.connect((new InetSocketAddress(_wifiP2pDevice.getHostAddress(), 8888)), 500);
                            subscriber.onNext(socket);
                        } catch (IOException e) {
                            subscriber.onError(e);
                        }
                    }
                })
                .map(new Func1<Socket, String>() {
                    @Override
                    public String call(final Socket socket) {
                        try {
                            /**
                             * Create a byte stream from a JPEG file and pipe it to the output stream
                             * of the socket. This data will be retrieved by the server device.
                             */
                            OutputStream outputStream = socket.getOutputStream();
                            InputStream inputStream = new BufferedInputStream(socket.getInputStream());

                            // communicate with the host first -- send a timestamp
                            final Long nanoNow = System.nanoTime();
                            PrintWriter printWriter = new PrintWriter(outputStream, true);
                            printWriter.println(nanoNow.toString());
                            printWriter.flush();

                            // wait for the host to echo back the timestamp
                            String receivedValue = new Scanner(inputStream, "UTF-8").nextLine();

                            // clean up
                            inputStream.close();
                            outputStream.close();

                            return receivedValue;
                        } catch (FileNotFoundException e) {
                            //catch logic
                            return e.getMessage();
                        } catch (IOException e) {
                            return e.getMessage();
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread()); // always execute asynchronously
    }
}
