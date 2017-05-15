/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.hwx.wheel.steeringwheel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.hwx.wheel.steeringwheel.bluetooth.ScaleActivity;

import java.io.File;
import java.util.concurrent.TimeUnit;


public class Application extends android.app.Application implements
        Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Intent intent = new Intent(this, ScaleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public static Context mContext;
    private static Application _instance;
    public static Application getInstance() {
        return _instance;
    }
    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        mContext=this;
        AppConfig.getInstance();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px( float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(float pxValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static void sendLocalBroadCast(String action) {
        sendLocalBroadCast(action, null);
    }

    public static void sendLocalBroadCast(String action, Bundle bundle) {
        Intent bi = new Intent();
        bi.setAction(action);
        if (bundle != null)
            bi.putExtras(bundle);
        LocalBroadcastManager.getInstance(getContext())
                .sendBroadcast(bi);
    }

}
