package com.champion.mipi.wifiServices;

import android.content.Context;
import android.net.wifi.WifiManager;

public class WifiCommunication {

    public static final int BUFFERSIZE = 256;

    public static final String REG_HEAD = "REG";

    public static final String MESSAGE_HEAD = "MSG";

    private Context mContext; 
    
    private WifiManager mWifiManager;

    public WifiCommunication (Context c) {
        mContext = c;

        registerMonitorWifiNetwork();

        getLocalIp();
    }

    private void registerMonitorWifiNetwork() {
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

    }

    private void getLocalIp() {
        if (mWifiManager == null) {
            return;
        }
        if (!mWifiManager.isWifiEnabled()) {
            return;
        }
        
        
    }
}
