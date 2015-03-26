package com.champion.mipis;

import com.champion.mipis.services.ConnectService;
import com.champion.mipis.util.Constant;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

    protected static final String TAG = "MainActivity";

    Fragment mOldFragment;

    Intent mMainServiceIntent;

    ConnectService mService;

    private long mExitTime;

    ImageView mPromptNewMessageImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.activity_main);
        initConnectServer();
        registerReceiver();
        initView();
    }

    @SuppressLint("NewApi")
    private void initView() {
        Fragment currentFragment = new VideoFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_place, currentFragment);
        fragmentTransaction.commit();
        mOldFragment = currentFragment;
        mPromptNewMessageImg = (ImageView)findViewById(R.id.home_message_prompt);
        mPromptNewMessageImg.setVisibility(View.INVISIBLE);
    }


    private void promptNewMessage() {
        if (mService != null) {
            mPromptNewMessageImg.setVisibility(View.VISIBLE);
        } else {
            mPromptNewMessageImg.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * ConnectService bind to activity connect.
     */
    private ServiceConnection sConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((ConnectService.ServiceBinder) service).getService();
            Log.d(TAG, "Service connected to activity...");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Log.d(TAG, "Service disconnected to activity...");
        }
    };

    private void initConnectServer() {
        Log.d(TAG, "Service disconnected to activity...");
        mMainServiceIntent = new Intent(this, ConnectService.class);
        bindService(mMainServiceIntent, sConnection, BIND_AUTO_CREATE);
        startService(mMainServiceIntent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub

        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            if (mOldFragment instanceof VideoFragment) {
                VideoFragment vfragment = (VideoFragment)mOldFragment;
                if (vfragment.webViewGoBack()) {
                    return true;                    
                }
            }
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unRegisterReceiver();

        if (mService != null) {
            unbindService(sConnection);            
        }

        super.onDestroy();
    }

    @SuppressLint("NewApi")
    public void selectFrag(View view) {

        Fragment currentFragment = null;


        if (view == findViewById(R.id.bottom_home_message)) {
            // set prompt message is invisible.
            mPromptNewMessageImg.setVisibility(View.INVISIBLE);

            if (mOldFragment instanceof MessageFragment) {
                return;
            } else {
                currentFragment = new MessageFragment();                
            }

        } else if (view == findViewById(R.id.bottom_home_contract)) {
            if (mOldFragment instanceof ContractFragment) {
                return;
            } else {
                currentFragment = new ContractFragment();                
            }

        } else if (view == findViewById(R.id.bottom_home_video)) {
            if (mOldFragment instanceof VideoFragment) {
                return;
            } else {
                currentFragment = new VideoFragment();                
            }
        } else if (view.getId() == R.id.home_setting) {
            if (mOldFragment instanceof SettingFragment) {
                return;
            } else {
                currentFragment = new SettingFragment();
            }
        }

        if (currentFragment!= null) {

            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_place, currentFragment);
            fragmentTransaction.commit();
            mOldFragment = currentFragment;
        }

    }

    public ConnectService getServices() {
        return mService;
    }
    
    MessageChangeReceviver mMessageChangeReceviver;
    
    IntentFilter mIntentFilter;
    private void registerReceiver() {
        mMessageChangeReceviver = new MessageChangeReceviver();

        mIntentFilter = new IntentFilter();

        mIntentFilter.addAction(Constant.hasMsgUpdatedAction);

        this.registerReceiver(mMessageChangeReceviver, mIntentFilter);
    }

    private void unRegisterReceiver() {
        this.unregisterReceiver(mMessageChangeReceviver);
    }

    private class MessageChangeReceviver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(Constant.hasMsgUpdatedAction)) {
                Log.d(TAG, "has message updated action..............");
                promptNewMessage();
            }
        }
    }

}
