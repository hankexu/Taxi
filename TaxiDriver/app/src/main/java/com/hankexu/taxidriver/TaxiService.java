package com.hankexu.taxidriver;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TaxiService extends Service {

    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private static final String SERVERIP = "192.168.137.1";
    private static final int PORT=9999;
    private ReceiveListener receiveListener;


    public void setReceiveListener(ReceiveListener receiveListener) {
        this.receiveListener = receiveListener;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    @Override
    public IBinder onBind(Intent intent) {
            return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        new AsyncTask<Void,String,Void>(){

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    socket = new Socket(SERVERIP,PORT);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    String line;
                    if (!socket.isClosed()){
                        while ((line=reader.readLine())!=null){
                            publishProgress(line);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                if (receiveListener!=null){
                    receiveListener.onReceived(values[0]);
                }
            }
        }.execute();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return super.onUnbind(intent);
    }

    public class Binder extends android.os.Binder{
        public TaxiService getService(){
            return TaxiService.this;
        }
    }

    public interface ReceiveListener{
        void onReceived(String msg);
    }

}
