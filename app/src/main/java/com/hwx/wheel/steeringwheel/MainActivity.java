package com.hwx.wheel.steeringwheel;

import android.content.Context;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hwx.wheel.steeringwheel.bluetooth.ScaleActivity;

public class MainActivity extends AppCompatActivity{

    private MyWheel mtwheel;
    private TextView notice;
    private TextView show;
    private String direction = "none";
    private SeekBar seekbar;
    private static final String tag = "SimpleFragment";
    private boolean issend = false;
    private boolean isstop = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mtwheel = (MyWheel)  findViewById(R.id.mywheel);
        //控件下面的提示信息notice，其他控件大家可以忽略.
        notice = (TextView)  findViewById(R.id.notice);
        seekbar = (SeekBar)  findViewById(R.id.turns);
        seekbar.setProgress(50);
        initData();
        initListener();
    }

    public void initListener() {
        //下面的监听器代码最为重要！！！！！！！！
        mtwheel.setOnMyWheelMoveListener(new MyWheel.OnMyWheelMoveListener() {

            @Override
            // paramInt1:角度
            // paramInt2：距离 根据这两个参数可以算出方向盘的方位
            public void onValueChanged(boolean isSmallTouch,int angle, int power) {
                if (isSmallTouch)
                    seekbar.setProgress(power);
                if (power >= 50) {
                    int temp = Math.abs(angle);
                    if (angle >= 0) {
                        if (temp > 50 && temp < 120) {
                            direction = "right";
                            LogUtils.e(direction);
                        } else if (temp < 40) {
                            direction = "forward";
                            LogUtils.e(direction);
                        } else if (temp > 140) {
                            direction = "back";
                            LogUtils.e(direction);
                        } else {
                            direction = "指向不明确";
                        }
                    } else {
                        if (temp > 50 && temp < 120) {
                            direction = "left";
                            LogUtils.e(direction);
                        } else if (temp < 40) {
                            direction = "forward";
                            LogUtils.e(direction);
                        } else if (temp > 140) {
                            direction = "back";
                            LogUtils.e(direction);
                        } else {
                            direction = "指向不明确";
                        }
                    }
                } else {
                    direction = "stop";
                }
                notice.setText("  getAngle:" + angle + "\n" + "  getPower:"
                        + power + "\n" + "direction:" + direction);

                if (direction.equals("stop") && (!isstop)) {
                    LogUtils.e(direction);
                    isstop = true;
                }
            }
        }, 100L);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
            }
        });

    }

    public void initData() {
        Thread.currentThread().setName(tag);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    // @Override
    // public boolean onTouch(View v, MotionEvent event) {
    // if (v.getId() == R.id.turns) {
    // String notice = "";
    // switch (event.getAction()) {
    // case MotionEvent.ACTION_DOWN:
    // notice = "ACTION_DOWN"+event.getX();
    // int process=(int) Math.floor(event.getX()+0.5);
    // seekbar.setProgress(process);
    // break;
    // case MotionEvent.ACTION_UP:
    // notice = "ACTION_UP";
    // break;
    // case MotionEvent.ACTION_CANCEL:
    // notice = "ACTION_CANCEL";
    // break;
    // default:
    // break;
    // }
    // if (!TextUtils.isEmpty(notice)) {
    // Toast.makeText(getActivity(), notice, 0).show();
    // }
    // }
    // return true;
    // }

}
