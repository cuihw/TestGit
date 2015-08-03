package com.champion.mipi.wifiServices;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.listener.FindListener;

import com.champion.mipi.bean.User;
import com.champion.mipi.util.PreferencesData;
import com.champion.mipi.util.XmlOperation;

public class WifiCommunication {

    private static final String TAG = "WifiCommunication";

    public static final int BUFFERSIZE = 4096;

    public static final String REG_HEAD = "reg";
    public static final String MESSAGE_HEAD = "msg";
    public static final String ACK_REG = "reg_ack";
    public static final String ACK_MESSAGE = "msg_ack";

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_FILE = "file";
    public static final String TYPE_PIC = "picture";
    public static final String TYPE_VOICE = "voice";

    public static final String MY_ID = "myId";

    private Context mContext;

    private WifiManager mWifiManager;

    private InetAddress mInetAddress;

    private String localIp;

    private byte[] localIpBytes;

    private static final String MULTICAST_IP = "239.9.9.1";

    private User myUserInfo = new User();

    private Map<String, User> mUserinfoMap = new HashMap<String, User>();// current

    private List<String> mUserAccount = new ArrayList<String>();

    private MulticastSocket mMulticastSocket = null;

    private static final int PORT = 9760;

    protected byte[] mRecvBuffer = new byte[BUFFERSIZE];

    private static WifiCommunication mInstence;

    private UpdateMe mUpdateMe;

    public static final String personHasChangedAction = "com.champion.mipis.personHasChanged";

    private static final int RECEIVE_MSG = 0;

    private static final int CHECK_USER_ONLIN = 1;
    
    Handler mHandler = new Handler () {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECEIVE_MSG:

                    parsePackage((String)msg.obj);
                    break;
                case CHECK_USER_ONLIN:
                    checkUerOnline();
                    break;
            }
            
        }
    };

    public Map<String, User> getUserinfoMap() {
        return mUserinfoMap;
    }
    
    public static synchronized WifiCommunication getInstence(Context c) {
        if (mInstence == null) {
            mInstence = new WifiCommunication(c);
        }
        return mInstence;
    }

    protected void checkUerOnline() {

        ArrayList<String> removePersonKeys = new ArrayList<String>();
        
        boolean hasChanged = false;

        if (mUserAccount.size() > 0) {

            for (String key : mUserAccount) {
                if (System.currentTimeMillis() - mUserinfoMap.get(key).updateTime > 2 * 60 * 1000) {
                    mUserinfoMap.remove(key);
                    removePersonKeys.add(key);
                    hasChanged  = true;
                }
            }
        }

        if (hasChanged) {
            mUserAccount.removeAll(removePersonKeys);            
        }
        onPersonHasChanged();

        Message msg = mHandler.obtainMessage(CHECK_USER_ONLIN);
        mHandler.sendMessageDelayed(msg, 60 * 1000);
    }

    private WifiCommunication(Context c) {
        mContext = c;

        startWifiNetwork();

        getLocalIp();
    }

    private void updateMyInfo() {
        
        String myAccount = null; 
        if (TextUtils.isEmpty(myUserInfo.getUsername())) {
            myAccount = PreferencesData.getStringData(mContext, MY_ID, "");
        }

        if (!TextUtils.isEmpty(localIp)) {
        }

        if (!TextUtils.isEmpty(myAccount)) {
            myUserInfo.setUsername(myAccount);
        }

        myUserInfo.ipAddress = localIp;

        queryUserInfo(myUserInfo);

        addUser(myUserInfo);
    }

    private User getUser (String username) {
        return mUserinfoMap.get(username);
    }

    private void addUser(User user) {
        if (!mUserAccount.contains(user.getUsername())) {
            mUserAccount.add(user.getUsername());            
        }
        mUserinfoMap.put(myUserInfo.getUsername(), myUserInfo);
    }

    private void startWifiNetwork() {
        regWifiStateChangeReceiver();
        listenWifiMsg();
    }

    private void listenWifiMsg() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    mMulticastSocket = new MulticastSocket(PORT);

                    mMulticastSocket.joinGroup(InetAddress.getByName(MULTICAST_IP));

                    Log.d(TAG, "Socket started...");

                    while (null != mMulticastSocket && !mMulticastSocket.isClosed()) {

                        DatagramPacket rdp = new DatagramPacket(mRecvBuffer, mRecvBuffer.length);
                        mMulticastSocket.receive(rdp);

                        int length = rdp.getLength();
                        
                        Log.d(TAG, "recv length: " + length + ", mRecvBuffer: " + new String (mRecvBuffer));

                        Log.d(TAG, "recv ipaddress: " + rdp.getAddress().getHostName());
                        
                        byte[] recv = new byte[length];

                        System.arraycopy(mRecvBuffer, 0, recv, 0, length);

                        Log.d(TAG, "recv " + new String(recv));
                        // send message to handler
                        Message msg = mHandler.obtainMessage(RECEIVE_MSG);
                        msg.obj = new String(recv);
                        mHandler.sendMessage(msg);
                    }

                } catch (IOException e) {

                    e.printStackTrace();
                }

            }
        }).start();

    }

    private void parsePackage(String recv) {
        String message = recv;
        Log.d(TAG, "parsePackage receive a message." + message);

        String cmd = XmlOperation.getAttriValueByTag(message, "cmd", "name");

        Log.d(TAG, "receive a message. cmd is  " + cmd);

        if (cmd.equals(REG_HEAD)) {
            Log.d(TAG, "receive a message. REG_HEAD ");
            String ipaddress = XmlOperation.getValueByTag(message, "ip");
            String name = XmlOperation.getValueByTag(message, "name");
            String nickNick = XmlOperation.getValueByTag(message, "nickname");

            Log.d(TAG, "nickNick = " + nickNick + ", name = " + name + ", ipaddress = " + ipaddress);

            sendRegAck(ipaddress);

            User newUser = new User();
            newUser.ipAddress = ipaddress;
            newUser.setUsername(name);
            if (!TextUtils.isEmpty(nickNick)) {
                newUser.setNick(nickNick);
            }

            addUser(newUser);
            queryUserInfo(newUser);

        } else if (cmd.equals(ACK_REG)) {
            Log.d(TAG, "receive a message. ACK_REG ");

            String ipaddress = XmlOperation.getValueByTag(message, "ip");
            String name = XmlOperation.getValueByTag(message, "name");
            String nickNick = XmlOperation.getValueByTag(message, "nickname");

            Log.d(TAG, "nickNick = " + nickNick + ", name = " + name + ", ipaddress = " + ipaddress);

            User newUser = new User();
            newUser.ipAddress = ipaddress;
            newUser.setUsername(name);
            if (!TextUtils.isEmpty(nickNick)) {
                newUser.setNick(nickNick);
            }
            addUser(newUser);

            queryUserInfo(newUser);
            
        } else if (cmd.equals(MESSAGE_HEAD)) {
            Log.d(TAG, "receive a message. MESSAGE_HEAD ");

            String msg = XmlOperation.getValueByTag(message, "msg");
            String ip = XmlOperation.getValueByTag(message, "ip");
            String username = XmlOperation.getValueByTag(message, "username");

            

        } else if (cmd.equals(ACK_MESSAGE)) {
            Log.d(TAG, "receive a message. ACK_MESSAGE ");
        }
    }

    

    private synchronized void sendRegAck(final String ipaddress) {
        // send ack
        String ackRegXml = makeReg(false);
        sendBroadCast(ackRegXml.getBytes(), ipaddress);
    }

    private synchronized void sendBroadCast(final byte[] b, final String ipAddress) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (mWifiManager.isWifiEnabled() && mMulticastSocket != null) {

                    try {
                        InetAddress targetIp = InetAddress.getByName(ipAddress);

                        DatagramPacket dp = new DatagramPacket(b, b.length, targetIp, PORT);

                        mMulticastSocket.send(dp);

                        Log.d(TAG, "mMulticastSocket send a package!" + new String(b));

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }}).start();
    }

    // <?xml version="1.0" encoding="UTF-8"?><cmd
    // name="REG_ACK"><nickname/><name>64027</name><ip>10.29.207.14</ip></cmd>
    private String makeReg(boolean isRegister) {

        Map<String, String> tagMap = new HashMap<String, String>();
        
        tagMap.put("ip", myUserInfo.ipAddress);
        tagMap.put("name", myUserInfo.getUsername());
        tagMap.put("nickname", myUserInfo.getNick());

        String regXml;

        if (isRegister) {
            regXml = XmlOperation.buildCmd(REG_HEAD, tagMap);
        } else {
            regXml = XmlOperation.buildCmd(ACK_REG, tagMap);
        }

        return regXml;
    }

    /* <cmd name="MSG">
     *    <msg>hello</msg>
     *    <ip>192.168.1.3</ip>
     *    <username>chris_cui</username>
     * </cmd>
    */
    private String makeMessagebody(String msg, String type) {

        Map<String, String> tagMap = new HashMap<String, String>();

        tagMap.put("type", type);
        tagMap.put("msg", msg);
        tagMap.put("ip", myUserInfo.ipAddress);
        tagMap.put("username", myUserInfo.getUsername());
        String msgXml = XmlOperation.buildCmd(MESSAGE_HEAD, tagMap);

        return msgXml;
    }
    
    public void sendMessage(String username, String msg) {
        User user = getUser(username);

        String msgBody = makeMessagebody(msg, "text");

        Log.d(TAG, "send: " + msgBody);
        sendBroadCast(msgBody.getBytes(), user.ipAddress);
    }

    /* <cmd name="FILE">
     *    <type>picture</type>
     *    <filename>sdcard/mipi/filename.jpg</filename>
     *    <ip>192.168.1.3</ip>
     *    <username>chris_cui</username>
     * </cmd>
     */
    public void sendFile(String username, String filenamepath, String type) {

        Map<String, String> tagMap = new HashMap<String, String>();

        // file
        tagMap.put("type", type);
        tagMap.put("filename", filenamepath);
        tagMap.put("ip", myUserInfo.ipAddress);
        tagMap.put("username", myUserInfo.getUsername());

        String msgXml = XmlOperation.buildCmd(MESSAGE_HEAD, tagMap);

    }

    private void getLocalIp() {
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        }

        if (!mWifiManager.isWifiEnabled()) {
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                            .hasMoreElements();) {
                        NetworkInterface intf = en.nextElement();

                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                                .hasMoreElements();) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress()) {
                                if (inetAddress.isReachable(1000)) {

                                    mInetAddress = inetAddress;

                                    localIp = inetAddress.getHostAddress().toString();

                                    localIpBytes = inetAddress.getAddress();

                                    Log.d(TAG, "localInetAddress = " + mInetAddress.toString() + "localIp = "
                                            + localIp + "localIpBytes = " + localIpBytes);
                                    beginBroadcastMe();

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

    protected void beginBroadcastMe() {

        updateMyInfo();

        if (mUpdateMe == null) {
            mUpdateMe = new UpdateMe();
            mUpdateMe.start();
        }

        // begin check user online.
        mHandler.removeMessages(CHECK_USER_ONLIN);
        Message msg = mHandler.obtainMessage(CHECK_USER_ONLIN);
        mHandler.sendMessageDelayed(msg, 10 * 1000);
    }

    private void regWifiStateChangeReceiver() {

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
            if (intent.getAction().equals(WIFIACTION) || intent.getAction().equals(ETHACTION)) {
                getLocalIp();
            }
        }
    }

    private void queryUserInfo(User user) {

        BmobUserManager userManager = BmobUserManager.getInstance(mContext);
        userManager.queryUser(user.getUsername(), new FindListener<User>() {

            @Override
            public void onError(int arg0, String arg1) {
                Log.d(TAG, "queryUser onError:" + arg1);
            }

            @Override
            public void onSuccess(List<User> arg0) {
                if (arg0 != null && arg0.size() > 0) {
                    User user = arg0.get(0);

                    User userCached = getUser(user.getUsername());

                    if (userCached != null && !TextUtils.isEmpty(userCached.getUsername())) {

                        user.ipAddress = userCached.ipAddress;
                        user.updateTime = System.currentTimeMillis();

                        addUser(user);

                        if (myUserInfo.getUsername().equals(user.getUsername())) {
                            myUserInfo = user;
                        }

                    } else {
                        Log.d(TAG, "onSuccess cached map not found this user.");
                    }

                } else {
                    Log.d(TAG, "onSuccess not found this user.");
                }
            }
        });
    }

    public static boolean isStopUpdateMe = false;

    private class UpdateMe extends Thread {

        @Override
        public void run() {
            synchronized (UpdateMe.this) {
                while (!isStopUpdateMe) {
                    try {
                        Log.d(TAG, "Update Me..");

                        myUserInfo.updateTime = System.currentTimeMillis();

                        if (!TextUtils.isEmpty(myUserInfo.getUsername())) {

                            addUser(myUserInfo);

                            queryUserInfo(myUserInfo);

                            if (myUserInfo.getAvatar() != null) {
                                queryUserInfo(myUserInfo);
                            }

                            broadcastMe();
                        }

                        sleep(10000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            super.run();
        }
    }

    public void broadcastMe() {
        String regXml = makeReg(true);

        Log.d(TAG, "broadcastMe regXml = " + regXml);
        sendBroadCast(regXml.getBytes(), MULTICAST_IP);
    }

    private void onPersonHasChanged() {
        Log.d(TAG, "sendPersonHasChangedBroadcast...............");
        Intent intent = new Intent();
        intent.setAction(personHasChangedAction);
        mContext.sendBroadcast(intent);
    }


}
