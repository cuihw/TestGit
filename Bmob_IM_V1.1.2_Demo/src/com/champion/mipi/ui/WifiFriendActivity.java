package com.champion.mipi.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.champion.mipi.R;
import com.champion.mipi.adapter.UserFriendAdapter;
import com.champion.mipi.view.ClearEditText;
import com.champion.mipi.view.MyLetterView;
import com.champion.mipi.view.MyLetterView.OnTouchingLetterChangedListener;

public class WifiFriendActivity extends ActivityBase {

    ListView list_wifi_friends;

    TextView dialog;

    MyLetterView right_letter;

    private UserFriendAdapter userAdapter;// friend
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_contacts);
        initView();
    }

    private void initView() {

        list_wifi_friends = (ListView)findViewById(R.id.list_friends);
        hideEditView();
        initRightLetterView();

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
