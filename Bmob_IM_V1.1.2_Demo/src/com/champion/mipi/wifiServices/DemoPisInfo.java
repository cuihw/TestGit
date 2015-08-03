package com.champion.mipi.wifiServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;



import java.net.Socket;
import java.net.UnknownHostException;
import android.util.Log;

public class DemoPisInfo {

    private static final String TAG = "DemoPisInfo";


    public DemoPisInfo() {

        new Thread(new Runnable(){

            @Override
            public void run() {

                Socket socket = null;
                try {
                    socket = new Socket("www.championlee.com.cn",7308);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "connect to championlee.com");

                new ReceiveThread(socket).start();  
            }}).start();

    }
    
    class ReceiveThread extends Thread{  
        private Socket socket;  
          
        public ReceiveThread(Socket socket) {  
            this.socket = socket;
        }
  
        @Override  
        public void run() {
            while(true){
                try {
                    Log.d(TAG, "ReceiveThread Running !");
                    //socket.set
                    //输入流  
                    InputStream is = socket.getInputStream();
                    
                    int length = is.read();
                    
                    if (length != -1) {
                        byte[] buffer = new byte[length];
                        is.read(buffer);
                        for (int i = 0; i < length; i++) {
                            Log.d(TAG, "buffer" + i + " = " + buffer[i]);
                        }
                        Log.d(TAG, "length = " + length + ", Receive message = " + new String(buffer));
                    }

                    sleep(1000);

                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  
    
}
