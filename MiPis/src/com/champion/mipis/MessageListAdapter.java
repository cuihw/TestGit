package com.champion.mipis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.champion.mipis.ContractListAdapter.ViewHolder;
import com.champion.mipis.services.ConnectService;
import com.champion.mipis.util.LocalMessage;
import com.champion.mipis.util.Person;

public class MessageListAdapter extends BaseAdapter {

    private static final String TAG = "MessageListAdapter";
    
    ConnectService mConnectService;
    
    Map<Integer, List<LocalMessage>>  mMessageMap;
    
    List<LocalMessage> mMessagelist;
    

    private LayoutInflater mInflater;

    @Override
    public int getCount() {
        if (mMessagelist != null)
            return mMessagelist.size();
        return 0;
    }

    public MessageListAdapter(Context c, ConnectService service) {

        Log.d(TAG, "constructor MessageListAdapter()");
        
        mConnectService = service;

        getShowMessageList();

        mInflater = LayoutInflater.from(c);
    }

    // check the user data from the service.
    public void getShowMessageList() {

        Log.d(TAG, "getShowMessageList()");
        if (mConnectService != null) {
            mMessageMap = mConnectService.getMsgContainer();

            if (mMessagelist == null) {
                mMessagelist = new ArrayList<LocalMessage>();                
            }
            mMessagelist.clear();

            for (int key : mMessageMap.keySet()) {
                List<LocalMessage> messages = mMessageMap.get(key);
                if (messages != null) {
                    int size = messages.size();
                    LocalMessage msg = messages.get(size -1);
                    mMessagelist.add(msg);
                }
            }
            
            if (mMessagelist.size() > 0) {
                Collections.sort(mMessagelist);
            }
            
            for (LocalMessage msg:mMessagelist) {
                
                Log.d(TAG, "getShowMessageList() : " + msg.toString());
            }
            
        }
    }

    @Override
    public Object getItem(int arg0) {

        if (mMessagelist!= null && mMessagelist.size() > arg0) {
            return mMessagelist.get(arg0);
        }

        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup arg2) {

        ViewHolder holder;
        Log.d(TAG, "getView pos = " + pos + ", " + convertView);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.last_message_list_item, null);
            holder = new ViewHolder();

            holder.headImageView = (ImageView) convertView.findViewById(R.id.head_img_item);
            holder.name = (TextView) convertView.findViewById(R.id.name_tv);
            holder.lastMsg = (TextView) convertView.findViewById(R.id.last_message_tv);
            

            Log.d(TAG, "headImageView = " + holder.headImageView  + ",  holder.name = " +  holder.name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mMessagelist != null || mMessagelist.size() > pos) {
            LocalMessage message = mMessagelist.get(pos);
            
            Log.d(TAG, pos + "message is " + message.toString());

            if (mConnectService != null) {
                Log.d(TAG, "get person info");
                Person person = mConnectService.getAllPerson().get(message.personID);
                
                Log.d(TAG, "person = " + person);
                if (person != null) {
                    holder.headImageView.setImageResource(person.personHeadIconId);
                    holder.name.setText(person.personNickeName);
                    holder.lastMsg.setText(message.msg);
                }
            }
        }

        return convertView;
    }

    public final class ViewHolder {
        public ImageView headImageView;
        public TextView name;
        public TextView lastMsg;
    }
}
