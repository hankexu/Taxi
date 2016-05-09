package com.hakexu.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hakexu.taxipassenger.App;
import com.hakexu.taxipassenger.R;

public class InfoActivity extends AppCompatActivity {


    private EditText etName,etPhone;
    private Button btn;
    private App app;
    private String name,phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        app = (App) getApplication();

        etName = (EditText) findViewById(R.id.et_name);
        etPhone = (EditText) findViewById(R.id.et_phone);
        btn = (Button) findViewById(R.id.btn_confirm);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*保存姓名和手机号*/
                name=etName.getText().toString();
                phone = etPhone.getText().toString();
                if(name.length()>0&&phone.length()>0){
                    app.setName(name);
                    app.setPhone(phone);
                    startActivity(new Intent(InfoActivity.this,TaxiActivity.class));
                    finish();
                }else {
                    Toast.makeText(InfoActivity.this,"请先填写姓名和手机号",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
