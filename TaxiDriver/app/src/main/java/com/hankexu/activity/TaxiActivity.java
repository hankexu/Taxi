package com.hankexu.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.hankexu.taxidriver.App;
import com.hankexu.adapter.OrderAdapter;
import com.hankexu.bean.Passenger;
import com.hankexu.taxidriver.R;
import com.hankexu.taxidriver.TaxiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TaxiActivity extends AppCompatActivity implements BDLocationListener, ServiceConnection, AdapterView.OnItemClickListener {


    private BufferedWriter writer;

    private LocationClient locationClient;

    private ListView listView;
    private OrderAdapter adapter;


    private ArrayList<Passenger> passengers;

    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi);

        app = (App) getApplication();

        passengers = new ArrayList<>();
        listView = (ListView) findViewById(R.id.lv);
        adapter = new OrderAdapter(passengers, this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        initLocationClient();
        Intent intent = new Intent(TaxiActivity.this, TaxiService.class);
        startService(intent);
        bindService(intent, this, Context.BIND_AUTO_CREATE);

    }

    /*初始化定位组件*/
    private void initLocationClient() {
        locationClient = new LocationClient(getApplicationContext());
        /*注册位置监听器*/
        locationClient.registerLocationListener(this);
        /*设定定位内容*/
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        /*3秒更新一次*/
        option.setScanSpan(3000);
        /*定位模式*/
        locationClient.setLocOption(option);
        /*开始定位*/
        locationClient.start();
    }

    /*定位，发送位置信息*/
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        double latitude = bdLocation.getLatitude();
        double longitude = bdLocation.getLongitude();
        String name = app.getName();
        String phone = app.getPhone();
        JSONObject root = new JSONObject();
        try {
            root.put("latitude", latitude);
            root.put("longitude", longitude);
            root.put("phone", phone);
            root.put("name", name);
            root.put("role", "driver");
            if (writer != null) {
                writer.write(root.toString() + "\n");
                writer.flush();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        final TaxiService.Binder binder = (TaxiService.Binder) service;
        if (binder != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (writer == null) {
                        writer = binder.getService().getWriter();
                    }
                }
            }).start();
            /*接收乘客信息*/
            binder.getService().setReceiveListener(new TaxiService.ReceiveListener() {
                @Override
                public void onReceived(String msg) {
                    try {
                        JSONArray jsonArray = new JSONArray(msg);
                        passengers.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = new JSONObject(jsonArray.get(i).toString());
                            Passenger passenger = new Passenger(obj.getString("inception"),
                                    obj.getString("destination"),
                                    obj.getString("name"),
                                    obj.getString("phone"));
                            passengers.add(passenger);
                        }
                        adapter.notifyDataSetChanged();
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
        Intent intent = new Intent(TaxiActivity.this, TaxiService.class);
        unbindService(this);
        stopService(intent);
        locationClient.stop();
        locationClient.unRegisterLocationListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final JSONObject root = new JSONObject();
        /*组建接单数据*/
        try {
            root.put("name", passengers.get(position).getName());
            root.put("role", "order");
            root.put("driver", app.getName());
            root.put("phone", app.getPhone());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*发送接单信息*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    writer.write(root.toString() + "\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Intent intent = new Intent(TaxiActivity.this, PassengerActivity.class);
        intent.putExtra("name", passengers.get(position).getName());
        intent.putExtra("phone", passengers.get(position).getPhone());
        intent.putExtra("inception",passengers.get(position).getInception());
        intent.putExtra("destination",passengers.get(position).getDestination());
        passengers.clear();
        adapter.notifyDataSetChanged();
        startActivity(intent);
    }
}
