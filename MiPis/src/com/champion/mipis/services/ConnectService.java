package com.champion.mipis.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.champion.mipis.R;
import com.champion.mipis.util.ByteAndInt;
import com.champion.mipis.util.CacheToFile;
import com.champion.mipis.util.Constant;
import com.champion.mipis.util.FileName;
import com.champion.mipis.util.FileState;
import com.champion.mipis.util.LocalMessage;
import com.champion.mipis.util.Person;
import com.champion.mipis.util.PreferencesData;

public class ConnectService extends Service {

    private static final boolean DEBUG = false;

    public static final String TAG = "ConnectService";

    private IBinder mBinder = new ServiceBinder();// bind service;

    private static Map<Integer, Person> childrenMap = new HashMap<Integer, Person>();// current
                                                                                     // online user

    private static ArrayList<Integer> personKeys = new ArrayList<Integer>();// current online user
                                                                            // id

    // message container for all the users
    private static Map<Integer, List<LocalMessage>> msgContainer = new HashMap<Integer, List<LocalMessage>>();

    private WifiManager wifiManager = null;

    private ServiceBroadcastReceiver receiver = null;

    public InetAddress localInetAddress = null;

    private String localIp = null;

    private byte[] localIpBytes = null;

    // register self to networks.
    private byte[] regBuffer = new byte[Constant.bufferSize];

    private byte[] msgSendBuffer = new byte[Constant.bufferSize];// buffer for send and recv

    private byte[] fileSendBuffer = new byte[Constant.bufferSize];

    private byte[] talkCmdBuffer = new byte[Constant.bufferSize];

    private static Person mySelf = null;

    private CommunicationBridge comBridge = null;// communication and protrol parse moudle.

    // send a heart beat every 20 second.
    boolean isStopUpdateMe = false;

    FileState mFileState = new FileState();

    XmppClient mXmppClient;

    private class UpdateMe extends Thread {

        @Override
        public void run() {
            while (!isStopUpdateMe) {
                try {
                    if (DEBUG)
                        Log.d(TAG, "Update Me..");

                    mySelf.timeStamp = System.currentTimeMillis();

                    if (!personKeys.contains(mySelf.personId)) {
                        personKeys.add(0, mySelf.personId);
                    }
                    childrenMap.put(mySelf.personId, mySelf);

                    comBridge.joinOrganization();
                    sendPersonHasChangedBroadcast();
                    sleep(20000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {

        if (DEBUG)
            Log.d(TAG, "onBind services");
        return mBinder;
    }


    // bind service.
    public class ServiceBinder extends Binder {
        public ConnectService getService() {
            return ConnectService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG)
            Log.d(TAG, "onCreate() Services");
    }

    public void sendMsg(final int personId, final String msg) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                comBridge.sendMsg(personId, msg);
            }
        }).start();

    }

    public void sendFile(final int personId, Uri filerui) {

        if (filerui != null) {


            final File file = new File(filerui.getPath());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    comBridge.sendfile(personId, file);
                }
            }).start();
        }

    }

    public Map<Integer, Person> getAllPerson() {
        return childrenMap;
    }

    public ArrayList<Integer> getPersonKeys() {
        return personKeys;
    }

    public Person getMe() {
        return mySelf;
    }

    @Override
    public void onStart(Intent intent, int startId) {

        if (DEBUG)
            Log.d(TAG, "onStart Services");
        initCmdBuffer();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        new CheckNetConnectivity().start();

        comBridge = new CommunicationBridge();

        comBridge.start();

        regBroadcastReceiver();

        getMyInfomation();

        new CheckUserOnline().start();

        sendPersonHasChangedBroadcast();

        new UpdateMe().start();


    }

    private class CheckUserOnline extends Thread {
        @Override
        public void run() {
            super.run();
            boolean hasChanged = false;
            while (!isStopUpdateMe) {

                ArrayList<Integer> removePersonKeys = new ArrayList<Integer>();

                if (childrenMap.size() > 0) {

                    for (Integer key : personKeys) {
                        if (System.currentTimeMillis() - childrenMap.get(key).timeStamp > 30000) {
                            childrenMap.remove(key);
                            removePersonKeys.add(key);
                            hasChanged = true;
                        }
                    }
                }

                if (hasChanged)
                    personKeys.removeAll(removePersonKeys);

                sendPersonHasChangedBroadcast();
                try {
                    sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void regBroadcastReceiver() {

        receiver = new ServiceBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.WIFIACTION);
        filter.addAction(Constant.ETHACTION);
        filter.addAction(Constant.updateMyInformationAction);
        filter.addAction(Constant.refuseReceiveFileAction);
        filter.addAction(Constant.imAliveNow);
        registerReceiver(receiver, filter);
    }

    public synchronized Map<Integer, List<LocalMessage>> getMsgContainer() {
        return msgContainer;
    }

    private void initCmdBuffer() {

        if (DEBUG)
            Log.d(TAG, "initCmdBuffer");
        for (int i = 0; i < Constant.bufferSize; i++) {
            regBuffer[i] = 0;
        }

        System.arraycopy(Constant.pkgHead, 0, regBuffer, 0, 3);
        regBuffer[3] = Constant.CMD80;
        regBuffer[4] = Constant.CMD_TYPE1;
        regBuffer[5] = Constant.OPR_CMD1;

        for (int i = 0; i < Constant.bufferSize; i++)
            msgSendBuffer[i] = 0;
        System.arraycopy(Constant.pkgHead, 0, msgSendBuffer, 0, 3);
        msgSendBuffer[3] = Constant.CMD81;
        msgSendBuffer[4] = Constant.CMD_TYPE1;
        msgSendBuffer[5] = Constant.OPR_CMD1;

        for (int i = 0; i < Constant.bufferSize; i++)
            fileSendBuffer[i] = 0;
        System.arraycopy(Constant.pkgHead, 0, fileSendBuffer, 0, 3);
        fileSendBuffer[3] = Constant.CMD82;
        fileSendBuffer[4] = Constant.CMD_TYPE1;
        fileSendBuffer[5] = Constant.OPR_CMD1;

        for (int i = 0; i < Constant.bufferSize; i++)
            talkCmdBuffer[i] = 0;
        System.arraycopy(Constant.pkgHead, 0, talkCmdBuffer, 0, 3);
        talkCmdBuffer[3] = Constant.CMD83;
        talkCmdBuffer[4] = Constant.CMD_TYPE1;
        talkCmdBuffer[5] = Constant.OPR_CMD1;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        flags = START_STICKY;
        if (DEBUG)
            Log.d(TAG, "onStartCommand...............");
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendPersonHasChangedBroadcast() {

        if (DEBUG)
            Log.d(TAG, "sendPersonHasChangedBroadcast...............");
        Intent intent = new Intent();
        intent.setAction(Constant.personHasChangedAction);
        sendBroadcast(intent);
    }

    private void getMyInfomation() {
        if (DEBUG)
            Log.d(TAG, "getMyInfomation...............");

        mySelf = getInfoFromPreferences(mySelf);

        if (DEBUG)
            Log.d(TAG, "ipAddress = " + localIp);

        // update the register cmd data.
        System.arraycopy(ByteAndInt.int2ByteArray(mySelf.personId), 0, regBuffer, 6, 4);
        System.arraycopy(ByteAndInt.int2ByteArray(mySelf.personHeadIconId), 0, regBuffer, 10, 4);

        for (int i = 14; i < 44; i++)
            regBuffer[i] = 0; // clear nickname buffer

        byte[] nickeNameBytes = mySelf.personNickeName.getBytes();
        System.arraycopy(nickeNameBytes, 0, regBuffer, 14, nickeNameBytes.length);

        // update the talk cmd
        System.arraycopy(ByteAndInt.int2ByteArray(mySelf.personId), 0, talkCmdBuffer, 6, 4);
        System.arraycopy(ByteAndInt.int2ByteArray(mySelf.personHeadIconId), 0, talkCmdBuffer, 10, 4);
        for (int i = 14; i < 44; i++)
            // clear nickname buffer
            talkCmdBuffer[i] = 0;
        System.arraycopy(nickeNameBytes, 0, talkCmdBuffer, 14, nickeNameBytes.length);

    }

    private Person getInfoFromPreferences(Person person) {

        if (null == person) {
            person = new Person();
            person.personId = 0;
        }

        if (person.personId == 0) {

            // id;
            int personId = PreferencesData.getIntData(this, "myId", 0);
            if (0 == personId) {
                // write the data to perferences.
                personId = Constant.getMyId();
                PreferencesData.setIntData(this, "myId", personId);

                // first open app; register a account.
                registerAccout(personId + "");
            }

            // nickname;
            String nickName = PreferencesData.getStringData(this, Constant.NICK_NAME, "");
            if (nickName.equals("")) {

                person.personNickeName = "游客" + personId;
                PreferencesData.setStringData(this, Constant.NICK_NAME, nickName);
            }

            // head icon
            int iconId = PreferencesData.getIntData(this, Constant.HEAD_ICON_ID, 0);
            if (iconId == 0) {
                int iconIndex = personId % (Constant.HEADICON_IDS.length);
                PreferencesData.setIntData(this, Constant.HEAD_ICON_ID, Constant.HEADICON_IDS[iconIndex]);
                iconId = Constant.HEADICON_IDS[iconIndex];
            }

            person.personId = personId;
            person.personNickeName = nickName;
            person.personHeadIconId = iconId;
        }

        person.ipAddress = localIp;
        return person;
    }

    private void registerAccout(final String personId) {
        new Thread(new Runnable() {

            @Override
            public void run() {

                mXmppClient = new XmppClient();
                mXmppClient.connect();
                if (mXmppClient.createAccount(personId, "123456789")) {
                    mXmppClient.login(personId, "123456789");
                }
            }
        }).start();
    }



    // check network connectivity, get local ip address
    private class CheckNetConnectivity extends Thread {
        public void run() {
            try {

                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }

                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
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

                                System.arraycopy(localIpBytes, 0, regBuffer, 44, 4);
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
    };

    // Receiver
    private class ServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.WIFIACTION) || intent.getAction().equals(Constant.ETHACTION)) {
                new CheckNetConnectivity().start();
            } else if (intent.getAction().equals(Constant.updateMyInformationAction)) {
                getMyInfomation();
                comBridge.joinOrganization();
            } else if (intent.getAction().equals(Constant.imAliveNow)) {

            }
        }
    }

    // ========================protocol and communication.
    private ServerSocket sFileSocket = null;

    private ServerSocket sAudioSocket = null;

    private class CommunicationBridge extends Thread {
        private MulticastSocket multicastSocket = null;
        private byte[] recvBuffer = new byte[Constant.bufferSize];
        private int fileSenderUid = 0;
        private boolean isBusyNow = false;
        private String fileSavePath = null;
        private boolean isStopTalk = false;
        private ArrayList<FileName> tempFiles = null;
        private int tempUid = 0;
        private ArrayList<FileState> receivedFileNames = new ArrayList<FileState>();
        private ArrayList<FileState> beSendFileNames = new ArrayList<FileState>();

        private FileHandler fileHandler = null;
        private AudioHandler audioHandler = null;

        public CommunicationBridge() {
            fileHandler = new FileHandler();
            fileHandler.start();

            audioHandler = new AudioHandler();
            audioHandler.start();
        }

        // 打开组播端口，准备组播通讯
        @Override
        public void run() {
            super.run();

            try {
                multicastSocket = new MulticastSocket(Constant.PORT);
                multicastSocket.joinGroup(InetAddress.getByName(Constant.MULTICAST_IP));
                System.out.println("Socket started...");
                while (!multicastSocket.isClosed() && null != multicastSocket) {
                    for (int i = 0; i < Constant.bufferSize; i++) {
                        recvBuffer[i] = 0;
                    }
                    DatagramPacket rdp = new DatagramPacket(recvBuffer, recvBuffer.length);
                    multicastSocket.receive(rdp);
                    parsePackage(recvBuffer);
                }
            } catch (Exception e) {
                Log.e(TAG, "open the port is error e = " + e.toString());
                try {
                    if (null != multicastSocket && !multicastSocket.isClosed()) {
                        multicastSocket.leaveGroup(InetAddress.getByName(Constant.MULTICAST_IP));
                        multicastSocket.close();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }

        }

        // 解析接收到的数据包
        private void parsePackage(byte[] pkg) {

            if (DEBUG)
                Log.d(TAG, "parsePackage .. ");

            int CMD = pkg[3];// 命令字
            int cmdType = pkg[4];// 命令类型
            int oprCmd = pkg[5];// 操作命令

            // 获得用户ID号
            byte[] uId = new byte[4];
            System.arraycopy(pkg, 6, uId, 0, 4);
            int userId = ByteAndInt.byteArray2Int(uId);

            switch (CMD) {
                case Constant.CMD80:

                    if (DEBUG)
                        Log.d(TAG, "parsePackage .. Constant.CMD80");
                    switch (cmdType) {
                        case Constant.CMD_TYPE1:
                            // 如果该信息不是自己发出则给对方发送回应包,并把对方加入用户列表
                            if (userId != mySelf.personId) {

                                updatePerson(userId, pkg);
                                // 发送应答包

                                byte[] ipBytes = new byte[4];// 获得请求方的ip地址
                                System.arraycopy(pkg, 44, ipBytes, 0, 4);
                                try {
                                    InetAddress targetIp = InetAddress.getByAddress(ipBytes);

                                    if (DEBUG)
                                        Log.d(TAG, "parsePackage() ipBytes = " + ipBytes.toString() + ", targetIp = "
                                                + targetIp.toString());

                                    regBuffer[4] = Constant.CMD_TYPE2;// 把自己的注册信息修改成应答信息标志，把自己的信息发送给请求方
                                    DatagramPacket dp =
                                            new DatagramPacket(regBuffer, Constant.bufferSize, targetIp, Constant.PORT);
                                    multicastSocket.send(dp);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case Constant.CMD_TYPE2:
                            updatePerson(userId, pkg);
                            break;
                        case Constant.CMD_TYPE3:
                            childrenMap.remove(userId);
                            personKeys.remove(Integer.valueOf(userId));
                            sendPersonHasChangedBroadcast();
                            break;
                    }
                    break;
                case Constant.CMD81:// 收到信息
                    if (DEBUG)
                        Log.d(TAG, "parsePackage .. receive a text message.");

                    switch (cmdType) {
                        case Constant.CMD_TYPE1:
                            List<LocalMessage> messages = null;

                            synchronized (msgContainer) {

                                if (msgContainer.containsKey(userId)) {
                                    messages = msgContainer.get(userId);
                                } else {
                                    messages = new ArrayList<LocalMessage>();
                                }

                                byte[] msgBytes = new byte[Constant.msgLength];
                                System.arraycopy(pkg, 10, msgBytes, 0, Constant.msgLength);
                                String msgStr = new String(msgBytes).trim();

                                // save the message to the container
                                String msg = msgStr;
                                long receivedMills = System.currentTimeMillis();
                                int personID = userId;
                                int fromTo = LocalMessage.FROM;

                                LocalMessage message = new LocalMessage(msg, receivedMills, personID, fromTo);
                                message.type = LocalMessage.TEXT;

                                if (DEBUG)
                                    Log.d(TAG, "put message to the messsage array. message = " + msg);
                                messages.add(message);

                                for (int i = 0; i < messages.size(); i++) {
                                    if (DEBUG)
                                        Log.d(TAG, "message index " + i + " : " + messages.get(i));
                                }

                                msgContainer.put(userId, messages);

                            }
                            broadcastNewMessage(userId, messages.size());
                            break;
                        case Constant.CMD_TYPE2:
                            break;
                    }
                    break;
                case Constant.CMD82:
                    if (DEBUG)
                        Log.d(TAG, "parsePackage .. Constant.CMD82");
                    switch (cmdType) {
                        case Constant.CMD_TYPE1:
                            switch (oprCmd) {
                                case Constant.OPR_CMD1:

                                    if (!isBusyNow) {
                                        // isBusyNow = true;
                                        // save the sent user id.
                                        fileSenderUid = userId;

                                        Person person = childrenMap.get(Integer.valueOf(userId));
                                        Intent intent = new Intent();
                                        intent.putExtra("person", person);
                                        intent.setAction(Constant.receivedSendFileRequestAction);
                                        sendBroadcast(intent);
                                    } else {
                                        // send busy on the transfer file.
                                        Person person = childrenMap.get(Integer.valueOf(userId));
                                        fileSendBuffer[4] = Constant.CMD_TYPE2;
                                        fileSendBuffer[5] = Constant.OPR_CMD4;
                                        byte[] meIdBytes = ByteAndInt.int2ByteArray(mySelf.personId);
                                        System.arraycopy(meIdBytes, 0, fileSendBuffer, 6, 4);
                                        try {
                                            DatagramPacket dp =
                                                    new DatagramPacket(fileSendBuffer, Constant.bufferSize,
                                                            InetAddress.getByName(person.ipAddress), Constant.PORT);
                                            multicastSocket.send(dp);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                                case Constant.OPR_CMD5:// get the file name by other user.
                                    byte[] fileNameBytes = new byte[Constant.fileNameLength];
                                    byte[] fileSizeByte = new byte[8];
                                    System.arraycopy(pkg, 10, fileNameBytes, 0, Constant.fileNameLength);
                                    System.arraycopy(pkg, 100, fileSizeByte, 0, 8);
                                    FileState fs = new FileState();
                                    fs.fileName = new String(fileNameBytes).trim();
                                    fs.fileSize = Long.valueOf(ByteAndInt.byteArrayToLong(fileSizeByte));
                                    receivedFileNames.add(fs);
                                    break;
                            }
                            break;
                        case Constant.CMD_TYPE2:
                            switch (oprCmd) {
                                case Constant.OPR_CMD2:// accept recivie file
                                    fileHandler.startSendFile();
                                    System.out.println("Start send file to remote user ...");
                                    break;
                                case Constant.OPR_CMD3:// reject
                                    Intent intent = new Intent();
                                    intent.setAction(Constant.remoteUserRefuseReceiveFileAction);
                                    sendBroadcast(intent);
                                    System.out.println("Remote user refuse to receive file ...");
                                    break;
                                case Constant.OPR_CMD4:// on busy
                                    System.out.println("Remote user is busy now ...");
                                    break;
                            }
                            break;
                    }
                    break;
                case Constant.CMD83:// about voice
                    switch (cmdType) {
                        case Constant.CMD_TYPE1:
                            switch (oprCmd) {
                                case Constant.OPR_CMD1://receive a talk request
                                    System.out.println("Received a talk request ... ");
                                    isStopTalk = false;
                                    Person person = childrenMap.get(Integer.valueOf(userId));
                                    Intent intent = new Intent();
                                    intent.putExtra("person", person);
                                    intent.setAction(Constant.receivedTalkRequestAction);
                                    sendBroadcast(intent);
                                    break;
                                case Constant.OPR_CMD2:
                                    // receive close cmd
                                    System.out.println("Received remote user stop talk cmd ... ");
                                    isStopTalk = true;
                                    Intent i = new Intent();
                                    i.setAction(Constant.remoteUserClosedTalkAction);
                                    sendBroadcast(i);
                                    break;
                            }
                            break;
                        case Constant.CMD_TYPE2:
                            switch (oprCmd) {
                                case Constant.OPR_CMD1:
                                    // start talk.
                                    if (!isStopTalk) {
                                        System.out.println("Begin to talk with remote user ... ");
                                        Person person = childrenMap.get(Integer.valueOf(userId));
                                        audioHandler.audioSend(person);
                                    }
                                    break;
                            }
                            break;
                    }
                    break;
            }
        }


        // update user.
        private void updatePerson(int userId, byte[] pkg) {
            Person person = new Person();
            getPerson(pkg, person);
            if (DEBUG)
                Log.d(TAG, "get person's ip = " + person.ipAddress);

            if (person.ipAddress.equals("0.0.0.0")) {
                return;
            }

            childrenMap.put(userId, person);
            if (!personKeys.contains(Integer.valueOf(userId))) {
                personKeys.add(Integer.valueOf(userId));
            }

            sendPersonHasChangedBroadcast();
        }

        // 关闭Socket连接
        private void release() {
            try {
                regBuffer[4] = Constant.CMD_TYPE3;// 把命令类型修改成注消标志，并广播发送，从所有用户中退出
                DatagramPacket dp =
                        new DatagramPacket(regBuffer, Constant.bufferSize,
                                InetAddress.getByName(Constant.MULTICAST_IP), Constant.PORT);
                multicastSocket.send(dp);
                System.out.println("Send logout cmd ...");

                multicastSocket.leaveGroup(InetAddress.getByName(Constant.MULTICAST_IP));
                multicastSocket.close();

                System.out.println("Socket has closed ...");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fileHandler.release();
                audioHandler.release();
            }
        }

        // 分析数据包并获取一个用户信息
        private void getPerson(byte[] pkg, Person person) {

            byte[] personIdBytes = new byte[4];
            byte[] iconIdBytes = new byte[4];
            byte[] nickeNameBytes = new byte[30];
            byte[] personIpBytes = new byte[4];

            System.arraycopy(pkg, 6, personIdBytes, 0, 4);
            System.arraycopy(pkg, 10, iconIdBytes, 0, 4);
            System.arraycopy(pkg, 14, nickeNameBytes, 0, 30);
            System.arraycopy(pkg, 44, personIpBytes, 0, 4);

            person.personId = ByteAndInt.byteArray2Int(personIdBytes);
            person.personHeadIconId = ByteAndInt.byteArray2Int(iconIdBytes);
            person.personNickeName = (new String(nickeNameBytes)).trim();

            if (DEBUG)
                Log.d(TAG, "getPerson() personIpBytes  = " + personIpBytes);
            person.ipAddress = Constant.intToIp(ByteAndInt.byteArray2Int(personIpBytes));
            if (DEBUG)
                Log.d(TAG, "getPerson() ipAddress  = " + personIpBytes);

            person.timeStamp = System.currentTimeMillis();
        }

        // 注册自己到网络中
        public void joinOrganization() {
            try {
                if (null != multicastSocket && !multicastSocket.isClosed()) {
                    regBuffer[4] = Constant.CMD_TYPE1;// 恢复成注册请求标志，向网络中注册自己
                    DatagramPacket dp =
                            new DatagramPacket(regBuffer, Constant.bufferSize,
                                    InetAddress.getByName(Constant.MULTICAST_IP), Constant.PORT);
                    multicastSocket.send(dp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 发送信息
        public void sendMsg(int personId, String msg) {
            try {
                Person psn = childrenMap.get(personId);

                if (null != psn) {

                    // put message to list
                    long sendMills = System.currentTimeMillis();
                    int personID = personId;
                    int fromTo = LocalMessage.TO;

                    LocalMessage message = new LocalMessage(msg, sendMills, personID, fromTo);
                    message.type = LocalMessage.TEXT;
                    List<LocalMessage> messages = null;
                    synchronized (msgContainer) {

                        if (msgContainer.containsKey(personId)) {
                            messages = msgContainer.get(personId);
                        } else {
                            messages = new ArrayList<LocalMessage>();
                        }

                        if (DEBUG)
                            Log.d(TAG, "put message to list which will sending.");
                        messages.add(message);
                        msgContainer.put(personId, messages);
                    }

                    System.arraycopy(ByteAndInt.int2ByteArray(mySelf.personId), 0, msgSendBuffer, 6, 4);
                    int msgLength = Constant.msgLength + 10;
                    for (int i = 10; i < msgLength; i++) {
                        msgSendBuffer[i] = 0;
                    }
                    byte[] msgBytes = msg.getBytes();

                    if (DEBUG)
                        Log.d(TAG, "sendMsg, person = " + psn.toString());

                    System.arraycopy(msgBytes, 0, msgSendBuffer, 10, msgBytes.length);
                    DatagramPacket dp =
                            new DatagramPacket(msgSendBuffer, Constant.bufferSize,
                                    InetAddress.getByName(psn.ipAddress), Constant.PORT);

                    if (DEBUG)
                        Log.d(TAG, "send DatagramPacket, psn.ipAddress = " + psn.ipAddress);


                    //
                    // while(!multicastSocket.isClosed()) {
                    // Thread.interrupted();
                    // sleep(500);
                    // this.start();
                    // sleep(500);
                    // }

                    multicastSocket.send(dp);

                    broadcastNewMessage(personID, messages.size());

                } else {
                    if (DEBUG)
                        Log.d(TAG, "person is null!");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void broadcastNewMessage(int personID, int size) {

            Intent intent = new Intent();
            intent.setAction(Constant.hasMsgUpdatedAction);
            intent.putExtra("userId", personID);
            intent.putExtra("msgCount", size);
            sendBroadcast(intent);
        }

        // send file .....
        public void sendfile(int personid, File file) {
            if (personid > 0 && file != null && !TextUtils.isEmpty(file.getName())) {
                String filename = file.getName();
                if (DEBUG)
                    Log.d(TAG, "send filename = " + filename);
                final byte[] sendFileCmd = new byte[Constant.bufferSize];

                Person person = childrenMap.get(personid);

                final String userIp = person.ipAddress;

                sendFileCmd[3] = Constant.CMD82;

                sendFileCmd[4] = Constant.CMD_SEND_FILE;

                sendFileCmd[5] = Constant.OPR_SEND_FILE;

                byte[] meIdBytes = ByteAndInt.int2ByteArray(mySelf.personId);

                System.arraycopy(meIdBytes, 0, sendFileCmd, 6, 4); // 10 BYTE

                Socket socket = null;

                OutputStream output = null;

                InputStream input = null;


                long sendMills = System.currentTimeMillis();
                int fromTo = LocalMessage.TO;
                LocalMessage message = new LocalMessage(file.getAbsolutePath(), sendMills, personid, fromTo);

                // TODO: message type
                message.type = LocalMessage.PIC;

                List<LocalMessage> messages = null;
                synchronized (msgContainer) {

                    if (msgContainer.containsKey(personid)) {
                        messages = msgContainer.get(personid);
                    } else {
                        messages = new ArrayList<LocalMessage>();
                    }

                    if (DEBUG)
                        Log.d(TAG, "put message to list which will sending.");
                    messages.add(message);
                    msgContainer.put(personid, messages);

                    broadcastNewMessage(personid, messages.size());
                }

                try {
                    socket = new Socket(userIp, Constant.PORT);

                    // put the filename
                    byte[] fileNameBytes = filename.getBytes();
                    int fileNameLength = Constant.fileNameLength + 10;// filename lenth is 90 byte
                                                                      // length.
                    for (int i = 10; i < fileNameLength; i++) {
                        sendFileCmd[i] = 0;
                    }
                    System.arraycopy(fileNameBytes, 0, sendFileCmd, 10, fileNameBytes.length);

                    // put the file size.
                    System.arraycopy(ByteAndInt.longToByteArray(file.length()), 0, sendFileCmd, 100, 8);
                    output = socket.getOutputStream();
                    output.write(sendFileCmd);
                    output.flush();

                    // waiting the server process.
                    sleep(1000);
                    byte[] readBuffer = new byte[Constant.readBufferSize];

                    // create a input stream to reading the file.
                    input = new FileInputStream(file);
                    int readSize = 0;
                    int length = 0;
                    long count = 0;

                    mFileState.fileName = file.getName();
                    mFileState.currentSize = 0;
                    mFileState.fileSize = file.length();
                    mFileState.percent = 0;

                    while (-1 != (readSize = input.read(readBuffer))) {
                        output.write(readBuffer, 0, readSize);// 鎶婂唴瀹瑰啓鍒拌緭鍑烘祦涓彂閫佺粰瀵规柟
                        output.flush();
                        length += readSize;
                        count++;
                        if (count % 10 == 0) {
                            mFileState.currentSize = length;
                            mFileState.percent =
                                    ((int) ((Float.valueOf(length) / Float.valueOf(mFileState.fileSize)) * 100));
                            Intent intent = new Intent();
                            intent.setAction(Constant.fileSendStateUpdateAction);
                            sendBroadcast(intent);
                        }
                    }

                    mFileState.currentSize = length;
                    if (length >= mFileState.fileSize) {
                        mFileState.percent = 100;
                    }
                    Intent intent = new Intent();
                    intent.setAction(Constant.fileSendStateUpdateAction);
                    sendBroadcast(intent);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.putExtra("msg", e.getMessage());
                    intent.setAction(Constant.dataSendErrorAction);
                    sendBroadcast(intent);
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != output)
                            output.close();
                        if (null != input)
                            input.close();
                        if (!socket.isClosed())
                            socket.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        // =========================TCP文件传输模块==================================================================
        // 基于Tcp传输的文件收发模块
        private class FileHandler extends Thread {

            public FileHandler() {}

            @Override
            public void run() {
                super.run();
                try {
                    if (sFileSocket == null) {
                        sFileSocket = new ServerSocket(Constant.PORT);
                    }

                    System.out.println("File Handler socket started ...");
                    while (!sFileSocket.isClosed() && null != sFileSocket) {
                        Socket socket = sFileSocket.accept();
                        socket.setSoTimeout(5000);
                        new SaveFileToDisk(socket).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 保存接收到的数据
            private class SaveFileToDisk extends Thread {
                private Socket socket = null;

                public SaveFileToDisk(Socket socket) {
                    this.socket = socket;
                }

                @Override
                public void run() {
                    super.run();
                    OutputStream output = null;
                    InputStream input = null;
                    try {
                        byte[] recvFileCmd = new byte[Constant.bufferSize];// 接收对方第一次发过来的数据，该数据包中包含了要发送的文件名
                        input = socket.getInputStream();
                        input.read(recvFileCmd);// 读取对方发过来的数据
                        int cmdType = recvFileCmd[4];// 按协议这位为命令类型
                        int oprCmd = recvFileCmd[5];// 操作命令

                        byte[] personidBytes = new byte[4];
                        System.arraycopy(recvFileCmd, 6, personidBytes, 0, 4);
                        int personid = ByteAndInt.byteArray2Int(personidBytes);

                        byte[] filesizeBytes = new byte[8];
                        System.arraycopy(recvFileCmd, 100, filesizeBytes, 0, 8);
                        long filesize = ByteAndInt.byteArrayToLong(filesizeBytes);


                        if (DEBUG)
                            Log.d(TAG, "receive a file from " + personid + ", filesize " + filesize);

                        if (cmdType == Constant.CMD_SEND_FILE && oprCmd == Constant.OPR_SEND_FILE) {
                            byte[] fileNameBytes = new byte[Constant.fileNameLength];// 从收到的数据包中提取文件名
                            System.arraycopy(recvFileCmd, 10, fileNameBytes, 0, Constant.fileNameLength);
                            String fName = new String(fileNameBytes).trim();

                            if (DEBUG)
                                Log.d(TAG, "receive a file fName " + fName);
                            String fileName = CacheToFile.SDPATH + fName;
                            Log.d(TAG, "receive a file name " + fileName);

                            File file = new File(fileName);
                            File parent = file.getParentFile();
                            if (!parent.exists()) {
                                parent.mkdirs();
                            }

                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }



                            Log.d(TAG, "receive a file name " + fileName);
                            byte[] readBuffer = new byte[Constant.readBufferSize];
                            output = new FileOutputStream(file);// 打开文件输出流准备把接收到的内容写到文件中
                            int readSize = 0;
                            int length = 0;
                            long count = 0;
                            FileState fs = new FileState();
                            fs.fileName = fileName;
                            fs.fileSize = filesize;
                            fs.currentSize = 0;
                            fs.percent = 0;

                            while (-1 != (readSize = input.read(readBuffer))) {// 循环读取内容
                                output.write(readBuffer, 0, readSize);// 把接收到的内容写到文件中
                                output.flush();
                                length += readSize;
                                count++;
                                if (count % 10 == 0) {
                                    fs.currentSize = length;
                                    fs.percent = ((int) ((Float.valueOf(length) / Float.valueOf(fs.fileSize)) * 100));
                                    Intent intent = new Intent();
                                    intent.setAction(Constant.fileReceiveStateUpdateAction);
                                    sendBroadcast(intent);
                                }
                            }
                            fs.currentSize = length;
                            fs.percent = ((int) ((Float.valueOf(length) / Float.valueOf(fs.fileSize)) * 100));
                            Intent intent = new Intent();
                            intent.setAction(Constant.fileReceiveStateUpdateAction);
                            sendBroadcast(intent);

                            long sendMills = System.currentTimeMillis();
                            int fromTo = LocalMessage.FROM;
                            LocalMessage message = new LocalMessage(fileName, sendMills, personid, fromTo);
                            message.type = LocalMessage.PIC;

                            List<LocalMessage> messages = null;

                            Log.d(TAG, "receive a file name " + fileName);

                            synchronized (msgContainer) {
                                Log.d(TAG, "put file: " + msgContainer);
                                if (msgContainer.containsKey(personid)) {

                                    Log.d(TAG, "have a message list");
                                    messages = msgContainer.get(personid);
                                } else {
                                    Log.d(TAG, "no message list, new a message list");
                                    messages = new ArrayList<LocalMessage>();
                                }

                                Log.d(TAG, "put file: " + message.toString());
                                messages.add(message);
                                msgContainer.put(personid, messages);

                                broadcastNewMessage(personid, messages.size());
                            }
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("msg", getString(R.string.data_receive_error));
                            intent.setAction(Constant.dataReceiveErrorAction);
                            sendBroadcast(intent);
                        }
                    } catch (Exception e) {
                        Intent intent = new Intent();
                        intent.putExtra("msg", e.getMessage());
                        intent.setAction(Constant.dataReceiveErrorAction);
                        sendBroadcast(intent);
                        e.printStackTrace();
                    } finally {
                        try {
                            if (null != input)
                                input.close();
                            if (null != output)
                                output.close();
                            if (!socket.isClosed())
                                socket.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }

            // 开始给对方发送文件
            public void startSendFile() {
                // 获得接收方信息
                Person person = childrenMap.get(Integer.valueOf(tempUid));
                final String userIp = person.ipAddress;
                // 组合头数据包，该数据包中包括要发送的文件名
                final byte[] sendFileCmd = new byte[Constant.bufferSize];
                for (int i = 0; i < Constant.bufferSize; i++)
                    sendFileCmd[i] = 0;
                System.arraycopy(Constant.pkgHead, 0, sendFileCmd, 0, 3);
                sendFileCmd[3] = Constant.CMD82;
                sendFileCmd[4] = Constant.CMD_TYPE1;
                sendFileCmd[5] = Constant.OPR_CMD6;
                System.arraycopy(ByteAndInt.int2ByteArray(mySelf.personId), 0, sendFileCmd, 6, 4);
                for (final FileName file : tempFiles) {// 采用多线程发送文件
                    new Thread() {
                        @Override
                        public void run() {
                            Socket socket = null;
                            OutputStream output = null;
                            InputStream input = null;
                            try {
                                socket = new Socket(userIp, Constant.PORT);
                                byte[] fileNameBytes = file.getFileName().getBytes();
                                int fileNameLength = Constant.fileNameLength + 10;// 清除头文件包的文件名存储区域，以便写新的文件名
                                for (int i = 10; i < fileNameLength; i++)
                                    sendFileCmd[i] = 0;
                                System.arraycopy(fileNameBytes, 0, sendFileCmd, 10, fileNameBytes.length);// 把文件名放入头数据包
                                System.arraycopy(ByteAndInt.longToByteArray(file.fileSize), 0, sendFileCmd, 100, 8);
                                output = socket.getOutputStream();// 构造一个输出流
                                output.write(sendFileCmd);// 把头数据包发给对方
                                output.flush();
                                sleep(1000);// sleep 1秒钟，等待对方处理完
                                // 定义数据发送缓冲区
                                byte[] readBuffer = new byte[Constant.readBufferSize];// 文件读写缓存
                                input = new FileInputStream(new File(file.fileName));// 打开一个文件输入流
                                int readSize = 0;
                                int length = 0;
                                long count = 0;
                                FileState fs = getFileStateByName(file.getFileName(), beSendFileNames);
                                while (-1 != (readSize = input.read(readBuffer))) {// 循环把文件内容发送给对方
                                    output.write(readBuffer, 0, readSize);// 把内容写到输出流中发送给对方
                                    output.flush();
                                    length += readSize;

                                    count++;
                                    if (count % 10 == 0) {
                                        fs.currentSize = length;
                                        fs.percent =
                                                ((int) ((Float.valueOf(length) / Float.valueOf(fs.fileSize)) * 100));
                                        Intent intent = new Intent();
                                        intent.setAction(Constant.fileSendStateUpdateAction);
                                        sendBroadcast(intent);
                                    }
                                }
                                fs.currentSize = length;
                                fs.percent = ((int) ((Float.valueOf(length) / Float.valueOf(fs.fileSize)) * 100));
                                Intent intent = new Intent();
                                intent.setAction(Constant.fileSendStateUpdateAction);
                                sendBroadcast(intent);
                            } catch (Exception e) {
                                // 往界面层发送文件传输出错信息
                                Intent intent = new Intent();
                                intent.putExtra("msg", e.getMessage());
                                intent.setAction(Constant.dataSendErrorAction);
                                sendBroadcast(intent);
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (null != output)
                                        output.close();
                                    if (null != input)
                                        input.close();
                                    if (!socket.isClosed())
                                        socket.close();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }.start();
                }
            }

            // 根据文件名从文件状态列表中获得该文件状态
            private FileState getFileStateByName(String fileName, ArrayList<FileState> fileStates) {
                for (FileState fileState : fileStates) {
                    if (fileState.fileName.equals(fileName)) {
                        return fileState;
                    }
                }
                return null;
            }

            public void release() {
                try {
                    System.out.println("File handler socket closed ...");
                    if (null != sFileSocket)
                        sFileSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // =========================TCP文件传输模块结束==============================================================

        // =========================TCP语音传输模块==================================================================
        // 基于Tcp语音传输模块
        private class AudioHandler extends Thread {

            // private G711Codec codec;
            public AudioHandler() {}

            @Override
            public void run() {
                super.run();
                try {
                    if (sAudioSocket == null) {
                        sAudioSocket = new ServerSocket(Constant.AUDIO_PORT);// 监听音频端口
                    }

                    System.out.println("Audio Handler socket started ...");
                    while (!sAudioSocket.isClosed() && null != sAudioSocket) {
                        Socket socket = sAudioSocket.accept();
                        socket.setSoTimeout(5000);
                        audioPlay(socket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 用来启动音频播放子线程
            public void audioPlay(Socket socket) {
                new AudioPlay(socket).start();
            }

            // 用来启动音频发送子线程
            public void audioSend(Person person) {
                new AudioSend(person).start();
            }

            // 音频播线程
            public class AudioPlay extends Thread {
                Socket socket = null;

                public AudioPlay(Socket socket) {
                    this.socket = socket;
                    // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                }

                @Override
                public void run() {
                    super.run();
                    try {
                        InputStream is = socket.getInputStream();
                        // 获得音频缓冲区大小
                        int bufferSize =
                                android.media.AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                        AudioFormat.ENCODING_PCM_16BIT);

                        // 获得音轨对象
                        AudioTrack player =
                                new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                        AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

                        // 设置喇叭音量
                        player.setStereoVolume(1.0f, 1.0f);
                        // 开始播放声音
                        player.play();
                        byte[] audio = new byte[160];// 音频读取缓存
                        int length = 0;

                        while (!isStopTalk) {
                            length = is.read(audio);// 从网络读取音频数据
                            if (length > 0 && length % 2 == 0) {
                                // for(int i=0;i<length;i++)audio[i]=(byte)(audio[i]*2);//音频放大1倍
                                player.write(audio, 0, length);// 播放音频数据
                            }
                        }
                        player.stop();
                        is.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // 音频发送线程
            public class AudioSend extends Thread {
                Person person = null;

                public AudioSend(Person person) {
                    this.person = person;
                    // android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                }

                @Override
                public void run() {
                    super.run();
                    Socket socket = null;
                    OutputStream os = null;
                    AudioRecord recorder = null;
                    try {
                        socket = new Socket(person.ipAddress, Constant.AUDIO_PORT);
                        socket.setSoTimeout(5000);
                        os = socket.getOutputStream();
                        // 获得录音缓冲区大小
                        int bufferSize =
                                AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                        AudioFormat.ENCODING_PCM_16BIT);

                        // 获得录音机对象
                        recorder =
                                new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                                        bufferSize * 10);

                        recorder.startRecording();// 开始录音
                        byte[] readBuffer = new byte[640];// 录音缓冲区

                        int length = 0;

                        while (!isStopTalk) {
                            length = recorder.read(readBuffer, 0, 640);// 从mic读取音频数据
                            if (length > 0 && length % 2 == 0) {
                                os.write(readBuffer, 0, length);// 写入到输出流，把音频数据通过网络发送给对方
                            }
                        }
                        recorder.stop();
                        os.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            public void release() {
                try {
                    System.out.println("Audio handler socket closed ...");
                    if (null != sAudioSocket)
                        sAudioSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // =========================TCP语音传输模块结束==================================================================
    }

    public void sendFile(final int personId, String filename) {
        // TODO Auto-generated method stub
        if (DEBUG)
            Log.d(TAG, "send file is " + filename);
        final File file = new File(filename);

        new Thread(new Runnable() {
            @Override
            public void run() {
                comBridge.sendfile(personId, file);
            }
        }).start();
    }



}
