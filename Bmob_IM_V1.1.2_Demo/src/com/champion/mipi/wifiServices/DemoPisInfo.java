package com.champion.mipi.wifiServices;


import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.champion.mipi.ui.EmergencyActivity;

public class DemoPisInfo {

    private static final String TAG = "DemoPisInfo";

    static Socket mSocket = null;

    static ReceiveThread mReceiveThread;

    Object mLock = new Object();

    static DemoPisInfo mInstence;

    Context mContext;
    
    public static synchronized DemoPisInfo getInstence(Context c) {
        
        if (mInstence == null) {
            mInstence = new DemoPisInfo(c);
        }
        return mInstence;
    }

    private DemoPisInfo(Context c) {
        mContext = c;
        connectToServer ();
    }

    private static int testCount = 5;

    private void connectToServer () {

        new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (mLock) {
                    Log.d(TAG, "start test");
                    Log.d(TAG, "testCount = " + testCount);
                    if (testCount == 0) {
                        startEmergencyActivity("test!! 紧急情况，地铁15号线由于突发状况暂停运行，请大家换乘其他交通工具！");
                    }

                    if (testCount >= 0) {
                        testCount--;
                    }
                    Log.d(TAG, "Try to connect championlee.com");
                    try {
                        if (mSocket == null) {
                            mSocket = new Socket("www.championlee.com.cn", 7308);
                        }
                    } catch (Exception e) {
                        mSocket = null;
                        e.printStackTrace();
                    }

                    if (mReceiveThread == null) {
                        mReceiveThread = new ReceiveThread(mSocket);
                        mReceiveThread.start();
                    }
                }
            }
        }).start();
    }


    class ReceiveThread extends Thread {
        private Socket socket;

        public ReceiveThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Log.d(TAG, "ReceiveThread Running !");
                    // socket.set
                    if (socket == null) {
                        Log.d(TAG, "socket is null !");
                        sleep(3000);
                        connectToServer ();
                        mReceiveThread = null;
                        return;
                    }
                    InputStream is = socket.getInputStream();

                    int length = is.read();
                    Log.d(TAG, "length = " + length);
                    if (length != -1) {
                        byte[] buffer = new byte[length];
                        is.read(buffer);
                        String message = new String(buffer);
                        Log.d(TAG, "recv message：" + message + ", length = " + length);
                        String strBuf = bytes2hex(buffer) ;
                        Log.d(TAG, "buffer：" + strBuf);
                        startEmergencyActivity(message);
                    }

                    sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

    }

    private void startEmergencyActivity(String message) {
        Log.d(TAG, "start startEmergencyActivity");

        if (!TextUtils.isEmpty(message)) {

            Intent intent = new Intent(mContext, EmergencyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("message", message);
            mContext.startActivity(intent);
        } else {
            Log.d(TAG, "message is null.");
        }
    }

    public static String bytes2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        for (byte b : bytes) {
            tmp = Integer.toHexString(0xFF & b);
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }
            tmp = "0x" + tmp + " ";
            sb.append(tmp);
        }
        return sb.toString();
    }
}
