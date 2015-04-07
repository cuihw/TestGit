package com.champion.mipi.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import cn.bmob.im.BmobUserManager;

/** 
  * @ClassName: ActivityBase
  * @Description: TODO
  * @date 2014-6-13 
  */
public class ActivityBase extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		checkLogin();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		checkLogin();
	}
	
	public void checkLogin() {
		BmobUserManager userManager = BmobUserManager.getInstance(this);
		if (userManager.getCurrentUser() == null) {
			ShowToast("Current user is not login.");
			startActivity(new Intent(this, LoginActivity.class));
			finish();
		}
	}
	
	/**
	  * hideSoftInputView
	  * @Title: hideSoftInputView
	  * @Description: TODO
	  * @param  
	  * @return void
	  * @throws
	  */
	public void hideSoftInputView() {
		InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
}
