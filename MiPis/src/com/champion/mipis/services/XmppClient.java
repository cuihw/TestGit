package com.champion.mipis.services;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.net.SocketFactory;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.search.UserSearchManager;

import android.util.Log;

import com.champion.mipis.util.UserBean;

public class XmppClient {

    private static final String TAG = "XmppClient";

    private static XMPPConnection connection;

    private static ConnectionConfiguration config;

    private final static String server = "www.championlee.com.cn";

    private final static int port = 5222;

    private XmppClient instences = null;

    public XmppClient getInstence() {
        if (instences == null) {
            instences = new XmppClient();
        }
        return instences;
    }

    public boolean sendMessage(Chat chat, String message) {
        try {
            
            chat.sendMessage(message);
            return true;
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }


    public XmppClient() {
        config = new ConnectionConfiguration(server, port);
        config.setCompressionEnabled(true);
        config.setSASLAuthenticationEnabled(true);
        config.setDebuggerEnabled(false);
        config.setReconnectionAllowed(true);

        // config.setRosterLoadedAtLogin(true);

        connection = new XMPPConnection(config);
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                connect();
            }}).start();

    }

    public boolean isLogin() {
        if (connection.isConnected()) {
            AccountManager accountManager = connection.getAccountManager();
            for (String attr : accountManager.getAccountAttributes()) {
                fail("AccountAttribute: {0}", attr);
            }
        }
        return false;
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public boolean createAccount(String account, String password) {

        AccountManager accountManager = connection.getAccountManager();

        for (String attr : accountManager.getAccountAttributes()) {
            fail("AccountAttribute: {0}", attr);
        }

        if (accountManager.supportsAccountCreation()) {

            try {
                accountManager.createAccount(account, password);
            } catch (XMPPException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean deleteAccount() {
        try {
            connection.getAccountManager().deleteAccount();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean changePassword(String pwd) {
        try {
            connection.getAccountManager().changePassword(pwd);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean login(String username, String password) {
        try {
            connection.login(username, password);
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public void connect() {
        try {
            connection.connect();

            ChatManager manager = connection.getChatManager();


            final Roster roster = connection.getRoster();  

            manager.addChatListener(new ChatManagerListener() {
                public void chatCreated(Chat chat, boolean arg1) {
                    chat.addMessageListener(new MessageListener() {
                        public void processMessage(Chat arg0, Message message) {
                            if (message != null && message.getBody() != null) {
                                System.out.println("收到消息:" + message.getBody());
                            }
                        }
                    });
                }
            });

            roster.addRosterListener(  
                    new RosterListener() {

                        @Override  
                        public void entriesAdded(Collection<String> arg0) {  
                            // TODO Auto-generated method stub  
                            System.out.println("--------EE:"+"entriesAdded");  
                        }  

                        @Override  
                        public void entriesDeleted(Collection<String> arg0) {  
                            // TODO Auto-generated method stub  
                            System.out.println("--------EE:"+"entriesDeleted");  
                        }  

                        @Override  
                        public void entriesUpdated(Collection<String> arg0) {  
                            // TODO Auto-generated method stub  
                            System.out.println("--------EE:"+"entriesUpdated");  
                        }  

                        @Override  
                        public void presenceChanged(Presence arg0) {  
                            // TODO Auto-generated method stub  
                            System.out.println("--------EE:"+"presenceChanged");  
                        }     
                          
                    });  
        } catch (XMPPException e) {
            e.printStackTrace();
        }
        fail(connection);
        fail(connection.getConnectionID());
    }


    public void testConfig() {
        fail("ServiceName: {0}", config.getServiceName());

        fail("TruststorePassword: {0}", config.getTruststorePassword());

        fail("TruststorePath: {0}", config.getTruststorePath());

        fail("TruststoreType: {0}", config.getTruststoreType());

        SocketFactory socketFactory = config.getSocketFactory();

        fail("SocketFactory: {0}", socketFactory);
    }

    private final void fail(Object o) {
        if (o != null) {
            Log.d(TAG, o.toString());
        }
    }

    private final void fail(Object o, Object... args) {

        if (o != null && args != null && args.length > 0) {
            String s = o.toString();
            for (int i = 0; i < args.length; i++) {
                String item = args[i] == null ? "" : args[i].toString();
                if (s.contains("{" + i + "}")) {
                    s = s.replace("{" + i + "}", item);
                } else {
                    s += " " + item;
                }
            }
            Log.d(TAG, s);
        }
    }

    public static List<UserBean> searchUsers(XMPPConnection connection, String serverDomain, String userName)
            throws XMPPException {
        List<UserBean> results = new ArrayList<UserBean>();
        System.out.println("begin sreach..............." + connection.getHost() + connection.getServiceName());

        UserSearchManager usm = new UserSearchManager(connection);


        Form searchForm = usm.getSearchForm(serverDomain);
        Form answerForm = searchForm.createAnswerForm();
        answerForm.setAnswer("Username", true);
        answerForm.setAnswer("search", userName);
        ReportedData data = usm.getSearchResults(answerForm, serverDomain);

        Iterator<Row> it = data.getRows();
        Row row = null;
        UserBean user = null;
        while (it.hasNext()) {
            user = new UserBean();
            row = it.next();
            user.setUserName(row.getValues("Username").next().toString());
            user.setName(row.getValues("Name").next().toString());
            user.setEmail(row.getValues("Email").next().toString());
            System.out.println(row.getValues("Username").next());
            System.out.println(row.getValues("Name").next());
            System.out.println(row.getValues("Email").next());
            results.add(user);
            // username is not null if the user is exist.
        }

        return results;
    }

    public static void updateStateToAvailable(XMPPConnection connection) {
        Presence presence = new Presence(Presence.Type.available);
        connection.sendPacket(presence);
    }

    public static void updateStateToUnAvailable(XMPPConnection connection) {
        Presence presence = new Presence(Presence.Type.unavailable);
        connection.sendPacket(presence);
    }

    /**
     * change mood.
     * 
     * @param connection
     * @param status
     */
    public static void changeStateMessage(XMPPConnection connection, String status) {
        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus(status);
        connection.sendPacket(presence);

    }

    public static void changeImage(XMPPConnection connection, File f) throws XMPPException, IOException {

        VCard vcard = new VCard();
        vcard.load(connection);

        byte[] bytes;

        bytes = getFileBytes(f);
        String encodedImage = StringUtils.encodeBase64(bytes);
        vcard.setAvatar(bytes);
        vcard.setEncodedImage(encodedImage);
        vcard.setField("PHOTO", "<TYPE>image/jpg</TYPE><BINVAL>" + encodedImage + "</BINVAL>", true);


        ByteArrayInputStream bais = new ByteArrayInputStream(vcard.getAvatar());
/*        Image image = ImageIO.  ImageIO.read(bais);
        ImageIcon ic = new ImageIcon(image);*/

        vcard.save(connection);

    }

    private static byte[] getFileBytes(File file) throws IOException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytes = (int) file.length();
            byte[] buffer = new byte[bytes];
            int readBytes = bis.read(buffer);
            if (readBytes != buffer.length) {
                throw new IOException("Entire file not read");
            }
            return buffer;
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    public static void sendFile(XMPPConnection connection,  
            String user, File file) throws XMPPException, InterruptedException {  
          
        System.out.println("start send file"+file.getName());  
        FileTransferManager transfer = new FileTransferManager(connection);  
        //System.out.println("send file to: "+user+connection.getServiceNameWithPre());  

/*        OutgoingFileTransfer out = transfer.createOutgoingFileTransfer(user+connection.getServiceNameWithPre()+"/Smack");//  
          
        out.sendFile(file, file.getName());  
          
        System.out.println("//////////");  
        System.out.println(out.getStatus());  
        System.out.println(out.getProgress());  
        System.out.println(out.isDone());*/
        System.out.println("//////////");  
          
        System.out.println("finished.");  
    }  
}
