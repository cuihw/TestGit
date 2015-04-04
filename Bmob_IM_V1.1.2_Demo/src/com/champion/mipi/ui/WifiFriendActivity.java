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
import com.champion.mipi.adapter.UserFriendAdapter;
import com.champion.mipi.bean.User;
import com.champion.mipi.util.CollectionUtils;
import com.champion.mipi.view.ClearEditText;
import com.champion.mipi.view.MyLetterView;
import com.champion.mipi.view.MyLetterView.OnTouchingLetterChangedListener;
import com.champion.mipi.wifiServices.ConnectService;

public class WifiFriendActivity extends ActivityBase {

    private static final String TAG = null;

    ListView list_wifi_friends;

    TextView dialog;

    MyLetterView right_letter;

    private UserFriendAdapter userAdapter;// friend

    List<User> friends = new ArrayList<User>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_contacts);
        
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        initView();
    }

    private void initView() {

        list_wifi_friends = (ListView)findViewById(R.id.list_friends);
        hideEditView();
        initRightLetterView();
        
        //getWifiUsers();
        userAdapter = new UserFriendAdapter(this, friends);

        list_wifi_friends.setAdapter(userAdapter);

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
            Log.d(TAG, "users　 " + users.size());
            friends = users;
        }
    }

    private void hideEditView() {
        ClearEditText editView = (ClearEditText)findViewById(R.id.et_msg_search);
        editView.setVisibility(View.GONE);
    }

    private void initRightLetterView() {
        right_letter = (MyLetterView)findViewById(R.id.right_letter);
        dialog = (TextView)findViewById(R.id.dialog);
        right_letter.setTextView(dialog);
        right_letter.setOnTouchingLetterChangedListener(new LetterListViewListener());
    }

    private class LetterListViewListener implements
            OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(String s) {
            // 该字母首次出现的位置
            int position = userAdapter.getPositionForSection(s.charAt(0));
            if (position != -1) {
                list_wifi_friends.setSelection(position);
            }
        }
    }

}
