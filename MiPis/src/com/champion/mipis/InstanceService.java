package com.champion.mipis;

import com.champion.mipis.services.ConnectService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class InstanceService {

    protected static final String TAG = "InstanceService";

    Context mContext;

    Intent mMainServiceIntent;

    ConnectService mService;

    private static InstanceService mInstance;

    public static InstanceService getInstanceService(Context c) {
        if (mInstance == null) {
            mInstance = new InstanceService(c);
        }
        return mInstance;
    }

    private InstanceService(Context c) {
        mContext = c;
        mMainServiceIntent = new Intent(mContext, ConnectService.class);
        mContext.bindService(mMainServiceIntent, sConnection, Context.BIND_AUTO_CREATE);
        mContext.startService(mMainServiceIntent);
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

    public ConnectService getConnectService() {
        return mService;
    }

    public void unBindService() {
        mContext.unbindService(sConnection); 
    }

}
