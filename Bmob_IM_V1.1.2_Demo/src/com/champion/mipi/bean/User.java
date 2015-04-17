package com.champion.mipi.bean;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.datatype.BmobGeoPoint;

/**
  * @ClassName: TextUser
  * @Description: TODO
  */
public class User extends BmobChatUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 */
	private String sortLetters;
	
	/**
	 */
	private boolean sex;

    public String ipAddress = null;

    public long updateTime = 0;

    public User() {}
    
    public User(BmobChatUser buser) {

    }
    
    /**
	 */
	private BmobGeoPoint location;//
	
	public BmobGeoPoint getLocation() {
		return location;
	}
	public void setLocation(BmobGeoPoint location) {
		this.location = location;
	}

	public boolean getSex() {
		return sex;
	}

	public void setSex(boolean sex) {
		this.sex = sex;
	}

	public String getSortLetters() {
		return sortLetters;
	}

	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}

}
