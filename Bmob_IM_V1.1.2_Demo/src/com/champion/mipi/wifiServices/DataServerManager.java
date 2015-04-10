package com.champion.mipi.wifiServices;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.champion.mipi.bean.User;
import com.champion.mipi.weather.WeatherData;

public class DataServerManager {

    private static final String TAG = null;

    private List<OnFriendChangeListener> mFriendListenerList = new ArrayList<OnFriendChangeListener>();

    private static DataServerManager instences = null;

    private Context mContext;

    private ConnectService mService;

    private DataServerManager(Context c) {
        mContext = c;
        mService = ConnectService.getInstence();
        if (mService == null) {
            startServices();
        }
    }

    public DataServerManager getInstences(Context c) {
        if (instences == null) {
            instences = new DataServerManager(c);
        }
        return instences;
    }
    
    public WeatherData getWeatherData() {
        if (mService != null) {
            return mService.getWeatherData();
        }
        return null;
    }

    public List<User> getFriend() {
        return null;
    }

    public List<User> getWifiFriend() {

        return null;
    }

    public void registerFriendListener(OnFriendChangeListener listener) {
        if (!mFriendListenerList.contains(listener)) {
            mFriendListenerList.add(listener);
        }
    }

    public void unRregisterFriendListener(OnFriendChangeListener listener) {
        if (mFriendListenerList.contains(listener)) {
            mFriendListenerList.remove(listener);
        }
    }

    private void startServices() {
        Intent intentStartServices = new Intent();
        intentStartServices.setClass(mContext, ConnectService.class);
        Log.d(TAG, "startServices..............");
        mContext.startService(intentStartServices);
    }
}
