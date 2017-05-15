package com.hwx.wheel.steeringwheel.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.hwx.wheel.steeringwheel.Application;
import com.hwx.wheel.steeringwheel.Constants;


/**
 * Created by bj 2016.10.27
 */
public abstract class CommandReceiver extends BroadcastReceiver {

    public abstract void onDataReceived(final byte[] buffer, final byte function, byte safeCod);
    public abstract void onFail();
    public abstract void onLost();
    public abstract void onDeviceInfo(String name,String address);
    public abstract void onStadeTag(int stade);
    public void regiest() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.SERIAL_PORT_COMMAND);
        filter.addAction(Constants.SERIAL_PORT_CONNECT_FAIL);
        filter.addAction(Constants.SERIAL_PORT_CONNECT_LOST);
        filter.addAction(Constants.SERIAL_PORT_CONNECT_NAME);
        filter.addAction(Constants.SERIAL_PORT_CONNECT_STATE);
        LocalBroadcastManager.getInstance(Application.getContext()).registerReceiver(this, filter);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle=intent.getExtras();
        if (intent.getAction().equals(Constants.SERIAL_PORT_COMMAND)) {
            onDataReceived(bundle.getByteArray("data"),bundle.getByte("numberNo"),bundle.getByte("safeCode"));
        }else if (intent.getAction().equals(Constants.SERIAL_PORT_CONNECT_FAIL)) {
            onFail();
        }else if (intent.getAction().equals(Constants.SERIAL_PORT_CONNECT_LOST)) {
            onLost();
        }else if (intent.getAction().equals(Constants.SERIAL_PORT_CONNECT_NAME)) {
            onDeviceInfo(bundle.getString("name"),bundle.getString("address"));
        }else if (intent.getAction().equals(Constants.SERIAL_PORT_CONNECT_STATE)) {
//            Constants that indicate the current connection state
//            public static final int STATE_NONE = 0; // we're doing nothing
//            public static final int STATE_LISTEN = 1; // now listening for incoming
//            // connections
//            public static final int STATE_CONNECTING = 2; // now initiating an outgoing
//            // connection
//            public static final int STATE_CONNECTED = 3; // now connected to a remote
//            // device
            onStadeTag(bundle.getInt("state"));
        }
    }
}
