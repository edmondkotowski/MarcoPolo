package com.app.marcopolo.util;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class HostSocket {

    public Observable<String> getClientResponse()
    {
        try {
            final ServerSocket serverSocket = new ServerSocket(8888);
            return Observable.just(serverSocket)
                    .subscribeOn(Schedulers.newThread())
                    .map(new Func1<ServerSocket, Socket>() {
                        @Override
                        public Socket call(final ServerSocket serverSocket) {
                            try {
                                return serverSocket.accept();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                    })
                    .map(new Func1<Socket, String>() {
                        @Override
                        public String call(final Socket socket) {
                            try {
                                InputStream inputStream = socket.getInputStream();
                                String receivedValue = new Scanner(inputStream, "UTF-8").next();

                                socket.getOutputStream().write(receivedValue.getBytes());

                                return receivedValue;
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                    })
                    .finallyDo(new Action0() {
                        @Override
                        public void call() {
                            try {
                                serverSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
            return Observable.empty();
        }
    }
}
