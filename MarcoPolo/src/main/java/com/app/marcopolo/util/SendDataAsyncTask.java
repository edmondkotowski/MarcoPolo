package com.app.marcopolo.util;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SendDataAsyncTask extends AsyncTask<String, String, String> {

    private final InetAddress _wifiP2pDevice;
    private final TextView _statusText;

    public SendDataAsyncTask(View statusText, InetAddress deviceAddress) {
        if(statusText == null) {
            throw new IllegalArgumentException("_statusText");
        }
        if(deviceAddress == null) {
            throw new IllegalArgumentException("deviceAddress");
        }

        _wifiP2pDevice = deviceAddress;
        _statusText = (TextView) statusText;


    }

    @Override
    protected String doInBackground(final String... strings) {
        try {
            Socket socket = new Socket();
            socket.bind(null);

            socket.connect((new InetSocketAddress(_wifiP2pDevice.getHostAddress(), 8888)), 500);

            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data will be retrieved by the server device.
             */
            OutputStream outputStream = socket.getOutputStream();

            final Long nanoNow = System.nanoTime();
            outputStream.write(nanoNow.toString().getBytes());

            outputStream.close();
            return nanoNow.toString();
        } catch (FileNotFoundException e) {
            //catch logic
        } catch (IOException e) {
            return e.getMessage();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            _statusText.append("\n Sending data complete" + result);
        }
    }
}
