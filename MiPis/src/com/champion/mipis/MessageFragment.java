package com.champion.mipis;


import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.champion.mipis.services.ConnectService;
import com.champion.mipis.util.Constant;
import com.champion.mipis.util.LocalMessage;
import com.champion.mipis.util.Person;

@SuppressLint("NewApi")
public class MessageFragment extends Fragment {

    private static final String TAG = "MessageFragment";

    private ListView mListView;

    private View mFragmentView;

    private MessageChangeReceviver mMessageChangeReceviver;

    private IntentFilter mIntentFilter;

    private MessageListAdapter mMessageListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        Log.d(TAG, "onCreateView");
        mFragmentView = inflater.inflate(R.layout.last_message, container, false);

        Log.d(TAG, "MessageFragment");

        this.getActivity().setTitle("消息列表");

        return mFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");
        initListView();
    }

    private void initListView() {

        mListView = (ListView) mFragmentView.findViewById(R.id.last_message_listView);
        
        Activity act = this.getActivity();

        ConnectService service = null;
        
        if (act instanceof MainActivity) {
            Log.d(TAG, "activity is instanceof MainActivity");
            service = ((MainActivity) act).getServices();
        }
        
        if (service != null && mMessageListAdapter == null) {
            mMessageListAdapter = new MessageListAdapter(this.getActivity(), service);
        }
        
        if (mMessageListAdapter != null) {

            Log.d(TAG, "initListView set adapter");

            mListView.setAdapter(mMessageListAdapter);

            mListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    // TODO Auto-generated method stub

                    Log.d(TAG, "index = " + arg2);
                    LocalMessage msg = (LocalMessage) mMessageListAdapter.getItem(arg2);

                    int personid = msg.personID;

                    startCharMessage(personid);

                }


            });
            updateListView();            
        }
    }

    private void startCharMessage(int personid) {
        
        Activity act = this.getActivity();

        ConnectService service = null;
        
        if (act instanceof MainActivity) {
            Log.d(TAG, "activity is instanceof MainActivity");
            service = ((MainActivity) act).getServices();
        }

        if (service != null) {
            Map<Integer, Person> personMap = service.getAllPerson();
            if (personMap != null) {
                Person person = personMap.get(personid);
                Person me = service.getMe();

                if (person != null && me != null) {
                    Intent intent = new Intent(this.getActivity(), ChatMessageActivity.class);
                    intent.putExtra("person", person);
                    intent.putExtra("me", me);
                    startActivity(intent);
                }
            }
        }

    }

    private void updateListView() {

        Log.d(TAG, "updateListView");
        mMessageListAdapter.getShowMessageList();
        mMessageListAdapter.notifyDataSetChanged();
    }

    private void registerReceiver() {
        mMessageChangeReceviver = new MessageChangeReceviver();

        mIntentFilter = new IntentFilter();

        mIntentFilter.addAction(Constant.hasMsgUpdatedAction);

        this.getActivity().registerReceiver(mMessageChangeReceviver, mIntentFilter);
    }

    private void unRegisterReceiver() {
        this.getActivity().unregisterReceiver(mMessageChangeReceviver);
    }

    private class MessageChangeReceviver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(Constant.hasMsgUpdatedAction)) {
                Log.d(TAG, "has message updated action..............");
                updateListView();
            }
        }

    }


    @Override
    public void onAttach(Activity activity) {
        registerReceiver();

        Log.d(TAG, "onAttach");
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");
        unRegisterReceiver();
        super.onDetach();
    }



}
