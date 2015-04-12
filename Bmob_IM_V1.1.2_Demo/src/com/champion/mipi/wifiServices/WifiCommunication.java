package com.champion.mipi.wifiServices;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.champion.mipi.bean.User;

public class WifiCommunication {

    public static final int BUFFERSIZE = 256;

    public static final String REG_HEAD = "REG";
    public static final String MESSAGE_HEAD = "MSG";
    public static final String ACK_REG = "ACR";
    public static final String ACK_MESSAGE = "ACM";

    private static final String TAG = null;

    private Context mContext;

    private WifiManager mWifiManager;

    private InetAddress localInetAddress;

    private String localIp;

    private byte[] localIpBytes;
    
    private static final String MULTICAST_IP = "239.9.9.1";

    public WifiCommunication(Context c) {
        mContext = c;

        startWifiNetwork();

        getLocalIp();
    }

    private void startWifiNetwork() {
        regWifiReceiver();
        listenWifiMsg();
    }

    private void listenWifiMsg() {
        
    }

    public void sendBroadcast(User myinfo) {
        
    }

    private void getLocalIp() {
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
        }

        if (!mWifiManager.isWifiEnabled()) {
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface
                            .getNetworkInterfaces(); en.hasMoreElements();) {
                        NetworkInterface intf = en.nextElement();

                        for (Enumeration<InetAddress> enumIpAddr = intf
                                .getInetAddresses(); enumIpAddr
                                .hasMoreElements();) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress()) {
                                if (inetAddress.isReachable(1000)) {

                                    localInetAddress = inetAddress;

                                    localIp = inetAddress.getHostAddress()
                                            .toString();

                                    localIpBytes = inetAddress.getAddress();

                                    Log.d(TAG, "localInetAddress = "
                                            + localInetAddress.toString()
                                            + "localIp = " + localIp
                                            + "localIpBytes = " + localIpBytes);
                                    notifyGetIpAddress();
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    protected void notifyGetIpAddress() {
        
    }

    private void regWifiReceiver() {

        ServiceWifiReceiver receiver = new ServiceWifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ServiceWifiReceiver.WIFIACTION);
        filter.addAction(ServiceWifiReceiver.ETHACTION);
        mContext.registerReceiver(receiver, filter);
    }

    // Receiver
    private class ServiceWifiReceiver extends BroadcastReceiver {
        public static final String WIFIACTION = "android.net.conn.CONNECTIVITY_CHANGE";
        public static final String ETHACTION = "android.intent.action.ETH_STATE";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WIFIACTION)
                    || intent.getAction().equals(ETHACTION)) {
                getLocalIp();
            }
        }
    }

}
