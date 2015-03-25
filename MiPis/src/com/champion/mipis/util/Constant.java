/**
 * @author Chris_Cui 2014-06-03
 */

package com.champion.mipis.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.champion.mipis.R;

public class Constant {

    public static Map<String, Integer> exts = new HashMap<String, Integer>();
    static {
        exts.put("doc", R.drawable.doc);
        exts.put("docx", R.drawable.doc);
        exts.put("xls", R.drawable.xls);
        exts.put("xlsx", R.drawable.xls);
        exts.put("ppt", R.drawable.ppt);
        exts.put("pptx", R.drawable.ppt);
        exts.put("jpg", R.drawable.image);
        exts.put("jpeg", R.drawable.image);
        exts.put("gif", R.drawable.image);
        exts.put("png", R.drawable.image);
        exts.put("ico", R.drawable.image);
        exts.put("apk", R.drawable.apk);
        exts.put("jar", R.drawable.jar);
        exts.put("rar", R.drawable.rar);
        exts.put("zip", R.drawable.rar);
        exts.put("mp3", R.drawable.music);
        exts.put("wma", R.drawable.music);
        exts.put("aac", R.drawable.music);
        exts.put("ac3", R.drawable.music);
        exts.put("ogg", R.drawable.music);
        exts.put("flac", R.drawable.music);
        exts.put("midi", R.drawable.music);
        exts.put("pcm", R.drawable.music);
        exts.put("wav", R.drawable.music);
        exts.put("amr", R.drawable.music);
        exts.put("m4a", R.drawable.music);
        exts.put("ape", R.drawable.music);
        exts.put("mid", R.drawable.music);
        exts.put("mka", R.drawable.music);
        exts.put("svx", R.drawable.music);
        exts.put("snd", R.drawable.music);
        exts.put("vqf", R.drawable.music);
        exts.put("aif", R.drawable.music);
        exts.put("voc", R.drawable.music);
        exts.put("cda", R.drawable.music);
        exts.put("mpc", R.drawable.music);
        exts.put("mpeg", R.drawable.video);
        exts.put("mpg", R.drawable.video);
        exts.put("dat", R.drawable.video);
        exts.put("ra", R.drawable.video);
        exts.put("rm", R.drawable.video);
        exts.put("rmvb", R.drawable.video);
        exts.put("mp4", R.drawable.video);
        exts.put("flv", R.drawable.video);
        exts.put("mov", R.drawable.video);
        exts.put("qt", R.drawable.video);
        exts.put("asf", R.drawable.video);
        exts.put("wmv", R.drawable.video);
        exts.put("avi", R.drawable.video);
        exts.put("3gp", R.drawable.video);
        exts.put("mkv", R.drawable.video);
        exts.put("f4v", R.drawable.video);
        exts.put("m4v", R.drawable.video);
        exts.put("m4p", R.drawable.video);
        exts.put("m2v", R.drawable.video);
        exts.put("dat", R.drawable.video);
        exts.put("xvid", R.drawable.video);
        exts.put("divx", R.drawable.video);
        exts.put("vob", R.drawable.video);
        exts.put("mpv", R.drawable.video);
        exts.put("mpeg4", R.drawable.video);
        exts.put("mpe", R.drawable.video);
        exts.put("mlv", R.drawable.video);
        exts.put("ogm", R.drawable.video);
        exts.put("m2ts", R.drawable.video);
        exts.put("mts", R.drawable.video);
        exts.put("ask", R.drawable.video);
        exts.put("trp", R.drawable.video);
        exts.put("tp", R.drawable.video);
        exts.put("ts", R.drawable.video);
    }

    // define my Action
    public static final String updateMyInformationAction = "com.champion.mipis.updateMyInformation";
    public static final String personHasChangedAction = "com.champion.mipis.personHasChanged";
    public static final String hasMsgUpdatedAction = "com.champion.mipis.hasMsgUpdated";
    public static final String receivedSendFileRequestAction = "com.champion.mipis.receivedSendFileRequest";
    public static final String refuseReceiveFileAction = "com.champion.mipis.refuseReceiveFile";
    public static final String remoteUserRefuseReceiveFileAction = "com.champion.mipis.remoteUserRefuseReceiveFile";
    public static final String dataReceiveErrorAction = "com.champion.mipis.dataReceiveError";
    public static final String dataSendErrorAction = "com.champion.mipis.dataSendError";
    public static final String whoIsAliveAction = "com.champion.mipis.whoIsAlive";// query which
                                                                                     // activity is
                                                                                     // on
                                                                                     // foreground
    public static final String imAliveNow = "com.champion.mipis.imAliveNow";
    public static final String remoteUserUnAliveAction = "com.champion.mipis.remoteUserUnAlive";
    public static final String fileSendStateUpdateAction = "com.champion.mipis.fileSendStateUpdate";
    public static final String fileReceiveStateUpdateAction = "com.champion.mipis.fileReceiveStateUpdate";
    public static final String receivedTalkRequestAction = "com.champion.mipis.receivedTalkRequest";
    public static final String acceptTalkRequestAction = "com.champion.mipis.acceptTalkRequest";
    public static final String remoteUserClosedTalkAction = "com.champion.mipis.remoteUserClosedTalk";

    // System Action declare
    public static final String bootCompleted = "android.intent.action.BOOT_COMPLETED";
    public static final String WIFIACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String ETHACTION = "android.intent.action.ETH_STATE";

    // create the ID code.
    public static int getMyId() {
        int id = (int) (Math.random() * 1000000);
        return id;
    }
    
    public static int[] HEADICON_IDS = {R.drawable.black_bird,
                                R.drawable.blue_bird,
                                R.drawable.green_bird,
                                R.drawable.green_pig,
                                R.drawable.pig_egg,
                                R.drawable.red_bird,
                                R.drawable.white_bird,
                                R.drawable.yellow_bird,
                                R.drawable.face0,
                                R.drawable.face1,
                                R.drawable.face2,
                                R.drawable.face3,
                                R.drawable.face4,
                                R.drawable.face5,
                                R.drawable.face6,
                                R.drawable.face7,
                                R.drawable.face8,
                                R.drawable.face9,
                                R.drawable.face10,
                                R.drawable.face11,
                                R.drawable.face12,
                                R.drawable.face13,
                                R.drawable.face14,
                                R.drawable.face15};

    // other
    // chinese length is 60, UTF-8 define one chinese char has three bytes. So define the message
    // length is 180 bytes.
    //
    // filename's length is 30. so define 90 bytes.
    public static final int bufferSize = 256;
    public static final int msgLength = 180;
    public static final int fileNameLength = 90;
    public static final int readBufferSize = 4096;// buffer for read and write
    public static final byte[] pkgHead = "AND".getBytes();
    public static final int CMD80 = 80;
    public static final int CMD81 = 81;
    public static final int CMD82 = 82;
    public static final int CMD83 = 83;
    public static final int CMD_TYPE1 = 1;
    public static final int CMD_TYPE2 = 2;
    public static final int CMD_TYPE3 = 3;
    public static final int OPR_CMD1 = 1;
    public static final int OPR_CMD2 = 2;
    public static final int OPR_CMD3 = 3;
    public static final int OPR_CMD4 = 4;
    public static final int OPR_CMD5 = 5;
    public static final int OPR_CMD6 = 6;
    public static final int OPR_CMD10 = 10;

    public static final int CMD_SEND_FILE = 11;
    public static final int OPR_SEND_FILE = 12;

    public static final String MULTICAST_IP = "239.9.9.1";
    public static final int PORT = 5760;
    public static final int AUDIO_PORT = 5761;

    // int to ip conversion
    public static String intToIp(int i) {
        String ip = ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + (i & 0xFF);

        return ip;
    }

    // other
    public static final int FILE_RESULT_CODE = 1;
    public static final int SELECT_FILES = 1;// show the file in the files selector.
    public static final int SELECT_FILE_PATH = 2;// file selector only show the file path.
    // file selected state save to map.
    public static TreeMap<Integer, Boolean> fileSelectedState = new TreeMap<Integer, Boolean>();

    // conversion the file size.(k,m,g);
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = fileS + "B";
            // fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    // for SharedPreferences.
    public static final String HEAD_ICON_ID = "headIconId";
    public static final String NICK_NAME = "nickName";
    public static final String MY_ID = "myId";
    public static final String SHARE_PRE = "SharedPreferences";
    

}
