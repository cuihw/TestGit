package com.champion.mipi.util;

import java.io.Serializable;

public class UserInfo implements Serializable {

	public String mAvatar;
	public String mAccount;
	public String mNickeName = null;
	public String ipAddress = null;
	public long updateTime = 0;
	public String mHeadIconPath;

	public UserInfo(String userId, String  HeadIcon ,
			String personNickeName, String ipAddress, long loginTime) {
		this.mAccount = userId;
		this.mHeadIconPath = HeadIcon;
		this.mNickeName = personNickeName;
		this.ipAddress = ipAddress;
		this.updateTime = loginTime;
	}

	public UserInfo() {
	}

	public String toString() {
		return "UserInfo: mAccount = " + mAccount + ", personHeadIconId = "
				+ mHeadIconPath + ", personNickeName = " + mNickeName
				+ ", ipAddress = " + ipAddress;
	}
}
