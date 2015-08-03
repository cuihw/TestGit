package com.champion.mipi.wifiServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.champion.mipi.bean.User;
import com.champion.mipi.weather.WeatherData;

public class ConnectService extends Service {

    private static final String TAG = "ConnectServices";

    private Map<String, User> mUserinfoMap = new HashMap<String, User>();// current

    private List<String> mUserAccount = new ArrayList<String>();

    private IBinder mBinder = new ServiceBinder();// bind service;

    public static ConnectService getInstence;

    // System Action declare
    public static final String bootCompleted = "android.intent.action.BOOT_COMPLETED";
    public static final String WIFIACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String ETHACTION = "android.intent.action.ETH_STATE";
    public static final String updateMyInformationAction = "com.champion.mipis.updateMyInformation";

    private WifiCommunication mWifiCommunication;
    
    private WeatherData mWeatherData;

    public Map<String, User> getUserInfoMap() {
        return mUserinfoMap;
    }

    public Map<String, User> getWifiUserInfoMap() {
        return mWifiCommunication.getUserinfoMap();
    }
    

    // bind service.
    @Override
    public IBinder onBind(Intent arg0) {

        init();
        return mBinder;
    }

    public class ServiceBinder extends Binder {
        public ConnectService getService() {
            getInstence = ConnectService.this;
            return ConnectService.this;
        }
    }

    public static ConnectService getInstence() {
        return getInstence;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getInstence = ConnectService.this;
        init();
        Log.d(TAG, "onStartCommand ");
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    public void init() {
        mWeatherData = new WeatherData(this);
        mWifiCommunication = WifiCommunication.getInstence(this);
        
        DemoPisInfo demoInfo = new DemoPisInfo();

    }

    public WeatherData getWeatherData() {
        return mWeatherData;
    }


}
