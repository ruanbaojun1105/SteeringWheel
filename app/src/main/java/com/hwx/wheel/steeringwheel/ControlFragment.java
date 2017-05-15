package com.hwx.wheel.steeringwheel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hwx.wheel.steeringwheel.bluetooth.BluetoothService;

/**
 * Created by Administrator on 2017/1/9.
 */

public class ControlFragment extends Fragment {

    private View rootView;
    private MyWheel mywheel;
    private TextView notice;
    private io.techery.progresshint.addition.widget.VerticalSeekBar seekBar2;
    private TextView reset;
    private String direction = "none";

    private int connSwitch=0;//默认用WIFI,0：wifi,1:蓝牙----通信方式

    public static ControlFragment newInstance(int connSwitch) {
        ControlFragment fragment = new ControlFragment();
        Bundle args = new Bundle();
        args.putInt("connSwitch", connSwitch);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.control_fragment, container, false);
        initView(rootView);
        connSwitch=getArguments().getInt("connSwitch");
        init();
        return rootView;
    }

    private void init() {
        seekBar2.getHintDelegate()
                .setHintAdapter((seekBar, progress) -> {
                    int a=progress-100;
                    String l="";
                    if (a>0){
                        l="\t向前";
                    }else {
                        l = "\t向后";
                    }
                    if (a==0)
                        l="";
                    switch (connSwitch){
                        case 0:
                            TcpConnActivity.sendData(getActivity(),(byte) 0x02, new byte[]{a < 0 ? ((byte) 0x00) : ((byte) 0x01), (byte) Math.abs(a)});
                            break;
                        case 1:
                            if (null != BluetoothService.getInstance() ) {
                                BluetoothService.getInstance().sendData((byte) 0x02, new byte[]{a < 0 ? ((byte) 0x00) : ((byte) 0x01), (byte) Math.abs(a)}, false);
                            }
                            break;
                    }

                    return "速度:"+a+l;
                });
        seekBar2.setOnTouchListener((view, motionEvent) -> {
            LogUtils.e("touch:"+seekBar2.getProgress());
            int a=seekBar2.getProgress()-100;
            switch (connSwitch){
                case 0:
                    TcpConnActivity.sendData(getActivity(),(byte) 0x02, new byte[]{a < 0 ? ((byte) 0x00) : ((byte) 0x01), (byte) Math.abs(a)});
                    break;
                case 1:
                    if (null != BluetoothService.getInstance() ) {
                        BluetoothService.getInstance().sendData((byte) 0x02, new byte[]{a < 0 ? ((byte) 0x00) : ((byte) 0x01), (byte) Math.abs(a)}, false);
                    }
                    break;
            }
            return false;
        });
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar2.postDelayed(() -> seekBar2.setProgress(100),10);
                switch (connSwitch){
                    case 0:
                        TcpConnActivity.sendData(getActivity(),(byte) 0x02, new byte[]{(byte) 0x01, (byte) 0});
                        TcpConnActivity.sendData(getActivity(),(byte) 0x02, new byte[]{(byte) 0x01, (byte) 0});
                        TcpConnActivity.sendData(getActivity(),(byte) 0x02, new byte[]{(byte) 0x01, (byte) 0});
                        break;
                    case 1:
                        if (null != BluetoothService.getInstance()){
                            BluetoothService.getInstance().sendData((byte) 0x02, new byte[]{(byte) 0x01, (byte) 0}, false);
                            BluetoothService.getInstance().sendData((byte) 0x02, new byte[]{(byte) 0x01, (byte) 0}, false);
                            BluetoothService.getInstance().sendData((byte) 0x02, new byte[]{(byte) 0x01, (byte) 0}, false);
                        }
                        break;
                }
            }
        });
        //seekbar.setProgress(100);
        seekBar2.postDelayed(() -> seekBar2.setProgress(100),500);
        initListener();
    }

    private void initView(View rootView) {
        mywheel = (MyWheel) rootView.findViewById(R.id.mywheel);
        notice = (TextView) rootView.findViewById(R.id.notice);
        seekBar2 = (io.techery.progresshint.addition.widget.VerticalSeekBar) rootView.findViewById(R.id.turns1);
        reset = (TextView) rootView.findViewById(R.id.reset);
    }
    public void initListener() {
        //下面的监听器代码最为重要！！！！！！！！
        mywheel.setOnMyWheelMoveListener((isSmallTouch, angle, power) -> {
            switch (connSwitch) {
                case 0:
                    TcpConnActivity.sendData(getActivity(),(byte) 0x01, new byte[]{angle < 0 ? ((byte) 0x00) : ((byte) 0x01), (byte) Math.abs(angle)});
                    break;
                case 1:
                    if (null != BluetoothService.getInstance() ){
                        BluetoothService.getInstance().sendData((byte) 0x01, new byte[]{angle < 0 ? ((byte) 0x00) : ((byte) 0x01), (byte) Math.abs(angle)}, false);
                    }
                    break;
            }
//            if (power >= 50) {
//                int temp = Math.abs(angle);
//                if (angle >= 0) {
//                    if (temp > 50 && temp < 120) {
//                        direction = "right";
//                        LogUtils.e(direction);
//                    } else if (temp < 40) {
//                        direction = "forward";
//                        LogUtils.e(direction);
//                    } else if (temp > 140) {
//                        direction = "back";
//                        LogUtils.e(direction);
//                    } else {
//                        direction = "指向不明确";
//                    }
//                } else {
//                    if (temp > 50 && temp < 120) {
//                        direction = "left";
//                        LogUtils.e(direction);
//                    } else if (temp < 40) {
//                        direction = "forward";
//                        LogUtils.e(direction);
//                    } else if (temp > 140) {
//                        direction = "back";
//                        LogUtils.e(direction);
//                    } else {
//                        direction = "指向不明确";
//                    }
//                }
//            } else {
//                direction = "stop";
//            }
            notice.setText("  getAngle:" + angle + "\t" + "  getPower:"
                    + power + "\t" + "direction:" + direction);

            if (direction.equals("stop")) {
                LogUtils.e(direction);
            }
        }, 100L);
        reset.setOnClickListener(view ->{
            seekBar2.post(() -> seekBar2.setProgress(100));
            switch (connSwitch){
                case 0:
                    for (int i = 0; i < 10; i++) {
                        TcpConnActivity.sendData(getActivity(),(byte) 0x02, new byte[]{(byte) 0x01, (byte) 0});
                    }
                    break;
                case 1:
                    if (null != BluetoothService.getInstance() ){
                        for (int i = 0; i < 10; i++) {
                            BluetoothService.getInstance().sendData((byte) 0x02, new byte[]{(byte) 0x01, (byte) 0}, false);
                        }
                    }
                    break;
            }

        });
//        seekbar.setOnSeekBarChangeListener(new MySeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(MySeekBar VerticalSeekBar, int progress, boolean fromUser) {
//                int a=progress-100;
//                String l="";
//                if (a>0){
//                    l="\t向前";
//                }else {
//                    l = "\t向后";
//                }
//                if (a==0) l="\t停止";
//                if (null != bluetoothService && isConnect){
//                    bluetoothService.sendData((byte) 0x02, new byte[]{a < 0 ? ((byte) 0x00) : ((byte) 0x01), (byte) a}, false);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(MySeekBar VerticalSeekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(MySeekBar VerticalSeekBar) {
//
//            }
//        });

    }
}
