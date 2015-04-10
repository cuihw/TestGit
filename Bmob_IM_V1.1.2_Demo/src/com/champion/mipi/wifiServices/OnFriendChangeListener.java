package com.champion.mipi.wifiServices;

import java.util.List;

import com.champion.mipi.bean.User;

public interface OnFriendChangeListener {
    public void updateFriend(List<User> userList);
}
