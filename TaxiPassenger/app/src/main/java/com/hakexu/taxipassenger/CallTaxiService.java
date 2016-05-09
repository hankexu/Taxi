package com.hakexu.taxipassenger;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by hanke on 2016-01-07.
 */
public class CallTaxiService extends Service {

    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private static final String SERVERIP = "192.168.137.1";
    private static final int PORT = 9999;
    private ReceiverListener receiverListener;

    public BufferedWriter getWriter() {
        return writer;
    }

    public void setReceiverListener(ReceiverListener receiverListener) {
        this.receiverListener = receiverListener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*创建连接*/
        new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    socket = new Socket(SERVERIP, PORT);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        publishProgress(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                if (receiverListener != null) {
                    receiverListener.onReceivedMessage(values[0]);
                }
            }
        }.execute();

    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class Binder extends android.os.Binder {
        public CallTaxiService getService() {
            return CallTaxiService.this;
        }
    }

    public interface ReceiverListener {
        void onReceivedMessage(String message);
    }

}
