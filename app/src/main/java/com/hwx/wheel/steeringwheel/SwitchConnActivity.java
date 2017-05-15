package com.hwx.wheel.steeringwheel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.hwx.wheel.steeringwheel.bluetooth.ScaleActivity;

/**
 * Created by Administrator on 2017/1/9.
 */

public class SwitchConnActivity extends AppCompatActivity implements View.OnClickListener {

    private int connSwitch = 0;//默认用WIFI,0：wifi,1:蓝牙----通信方式
    private Button wifi_btn;
    private Button bluetooth_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switch_conn_activity);
        initView();
    }

    private void initView() {
        wifi_btn = (Button) findViewById(R.id.wifi_btn);
        bluetooth_btn = (Button) findViewById(R.id.bluetooth_btn);

        wifi_btn.setOnClickListener(this);
        bluetooth_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wifi_btn:
                startActivity(new Intent(this,TcpConnActivity.class));
                break;
            case R.id.bluetooth_btn:
                startActivity(new Intent(this,ScaleActivity.class));
                break;
        }
    }
}
