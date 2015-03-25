package com.champion.mipis;



import com.champion.mipis.services.ConnectService;
import com.champion.mipis.util.Constant;
import com.champion.mipis.util.PreferencesData;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoginMainActivity extends Activity {

    private static final String TAG = "LoginMainActivity";

    TextView mUserNameTextview;
    TextView mPasswordTextView;

    private String mUsername;
    private String mPassword;
    private int mHeadIcon;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mUserNameTextview = (TextView)findViewById(R.id.userId); 

        mPasswordTextView = (TextView)findViewById(R.id.pwd);

        // get username and password
        mUsername = PreferencesData.getStringData(this, Constant.NICK_NAME, "");
        mPassword = PreferencesData.getStringData(this, "password", "");

        mUserNameTextview.setText(mUsername);
        mPasswordTextView.setText(mPassword);

        mHeadIcon = PreferencesData.getIntData(this, Constant.HEAD_ICON_ID, 0);
        if (mHeadIcon == 0) {
            generateHeadIcon();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    private void generateHeadIcon() {
        int myId = Constant.getMyId();
        PreferencesData.setIntData(this, "myId", myId);

        int iconIndex = myId % (Constant.HEADICON_IDS.length);
        PreferencesData.setIntData(this, Constant.HEAD_ICON_ID, Constant.HEADICON_IDS[iconIndex]);
        Log.d(TAG, "generateHeadIcon: " + "myId = " + myId + ", iconIndex = " + iconIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startServices();
    }

    private void startServices() {
        Intent intentStartServices = new Intent();
        intentStartServices.setClass(this, ConnectService.class);
        Log.d(TAG, "login startServices..............");
        startService(intentStartServices);
    }

    public void onClickLogin(View view) {
        String username = mUserNameTextview.getText().toString();
        String password = mPasswordTextView.getText().toString();
        
        if (TextUtils.isEmpty(username)) {
            Log.d(TAG, "username is empty!");
            Toast.makeText(this, "username is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        saveUsernameData(username,password);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveUsernameData(String username, String password) {
        if (!username.equals(mUsername)) {
            PreferencesData.setStringData(this, Constant.NICK_NAME, username);
            PreferencesData.setStringData(this, "password", password);
        }
    }

}
