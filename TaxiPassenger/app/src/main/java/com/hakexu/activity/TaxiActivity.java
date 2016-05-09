package com.hakexu.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.hakexu.taxipassenger.CallTaxiService;
import com.hakexu.taxipassenger.R;

import java.util.List;

public class TaxiActivity extends AppCompatActivity implements View.OnClickListener, BDLocationListener, ServiceConnection {


    private MapView mapView;
    private BaiduMap baiduMap;
    private Button btnInception, btnDestination, btnStart, btnReset;
    private boolean flag = true;
    private List<Poi> poiList;
    private String city;
    private String inception,destination;
    private LocationClient locationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi);
        /*初始化控件*/
        mapView = (MapView) findViewById(R.id.map);
        btnInception = (Button) findViewById(R.id.btn_inception);
        btnDestination = (Button) findViewById(R.id.btn_destination);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnReset = (Button) findViewById(R.id.btn_reset);
        /*设置监听事件*/
        btnInception.setOnClickListener(this);
        btnDestination.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnReset.setOnClickListener(this);

        /*隐藏缩放控件*/
        mapView.showZoomControls(false);

        /*初始化百度地图*/
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        initLocationClient();

        Intent intent = new Intent(this, CallTaxiService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    /*初始化定位组件*/
    private void initLocationClient() {
        locationClient = new LocationClient(getApplicationContext());
        /*注册位置监听器*/
        locationClient.registerLocationListener(this);
        /*设定定位内容*/
        LocationClientOption option = new LocationClientOption();
        /*获取详细描述*/
        option.setIsNeedLocationDescribe(true);
        /*获取地址信息*/
        option.setIsNeedAddress(true);
        /*获取周边地址*/
        option.setIsNeedLocationPoiList(true);
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        /*3秒更新一次*/
        option.setScanSpan(3000);
        /*定位模式*/
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null));
        locationClient.setLocOption(option);
        /*开始定位*/
        locationClient.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*停止定位，注销监听*/
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        locationClient.unRegisterLocationListener(this);
        mapView.onDestroy();
        unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.btn_inception:
                /*设定始发地，将附近地址提供为备选项*/
                intent = new Intent(TaxiActivity.this,ChoosePlaceActivity.class);
                bundle.clear();
                if(poiList==null)
                    return;
                String[] places= new String[poiList.size()];
                bundle.putString("title","请选择乘车位置");
                for(int i=0;i<poiList.size();i++){
                    places[i]=poiList.get(i).getName();
                }
                bundle.putStringArray("places",places);
                bundle.putString("city",city);
                intent.putExtra("data",bundle);
                startActivityForResult(intent,ChoosePlaceActivity.PlaceType.INCEPTION);
                break;
            case R.id.btn_destination:
                intent = new Intent(TaxiActivity.this,ChoosePlaceActivity.class);
                bundle.clear();
                bundle.putString("title","请选择要去的地方");
                bundle.putString("city",city);
                intent.putExtra("data",bundle);
                startActivityForResult(intent,ChoosePlaceActivity.PlaceType.DESTINATION);
                break;
            case R.id.btn_start:
                inception = btnInception.getText().toString();
                destination = btnDestination.getText().toString();
                if (inception.equals("出发地")||destination.equals("目的地")){
                    Toast.makeText(TaxiActivity.this,"请先选择起止地点",Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(TaxiActivity.this,CallTaxiActivity.class);
                bundle.clear();
                bundle.putString("inception",inception);
                bundle.putString("destination",destination);
                intent.putExtra("data",bundle);
                startActivity(intent);
                break;
            case R.id.btn_reset:{
                flag = true;
                btnInception.setText(this.getString(R.string.start_location));
                btnDestination.setText(this.getString(R.string.destination));
            }
                break;
            default:
                return;
        }
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if (bdLocation == null || mapView == null)
            return;
        baiduMap.clear();
        MyLocationData locationData = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                .direction(100).latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();
        baiduMap.setMyLocationData(locationData);

        LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
        if (flag) {
            /*自动将当前位置设为默认始发地*/
            String address = bdLocation.getAddrStr();
            if (address != null && address.length() > 0) {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("address", address);
                msg.setData(bundle);
                handler.sendMessage(msg);
                poiList = bdLocation.getPoiList();
                city = bdLocation.getCity();
            }
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng, 18);
            baiduMap.animateMapStatus(mapStatusUpdate);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode!=0)
            return;
        if (data==null)
            return;
        switch (requestCode){
            case ChoosePlaceActivity.PlaceType.INCEPTION:
                btnInception.setText(data.getStringExtra("place"));
                break;
            case ChoosePlaceActivity.PlaceType.DESTINATION:
                btnDestination.setText(data.getStringExtra("place"));
                break;
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String address = bundle.getString("address");
            if (address != null && address.length() > 0) {
                btnInception.setText(address);
                flag = false;
            }
        }
    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        final CallTaxiService.Binder binder = (CallTaxiService.Binder) service;

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
