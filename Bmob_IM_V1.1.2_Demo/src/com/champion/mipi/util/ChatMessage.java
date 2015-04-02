package com.champion.mipi.util;

import java.io.Serializable;

public class ChatMessage implements Serializable, Comparable {

	private static final long serialVersionUID = 1L;

	public String msg;

	public boolean isReaded = false;

	public long mMills = 0;

	public UserInfo user; // users

	public String mAvatar;

	public int type = 0;
	// 10 TYPE MESSAGE.
	// TEXT
	public static final int TYPE_RECEIVER_TXT = 0;
	public static final int TYPE_SEND_TXT = 1;
	// PICTRUE
	public static final int TYPE_SEND_IMAGE = 2;
	public static final int TYPE_RECEIVER_IMAGE = 3;
	// LOCATION
	public static final int TYPE_SEND_LOCATION = 4;
	public static final int TYPE_RECEIVER_LOCATION = 5;
	// voice
	public static final int TYPE_SEND_VOICE =6;
	public static final int TYPE_RECEIVER_VOICE = 7;
	// file
	public static final int TYPE_SEND_FILE =8;
	public static final int TYPE_RECEIVER_FILE = 9;

	@Override
	public int compareTo(Object arg0) {
		ChatMessage s = (ChatMessage) arg0;
		return mMills < s.mMills ? 1 : (mMills == s.mMills ? 0 : -1);
	}
}
