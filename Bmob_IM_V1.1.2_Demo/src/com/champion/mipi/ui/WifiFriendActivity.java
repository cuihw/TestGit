package com.champion.mipi.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.champion.mipi.R;
import com.champion.mipi.adapter.WifiUserFriendAdapter;
import com.champion.mipi.bean.User;
import com.champion.mipi.ui.BaseActivity.OnLeftButtonClickListener;
import com.champion.mipi.util.CollectionUtils;
import com.champion.mipi.view.HeaderLayout;
import com.champion.mipi.view.MyLetterView;
import com.champion.mipi.view.HeaderLayout.HeaderStyle;
import com.champion.mipi.wifiServices.ConnectService;

public class WifiFriendActivity extends ActivityBase {

    private static final String TAG = null;

    ListView list_wifi_friends;

    TextView dialog;

    MyLetterView right_letter;

    private WifiUserFriendAdapter userAdapter;// friend

    List<User> friends = new ArrayList<User>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wifi_contacts);
        
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        initView();
    }
    public void initTopBarForLeft(String titleName) {
        mHeaderLayout = (HeaderLayout)findViewById(R.id.common_actionbar);
        mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGEBUTTON);
        mHeaderLayout.setTitleAndLeftImageButton(titleName,
                R.drawable.base_action_bar_back_bg_selector,
                new OnLeftButtonClickListener());
    }
    private void initView() {
        initTopBarForLeft(getString(R.string.same_wifi));
        list_wifi_friends = (ListView)findViewById(R.id.list_wifi_friends);
        
        getWifiUsers();

        if (friends != null) {

            userAdapter = new WifiUserFriendAdapter(this, friends);

            list_wifi_friends.setAdapter(userAdapter);
        }

        list_wifi_friends.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                
             }});
    }
    
    private void getWifiUsers() {
        ConnectService service = ConnectService.getInstence();
        if (service!= null) {
            Map<String, User> userMap = service.getUserInfoMap();
            List<User> users = CollectionUtils.userMap2list(userMap);
            Log.d(TAG, "users size: " + users.size());
            friends = users;
        }
    }


}
