package com.hwx.wheel.steeringwheel;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hwx.wheel.steeringwheel.bluetooth.BluetoothService;
import com.hwx.wheel.steeringwheel.tcp.BackService;
import com.hwx.wheel.steeringwheel.tcp.IBackService;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/1/9.
 */

public class TcpConnActivity extends AppCompatActivity {

    private int connSwitch=0;//默认用WIFI,0：wifi,1:蓝牙----通信方式

    private IBackService iBackService;
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iBackService = null;

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iBackService = IBackService.Stub.asInterface(service);
        }
    };

    private MessageBackReciver mReciver;

    private IntentFilter mIntentFilter;

    private LocalBroadcastManager mLocalBroadcastManager;
    private TextView mResultText;
    private EditText mEditText;
    private Intent mServiceIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank_activity);


        mResultText = (TextView) findViewById(R.id.resule_text);
        mEditText = (EditText) findViewById(R.id.content_edit);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mReciver = new MessageBackReciver(mResultText);

        mServiceIntent = new Intent(this, BackService.class);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BackService.HEART_BEAT_ACTION);
        mIntentFilter.addAction(BackService.MESSAGE_ACTION);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, ControlFragment.newInstance(0)).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocalBroadcastManager.registerReceiver(mReciver, mIntentFilter);
        bindService(mServiceIntent, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
        mLocalBroadcastManager.unregisterReceiver(mReciver);
    }

    public static boolean sendData(Activity activity,byte function,byte[] content) {
        if (!(activity instanceof TcpConnActivity))
            return false;
        String  data=new String(BluetoothService.reData(function,content,true));
        boolean isSend = false;//Send Content by socket
        try {
            isSend = ((TcpConnActivity)activity).getiBackService().sendMessage(data);
        } catch (RemoteException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return isSend;
    }

    public IBackService getiBackService() {
        return iBackService;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send:
                String  data=new String(new byte[]{0x30,0x31,0x33,0x3a,0x3b});
                //String content = mEditText.getText().toString();
                try {
                    boolean isSend = iBackService.sendMessage(data);//Send Content by socket
                    LogUtils.e("send:"+(isSend ? "success" : "fail"));
                    mEditText.setText("");
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
    class MessageBackReciver extends BroadcastReceiver {


        private WeakReference<TextView> textView;

        public MessageBackReciver(TextView tv) {
            textView = new WeakReference<TextView>(tv);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TextView tv = textView.get();
            if (action.equals(BackService.HEART_BEAT_ACTION)) {
                if (null != tv) {
                    tv.setText("Get a heart heat");
                }
            } else {
                String message = intent.getStringExtra("message");
                tv.setText(message);
            }
        };
    }
}
