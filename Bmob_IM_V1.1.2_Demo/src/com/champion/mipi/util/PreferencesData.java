package com.champion.mipi.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesData {

    public static final String SHARE_PRE = "SharedPreferences";
    public static SharedPreferences mSharedPreferences;
    
    public static SharedPreferences.Editor mEditor;
    
    public static void setStringData(Context c, String key, String value) {
        mSharedPreferences = c.getSharedPreferences( SHARE_PRE, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mEditor.putString(key, value);
        mEditor.commit();
    }
    
    public static String getStringData(Context c, String key, String defaulValue) {
        mSharedPreferences = c.getSharedPreferences( SHARE_PRE, Context.MODE_PRIVATE);
        String value = mSharedPreferences.getString(key, defaulValue);
        return value;
    }

    public static int getIntData(Context c, String key, int defaulValue) {
        mSharedPreferences = c.getSharedPreferences( SHARE_PRE, Context.MODE_PRIVATE);
        int value = mSharedPreferences.getInt(key, defaulValue);
        return value;
    }

    public static void setIntData(Context c, String key, int value) {
        mSharedPreferences = c.getSharedPreferences( SHARE_PRE, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mEditor.putInt(key, value);
        mEditor.commit();
    }
}
