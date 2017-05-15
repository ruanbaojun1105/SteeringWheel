package com.hwx.wheel.steeringwheel;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.SharedPreferencesCompat;

/**
 * Created in Nov 8, 2015 7:48:11 PM.
 *
 * @author Yan Zhenjie.
 */
public class AppConfig {

    private static AppConfig appConfig;

    private SharedPreferences preferences;

    /**
     * 是否是测试环境.
     */
    public static final boolean DEBUG = false;


    private AppConfig() {
        preferences = Application.getInstance().getSharedPreferences("HWXSharedPreferences_Wheel", Context.MODE_PRIVATE);
    }

    public static AppConfig getInstance() {
        if (appConfig == null)
            appConfig = new AppConfig();
        return appConfig;
    }
    public void putInt(String key, int value) {
        //preferences.edit().putInt(key, value).commit();
        SharedPreferencesCompat.EditorCompat.getInstance().apply(preferences.edit().putInt(key, value));
    }

    public int getInt(String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public void putString(String key, String value) {
        //preferences.edit().putString(key, value).commit();
        SharedPreferencesCompat.EditorCompat.getInstance().apply(preferences.edit().putString(key, value));
    }

    public String getString(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public void putBoolean(String key, boolean value) {
        //preferences.edit().putBoolean(key, value).commit();
        SharedPreferencesCompat.EditorCompat.getInstance().apply(preferences.edit().putBoolean(key, value));
    }

    public boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public void putLong(String key, long value) {
        //preferences.edit().putLong(key, value).commit();
        SharedPreferencesCompat.EditorCompat.getInstance().apply(preferences.edit().putLong(key, value));
    }

    public long getLong(String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    public void putFloat(String key, float value) {
        preferences.edit().putFloat(key, value).commit();
    }

    public float getFloat(String key, float defValue) {
        return preferences.getFloat(key, defValue);
    }
}