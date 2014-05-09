package com.app.marcopolo.util;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SendDataAsyncTask extends AsyncTask<String, String, String> {

    private Context context;
    private final WifiP2pDevice _wifiP2pDevice;
    private final TextView statusText;

    public SendDataAsyncTask(Context context, View statusText, WifiP2pDevice wifiP2pDevice) {
        if(statusText == null) {
            throw new IllegalArgumentException("statusText");
        }
        if(wifiP2pDevice == null) {
            throw new IllegalArgumentException("wifiP2pDevice");
        }

        this.context = context;
        _wifiP2pDevice = wifiP2pDevice;
        this.statusText = (TextView) statusText;
    }

    @Override
    protected String doInBackground(final String... strings) {
        try {
            Socket socket = new Socket();
            socket.bind(null);
            socket.connect((new InetSocketAddress(_wifiP2pDevice.deviceName, 8888)), 500);

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
            statusText.append("\n Sending data complete" + result);
        }
    }
}
