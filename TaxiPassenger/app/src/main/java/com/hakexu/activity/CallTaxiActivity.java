package com.hakexu.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Call;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hakexu.bean.Passenger;
import com.hakexu.taxipassenger.App;
import com.hakexu.taxipassenger.CallTaxiService;
import com.hakexu.taxipassenger.R;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;

public class CallTaxiActivity extends AppCompatActivity implements ServiceConnection {

    private String inception;
    private String destination;
    private String name;
    private String phone;
    private App app;

    private ProgressBar progressBar;
    private TextView tvDriver;
    private Button btnCancel, btnCall;
    private boolean isCall = true;

    private BufferedWriter writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_taxi);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCall = (Button) findViewById(R.id.btn_call);
        tvDriver = (TextView) findViewById(R.id.tv_driver);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + btnCall.getText().toString()));
                if (ActivityCompat.checkSelfPermission(CallTaxiActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(intent);
            }
        });

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        Bundle bundle = getIntent().getBundleExtra("data");
        if (bundle == null)
            return;
        inception = bundle.getString("inception");
        destination = bundle.getString("destination");

        app = (App) getApplication();
        name = app.getName();
        phone = app.getPhone();

        final Passenger passenger = new Passenger(inception, destination, name, phone);

        Gson gson = new Gson();
        final String json = gson.toJson(passenger);
        Intent intent = new Intent(CallTaxiActivity.this, CallTaxiService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isCall) {
                    if (writer != null) {
                        try {
                            writer.write(json+"\n");
                            writer.flush();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        final CallTaxiService.Binder binder = (CallTaxiService.Binder) service;
        if (binder != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (writer==null){
                        writer = binder.getService().getWriter();
                    }
                }
            }).start();
            binder.getService().setReceiverListener(new CallTaxiService.ReceiverListener() {
                @Override
                public void onReceivedMessage(String message) {
                    try{
                        JSONObject obj = new JSONObject(message);
                        if (obj.getString("name").equals(app.getName())){
                            String driver = obj.getString("driver");
                            String phone = obj.getString("phone");
                            Message msg = handler.obtainMessage();
                            Bundle bundle = msg.getData();
                            bundle.putString("driver",driver);
                            bundle.putString("phone",phone);
                            msg.setData(bundle);
                            handler.handleMessage(msg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*解绑并停止服务*/
        isCall = false;
        unbindService(this);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String name = bundle.getString("driver");
            String phone = bundle.getString("phone");
            progressBar.setVisibility(View.INVISIBLE);
            btnCancel.setVisibility(View.INVISIBLE);

            tvDriver.setVisibility(View.VISIBLE);
            tvDriver.setText(name);
            btnCall.setVisibility(View.VISIBLE);
            btnCall.setText(phone);
            isCall=false;
            unbindService(CallTaxiActivity.this);
        }
    };

}
