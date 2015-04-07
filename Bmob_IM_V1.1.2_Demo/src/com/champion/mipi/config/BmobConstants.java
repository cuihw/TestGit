package com.champion.mipi.config;

import android.annotation.SuppressLint;
import android.os.Environment;


/**
 * @ClassName: BmobConstants
 * @Description: TODO
 * @author smile
 * @date 2014-6-19 ����2:48:33
 */
@SuppressLint("SdCardPath")
public class BmobConstants {

    public static String BMOB_PICTURE_PATH = Environment.getExternalStorageDirectory() + "/mipi/image/";

    public static String MyAvatarDir = Environment.getExternalStorageDirectory()+ "/mipi/avatar/";
    public static final int REQUESTCODE_UPLOADAVATAR_CAMERA = 1;
    public static final int REQUESTCODE_UPLOADAVATAR_LOCATION = 2;
    public static final int REQUESTCODE_UPLOADAVATAR_CROP = 3;

    public static final int REQUESTCODE_TAKE_CAMERA = 0x000001;
    public static final int REQUESTCODE_TAKE_LOCAL = 0x000002;
    public static final int REQUESTCODE_TAKE_LOCATION = 0x000003;
    public static final String EXTRA_STRING = "extra_string";


    public static final String ACTION_REGISTER_SUCCESS_FINISH = "register.success.finish";// ע��ɹ�֮���½ҳ���˳�
}
