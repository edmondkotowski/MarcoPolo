package com.app.marcopolo.util;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import rx.Observable;
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

        Socket socket = new Socket();

        return Observable.just(socket)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Socket, String>() {
                    @Override
                    public String call(final Socket socket) {

                        try {
                            socket.bind(null);

                            socket.connect((new InetSocketAddress(_wifiP2pDevice.getHostAddress(), 8888)), 500);

                            /**
                             * Create a byte stream from a JPEG file and pipe it to the output stream
                             * of the socket. This data will be retrieved by the server device.
                             */
                            OutputStream outputStream = socket.getOutputStream();

                            final Long nanoNow = System.nanoTime();
                            PrintWriter printWriter = new PrintWriter(outputStream);
                            printWriter.println(nanoNow);
                            printWriter.flush();

                            InputStream inputStream = socket.getInputStream();
                            String receivedValue = new Scanner(inputStream, "UTF-8").next();
                            inputStream.close();
                            printWriter.close();

                            return receivedValue;
                        } catch (FileNotFoundException e) {
                            //catch logic
                            return e.getMessage();
                        } catch (IOException e) {
                            return e.getMessage();
                        }
                    }
                });

    }
}
