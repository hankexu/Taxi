package com.hankexu.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hankexu.taxidriver.R;

public class PassengerActivity extends AppCompatActivity {


    private TextView tvInception, tvDestination, tvName;

    private Button btnCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);

        tvDestination = (TextView) findViewById(R.id.tv_destination);
        tvInception = (TextView) findViewById(R.id.tv_inception);
        tvName = (TextView) findViewById(R.id.tv_name);
        btnCall = (Button) findViewById(R.id.btn_call);

        Intent intent = getIntent();


        String name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        String inception = intent.getStringExtra("inception");
        String destination = intent.getStringExtra("destination");

        tvDestination.setText(destination);
        tvInception.setText(inception);
        tvName.setText(name);
        btnCall.setText(phone);

        /*拨打电话*/
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + btnCall.getText().toString())));
            }
        });
    }
}
