package com.hakexu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hakexu.taxipassenger.App;
import com.hakexu.taxipassenger.R;

public class MainActivity extends AppCompatActivity {


    private App app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*从sharedPreferences中读取个人信息*/
        app = (App) getApplication();
        Intent intent;
        if(app.getName()==null){
            intent = new Intent(MainActivity.this,InfoActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            intent = new Intent(MainActivity.this,TaxiActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
