package com.champion.mipi.wifiServices;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.champion.mipi.util.ByteAndInt;
import com.champion.mipi.util.CommonUtils;
import com.champion.mipi.util.PreferencesData;
import com.champion.mipi.util.UserInfo;

public class ConnectService  extends Service  {
	
    private static final String TAG = "ConnectServices";
	private static final boolean DEBUG = true;

    public static final int BUFFERSIZE = 256;

    public static final String REG_HEAD = "REG" ;
    public static final String MESSAGE_HEAD = "MSG" ;

    public static final String HEAD_ICON_ID = "headIconId";
    public static final String NICK_NAME = "nickName";
    public static final String MY_ID = "myId";
    
	private static Map<String, UserInfo> mUserinfoMap = new HashMap<String, UserInfo>();// current

	private static List<String> mUserAccount = new ArrayList<String>();
	
    private IBinder mBinder = new ServiceBinder();// bind service;
    private WifiManager mWifiManager;
    private UserInfo myUserInfo;

    private InetAddress localInetAddress;
    private String localIp;
    private byte[] localIpBytes;
    private byte[] regBuffer = new byte[BUFFERSIZE];
    protected byte[] recvBuffer = new byte[BUFFERSIZE];
    private Context mContext;
    ServiceBroadcastReceiver receiver;
    public static ConnectService getInstence;

    // System Action declare
    public static final String bootCompleted = "android.intent.action.BOOT_COMPLETED";
    public static final String WIFIACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String ETHACTION = "android.intent.action.ETH_STATE";
    public static final String updateMyInformationAction = "com.champion.mipis.updateMyInformation";
    public static final String personHasChangedAction = "com.champion.mipis.personHasChanged";

    private MulticastSocket mMulticastSocket = null;
    public boolean isStopUpdateMe = false;
    
    // bind service.
    @Override
	public IBinder onBind(Intent arg0) {
        if (DEBUG) Log.d(TAG, "onBind services");
        init();
        return mBinder;	
    }
    public class ServiceBinder extends Binder {
        public ConnectService getService() {
            getInstence = ConnectService.this;
            return ConnectService.this;
        }
    }
    
    public static ConnectService getstence() {
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
	    mContext = this;

        getInstence = ConnectService.this;
        init();
        Log.d(TAG, "onStartCommand��������");
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
 	}
	
	public void init(){

        regBroadcastReceiver();
        initNetWork();
	}

    private void initNetWork() {
        mWifiManager =  (WifiManager) getSystemService(Context.WIFI_SERVICE);
        new CheckNetConnectivity().start();
    }

    private boolean getMyinfoFromSharedPreference() {
        String myAccount = PreferencesData.getStringData( this, MY_ID, ""); 

        if (myAccount.equals("")) {
            return false;
        }
        myUserInfo.mAccount = myAccount; 
        return true;
    }

    // 0`2 REG
    // 3456 IPINFO;
    // 7 - 29 ACCOUNT;  23bit
    // 30 - 
    private void initRegCmdBuffer(UserInfo userInfo) {

        for (int i = 0; i < BUFFERSIZE; i++) {
            regBuffer[i] = 0;
        }
        System.arraycopy(REG_HEAD.getBytes(), 0, regBuffer, 0, 3);
        setMyIpinfo();

        // init account
        byte[] account =  userInfo.mAccount.getBytes();
        System.arraycopy(account, 0, regBuffer, 7, account.length);
    }

    private UserInfo updatePerson(byte[] pkg) {
        UserInfo user = new UserInfo();

        byte[] ipByte = new byte[4];
        System.arraycopy(pkg, 3, ipByte, 0, 4);
        user.ipAddress = CommonUtils.intToIp(ByteAndInt.byteArray2Int(ipByte));

        byte[] userAccount = new byte[23];
        System.arraycopy(pkg, 7, userAccount, 0, 23);

        user.mAccount = (new String(userAccount)).trim();

        Log.d(TAG, "put a person: " + user.toString());

        mUserinfoMap.put(user.mAccount, user);
        if (!mUserAccount.contains(user.mAccount)) {
            mUserAccount.add(user.mAccount);
        }
        return user;
    }
 
    private void setMyIpinfo() {
        Log.d(TAG, "setMyIpinfo my ip is = " + localIp);
        System.arraycopy(localIpBytes, 0, regBuffer, 3, 4);
    }
 
    private void regBroadcastReceiver() {

        Log.d(TAG, "regBroadcastReceiver = " + localIp);
        receiver = new ServiceBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WIFIACTION);
        filter.addAction(ETHACTION);
        filter.addAction(updateMyInformationAction);

        mContext.registerReceiver(receiver, filter);
    }

    public void registerMyInfo(){
        startNetworkListener();
        if (getMyinfoFromSharedPreference()) {
            initRegCmdBuffer(myUserInfo);
            joinOrganization();
            //new UpdateMe().start();
        }
    }

    public static final String MULTICAST_IP = "239.9.9.1";
    public static final int PORT = 5760;
    private static final int HEAD_LENGTH = 3;
    
    private void startNetworkListener() {
 
        new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    mMulticastSocket = new MulticastSocket( PORT);

                    mMulticastSocket.joinGroup(InetAddress.getByName(MULTICAST_IP));
                    Log.d(TAG, "Socket started...");
                    while ( null != mMulticastSocket && !mMulticastSocket.isClosed()) {
                        for (int i = 0; i < BUFFERSIZE; i++) {
                            recvBuffer[i] = 0;
                        }
                        DatagramPacket rdp = new DatagramPacket(recvBuffer, recvBuffer.length);
                        mMulticastSocket.receive(rdp);
                        parsePackage(recvBuffer);
                    }
                } catch ( Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }}).start();
    }
 

    protected void parsePackage(byte[] pkg) {
        Log.d(TAG, "receive a message.");

        byte[] headBytes = new byte[3];
        System.arraycopy(pkg, 0, headBytes, 0, HEAD_LENGTH);
        String headStr = new String(headBytes).trim();
        
        if (headStr.equals(REG_HEAD)) {

            UserInfo user = updatePerson(pkg);

            try {
                InetAddress targetIp = InetAddress.getByName(user.ipAddress);
                DatagramPacket dp =
                        new DatagramPacket(regBuffer, BUFFERSIZE, targetIp, PORT);
                mMulticastSocket.send(dp);
            } catch ( Exception e) {
                 e.printStackTrace();
            }
        } else if (headStr.equals(MESSAGE_HEAD)) {
            
        }

     }

    // register myself to the network;
    public void joinOrganization() {
        try {
            if (null != mMulticastSocket && !mMulticastSocket.isClosed()) {
                 
                DatagramPacket dp =
                        new DatagramPacket(regBuffer, BUFFERSIZE,
                                InetAddress.getByName( MULTICAST_IP),  PORT);
                mMulticastSocket.send(dp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Receiver
    private class ServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WIFIACTION) || intent.getAction().equals(ETHACTION)) {
                new CheckNetConnectivity().start();
            } else if (intent.getAction().equals(updateMyInformationAction)) {
                getMyinfoFromSharedPreference();
                registerMyInfo();
            }
        }
    }

    public UserInfo getMyInfomation() {
	    return myUserInfo;
	}

    private class CheckNetConnectivity extends Thread {

        public void run() {
            try {

                if (!mWifiManager.isWifiEnabled()) {
                    return;
                }

                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                        en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            if (inetAddress.isReachable(1000)) {

                                localInetAddress = inetAddress;

                                localIp = inetAddress.getHostAddress().toString();

                                localIpBytes = inetAddress.getAddress();

                                if (DEBUG)
                                    Log.d(TAG, "localInetAddress = " + localInetAddress.toString() + "localIp = "
                                            + localIp + "localIpBytes = " + localIpBytes);

                                setMyIpinfo();
                                registerMyInfo();
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
    }
    
//
//    private class UpdateMe extends Thread {
//
//        @Override
//        public void run() {
//            while (!isStopUpdateMe) {
//                try {
//                    if (DEBUG)
//                        Log.d(TAG, "Update Me..");
//
//                    myUserInfo.updateTime = System.currentTimeMillis();
//
//                    mUserinfoMap.put(myUserInfo.mAccount , myUserInfo);
//
//                    if (!mUserAccount.contains(myUserInfo.mAccount)) {
//                        mUserAccount.add(myUserInfo.mAccount);
//                    }
//
//                    joinOrganization();
//                    sendPersonHasChangedBroadcast();
//                    sleep(20000);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//     }
//
//    private void sendPersonHasChangedBroadcast() {
//
//        if (DEBUG)
//            Log.d(TAG, "sendPersonHasChangedBroadcast...............");
//        Intent intent = new Intent();
//        intent.setAction(personHasChangedAction);
//        sendBroadcast(intent);
//    }
}