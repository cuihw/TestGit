package com.champion.mipi.wifiServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


import java.io.Reader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.CharBuffer;

import org.jivesoftware.smack.util.Base64.InputStream;

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
                // new SendThread(socket).start();  
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
                    if (socket == null) {
                        Log.d(TAG, "socket is null !");
                        return;
                    }
                    java.io.InputStream is = socket.getInputStream();  
                    BufferedReader br=new BufferedReader(new InputStreamReader(is));  
                    if (is != null && br != null) {
                        String readStr=null;  
                        while(!((readStr=br.readLine())==null)){  
                            Log.d(TAG, "recv message："+readStr);  
                        }
                    } else {
                        Log.d(TAG, "InputStream is null or BufferedReader is null");
                        sleep(2000);
                        return;
                    }
                    
//                  Reader reader = new InputStreamReader(socket.getInputStream());
//                    CharBuffer charBuffer = CharBuffer.allocate(8192);  
//                    int index = -1;
//                    while((index=reader.read(charBuffer))!=-1){  
//                        charBuffer.flip();
//                        System.out.println("client:"+charBuffer.toString());
//                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
      
    
    class SendThread extends Thread{  
        private Socket socket;  
        public SendThread(Socket socket) {  
            this.socket = socket;  
        }  

//        @Override  
//        public void run() {  
//            while(true){  
//                try {
//                    String send = getSend();              
//                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));  
//                    pw.write(send);  
//                    pw.flush();  
//                } catch (Exception e) {
//                    e.printStackTrace();  
//                }  
//            }  
//        }  
//
//        public String getSend() throws InterruptedException{
//            Thread.sleep(1000);
//            return "<SOAP-ENV:Envelope>"+System.currentTimeMillis()+"</SOAP-ENV:Envelope>";  
//        }

    }  
    
    
}
