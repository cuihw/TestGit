package com.champion.mipi.wifiServices;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.champion.mipi.bean.User;

public class DataServerManager {
    
    private List<OnFriendChangeListener> mFriendListenerList = new ArrayList<OnFriendChangeListener> ();

    private static DataServerManager instences = null;
    
    private Context mContext;

    private ConnectService mService;
    
    private DataServerManager (Context c) {
        mContext = c;
        mService = ConnectService.getInstence();
        if (mService == null) {

        }
    }

    public List<User> getFriend() {
        return null;
    }

    public List<User> getWifiFriend() {
        return null;
    }

    public void registerFriendListener(OnFriendChangeListener listener) {
        if (!mFriendListenerList.contains(listener)) {
            mFriendListenerList.add(listener);            
        }
    }
    
    public void unRregisterFriendListener(OnFriendChangeListener listener) {
        if (mFriendListenerList.contains(listener)) {
            mFriendListenerList.remove(listener);            
        }
    }
    
}
