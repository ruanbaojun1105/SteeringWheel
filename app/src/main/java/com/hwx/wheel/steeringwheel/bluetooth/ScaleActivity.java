/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hwx.wheel.steeringwheel.bluetooth;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hwx.wheel.steeringwheel.AppConfig;
import com.hwx.wheel.steeringwheel.Application;
import com.hwx.wheel.steeringwheel.ControlFragment;
import com.hwx.wheel.steeringwheel.LogUtils;
import com.hwx.wheel.steeringwheel.MySeekBar;
import com.hwx.wheel.steeringwheel.MyWheel;
import com.hwx.wheel.steeringwheel.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.techery.progresshint.addition.widget.VerticalSeekBar;


public class ScaleActivity extends AppCompatActivity  {
    // Debugging
    private static final String TAG = "OtherActivity";
    private static final boolean D = true;

    private static final int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter mBluetoothAdapter = null;

    private Object lock = new Object();
    private StringBuilder builder = new StringBuilder("");
    private TextView connect_state;
    private TextView text_data;

    private BluetoothDevice connect_device;
    public static String CONNECT_MAC;//连接的mac地址
    private String CONNECT_NAME="";
    private ItemClickAdapter itemClickAdapter;
    private List<BluetoothDevice> deviceList=new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG, "+++ ON CREATE +++");
        setContentView(R.layout.bluetooth_other);
        connect_state = (TextView) findViewById(R.id.connect_state);
        text_data = (TextView) findViewById(R.id.text_data);
        CONNECT_MAC=AppConfig.getInstance().getString("isEmpetConnectMac","");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        itemClickAdapter=new ItemClickAdapter(deviceList);
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        LocalBroadcastManager.getInstance(Application.getContext()).registerReceiver(mReceiver, filter);
        new CommandReceiver() {
            @Override
            public void onDataReceived(byte[] buffer, byte function, byte safeCod) {
            }
            @Override
            public void onFail() {
                connect_state.setText("fail connect");
                builder.setLength(0);
                connect_state.postDelayed(() -> startOpen(),3000);
                startOpen();
            }
            @Override
            public void onLost() {
                connect_state.setText("lost connect");
                builder.setLength(0);
                connect_state.postDelayed(() -> startOpen(),3000);
            }
            @Override
            public void onDeviceInfo(String name, String address) {
                connect_state.append("\t"+name);
            }
            @Override
            public void onStadeTag(int stade) {
                switch (stade){
                    case 0:
                        connect_state.setText("we're doing nothing");
                        break;
                    case 1:
                        connect_state.setText("now listening for incoming connections");
                        break;
                    case 2:
                        connect_state.setText("now initiating an outgoing connection");
                        break;
                    case 3:
                        connect_state.setText("now connected to a remote "+CONNECT_NAME);
                        break;
                }
            }
        }.regiest();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, ControlFragment.newInstance(1)).commit();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return false;
    }

    public void startOpen() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");
        if (BluetoothService.getInstance().getState()==BluetoothService.STATE_CONNECTED){
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();//配对设备
            if (pairedDevices.size() > 0) {
                boolean has=false;
                for (BluetoothDevice device : pairedDevices) {
                    if (toCheck(device)){
                        has=true;
                        break;
                    }
                }
                if (!has)
                    doDiscovery();
            } else {
                doDiscovery();
            }
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");
        startOpen();
    }

    public synchronized boolean isEmpetConnectMac() {
        CONNECT_MAC=AppConfig.getInstance().getString("isEmpetConnectMac","");
        return TextUtils.isEmpty(CONNECT_MAC);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D)
            Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D)
            Log.e(TAG, "-- ON STOP --");
    }

    private void ensureDiscoverable() {
        if (D)
            Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private Dialog dialog;
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");
        mBluetoothAdapter.startDiscovery();
        if (isEmpetConnectMac()){
            deviceList.clear();
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();//配对设备
            for (BluetoothDevice device : pairedDevices){
                itemClickAdapter.addData(device);
            }
            if (dialog!=null&&dialog.isShowing()){
                dialog.dismiss();
            }
            dialog=BluetoothService.showBluetoothListDialog(this, itemClickAdapter, item -> {
                CONNECT_MAC=item.getAddress();
                CONNECT_NAME=item.getName();
                AppConfig.getInstance().putString("isEmpetConnectMac",CONNECT_MAC);
                toCheck(item);
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    doDiscovery();
                } else {
                    Log.d(TAG, "BT not enabled");
                    connect_state.setText(getString(R.string.bt_not_enabled_leaving));
                }
        }
    }

    private void connDevice(String address) {
        mBluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        BluetoothService.getInstance().connect(device);
    }

    private boolean toCheck(BluetoothDevice device){
        if (device.getAddress().equals(CONNECT_MAC)) {
            connect_device = device;
            connDevice(connect_device.getAddress());
            return true;
        }
        return false;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    toCheck(device);
                    if (isEmpetConnectMac()&&itemClickAdapter!=null){
                        itemClickAdapter.addData(device);
                    }
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            }
        }
    };
}