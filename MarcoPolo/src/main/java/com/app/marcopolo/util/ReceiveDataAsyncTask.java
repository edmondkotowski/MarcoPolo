package com.app.marcopolo.util;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ReceiveDataAsyncTask extends AsyncTask<String, String, String> {

    private Context context;
    private TextView statusText;

    public ReceiveDataAsyncTask(Context context, View statusText) {
        this.context = context;
        this.statusText = (TextView) statusText;
    }

    @Override
    protected String doInBackground(final String... strings) {
        try {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket client = serverSocket.accept();

            InputStream inputstream = client.getInputStream();
            String receivedValue = new Scanner(inputstream, "UTF-8").next();

            serverSocket.close();
            return receivedValue;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            statusText.append("\n Received data - " + result);
        }
    }
}
