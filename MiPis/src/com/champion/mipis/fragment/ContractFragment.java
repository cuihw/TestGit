package com.champion.mipis.fragment;


import java.util.ArrayList;
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

import com.champion.mipis.ChatMessageActivity;
import com.champion.mipis.ContractListAdapter;
import com.champion.mipis.MainActivity;
import com.champion.mipis.R;
import com.champion.mipis.R.id;
import com.champion.mipis.R.layout;
import com.champion.mipis.services.ConnectService;
import com.champion.mipis.util.Constant;
import com.champion.mipis.util.Person;


@SuppressLint("NewApi")
public class ContractFragment extends Fragment {

    private static final String TAG = "ContractFragment";

    private Context mContext;

    private ContractChangeReceviver mContractChangeReceviver;

    private IntentFilter mIntentFilter;

    private ListView mListView;

    private View mFragmentView;

    private ContractListAdapter mContractListAdapter;

    private ConnectService mConnectService;

    private ArrayList<Integer> mPersonidList;

    private Map<Integer, Person> mPersonMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment

        mFragmentView = inflater.inflate(R.layout.message, container, false);

        Log.d(TAG, "ContractFragment onCreateView");

        mContext = getActivity();

        getActivity().setTitle("好友列表");

        return mFragmentView;
    }

    @Override
    public void onResume() {

        Log.d(TAG, "ContractFragment onResume");
        super.onResume();

        registerReceiver();
        initListView();
    }

    private void initListView() {
        mListView = (ListView) mFragmentView.findViewById(R.id.listView);
        updateContract();
        mListView.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.d(TAG, "index = " + arg2);
                
                if (mPersonidList != null && mPersonidList.size() > arg2) {
                    int personId = mPersonidList.get(arg2);

                    Person  person = mPersonMap.get(personId);
                    
                    personId = mPersonidList.get(0);
                    
                    Person  me = mPersonMap.get(personId);

                    Intent intent = new Intent(mContext, ChatMessageActivity.class);
                    intent.putExtra("person", person);
                    intent.putExtra("me", me);
                    startActivity(intent);
                }
                
            }});
    }

    private void updateContract() {
        Log.d(TAG, "updateContract");
        if (mConnectService == null) {

            Activity activity = getActivity();

            if (activity instanceof MainActivity) {
                Log.d(TAG, "activity is instanceof MainActivity");
                mConnectService = ((MainActivity) activity).getServices();
            }
        }

        if (mConnectService != null) {
            mPersonidList = mConnectService.getPersonKeys();
            mPersonMap = mConnectService.getAllPerson();

            if (mContractListAdapter == null) {
                mContractListAdapter = new ContractListAdapter(mContext, mPersonidList, mPersonMap);   

            } else {
                mContractListAdapter.setData(mPersonidList, mPersonMap);
                mContractListAdapter.notifyDataSetChanged();
            }
            mListView.setAdapter(mContractListAdapter);

        }
    }

    private void registerReceiver() {
        mContractChangeReceviver = new ContractChangeReceviver();

        mIntentFilter = new IntentFilter();

        mIntentFilter.addAction(Constant.personHasChangedAction);
        mContext.registerReceiver(mContractChangeReceviver, mIntentFilter);
    }


    private void unRegisterReceiver() {
        mContext.unregisterReceiver(mContractChangeReceviver);
    }

    private class ContractChangeReceviver extends BroadcastReceiver{

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(Constant.personHasChangedAction)) {
                updateContract();
            }
        }
    }

    @Override
    public void onStop() {
        unRegisterReceiver();
        super.onStop();
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "ContractFragment onAttach");
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "ContractFragment onDetach");
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
    
}
