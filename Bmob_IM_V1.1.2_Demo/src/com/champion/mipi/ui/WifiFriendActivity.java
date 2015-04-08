package com.champion.mipi.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import cn.bmob.im.bean.BmobChatUser;

import com.champion.mipi.R;
import com.champion.mipi.adapter.WifiUserFriendAdapter;
import com.champion.mipi.bean.User;
import com.champion.mipi.util.CollectionUtils;
import com.champion.mipi.view.HeaderLayout;
import com.champion.mipi.view.MyLetterView;
import com.champion.mipi.view.HeaderLayout.HeaderStyle;
import com.champion.mipi.wifiServices.ConnectService;

public class WifiFriendActivity extends ActivityBase {

    private static final String TAG = "WifiFriendActivity";

    ListView list_wifi_friends;

    TextView dialog;

    MyLetterView right_letter;

    private WifiUserFriendAdapter userAdapter;// friend

    List<User> friends = new ArrayList<User>();

    BmobChatUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wifi_contacts);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        mCurrentUser = userManager.getCurrentUser();
        initView();
    }

    public void initTopBarForLeft(String titleName) {
        mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
        mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGEBUTTON);
        mHeaderLayout.setTitleAndLeftImageButton(titleName, R.drawable.base_action_bar_back_bg_selector,
                new OnLeftButtonClickListener());
    }

    private void initView() {


        initTopBarForLeft(getString(R.string.same_wifi));
        list_wifi_friends = (ListView) findViewById(R.id.list_wifi_friends);

        updateFriend();

        list_wifi_friends.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                User user = friends.get(arg2);
                String userName = user.getUsername();

                Intent intent = new Intent(WifiFriendActivity.this, SetMyInfoActivity.class);
                if (mCurrentUser.getUsername().equals(userName)) {
                    intent.putExtra("from", "me");
                    intent.putExtra("username", user.getUsername());
                } else {
                    intent.putExtra("from", "other");
                    intent.putExtra("username", user.getUsername());
                }
                startAnimActivity(intent);
            }
        });
    }
    

    private void regBroadcastReceiver() {

        BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                if (intent.getAction().equals(ConnectService.personHasChangedAction)) {
                    updateFriend();
                }
            }
          };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectService.personHasChangedAction);
        this.registerReceiver(receiver, filter);
    }

    private void updateFriend() {

        getWifiUsers();

        if (friends != null) {

            userAdapter = new WifiUserFriendAdapter(this, friends);

            list_wifi_friends.setAdapter(userAdapter);
        }
    }

    private void getWifiUsers() {
        ConnectService service = ConnectService.getInstence();
        if (service != null) {
            Map<String, User> userMap = service.getUserInfoMap();
            List<User> users = CollectionUtils.userMap2list(userMap);
            Log.d(TAG, "users size: " + users.size());
            friends = users;
        }
    }

}
