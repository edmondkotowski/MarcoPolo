package com.app.marcopolo.util;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class HostSocket {

    public Observable<String> getClientResponse()
    {
        try {
            final ServerSocket serverSocket = new ServerSocket(8888);
            return Observable
                .create(new Observable.OnSubscribe<Socket>() {
                    @Override
                    public void call(Subscriber<? super Socket> subscriber) {
                    // an data source that forever tries to accept new connections while the server socket is open
                        while (!serverSocket.isClosed()) {
                            try {
                                subscriber.onNext(serverSocket.accept());
                            } catch (IOException e) {
                                subscriber.onError(e);
                            }
                        }
                    }
                })
                .map(new Func1<Socket, String>() {
                    @Override
                    public String call(final Socket socket) {
                        try {
                            InputStream inputStream = new BufferedInputStream(socket.getInputStream());

                            // as a host, wait for the client to say something first...
                            String receivedValue = new Scanner(inputStream, "UTF-8").nextLine();

                            // echo back the received value
                            OutputStream outputStream = socket.getOutputStream();
                            PrintWriter printWriter = new PrintWriter(outputStream, true);
                            printWriter.println(receivedValue);

                            // and clean things up -- optional since the socket to the client will be closed?
                            printWriter.flush();
                            inputStream.close();
                            outputStream.close();

                            return receivedValue;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return e.getMessage();
                        } finally {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread()); // always execute asynchronously

            } catch (IOException e) {
                e.printStackTrace();
                return Observable.empty();
        }
    }
}
