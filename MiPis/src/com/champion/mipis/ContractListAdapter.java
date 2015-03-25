package com.champion.mipis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.champion.mipis.util.Person;
import com.champion.mipis.util.PreferencesData;

public class ContractListAdapter extends BaseAdapter {

    private static final String TAG = "ContractListAdapter";

    private Map<Integer, Person> childrenMap = new HashMap<Integer, Person>(); // current online
                                                                               // user

    private ArrayList<Integer> personKeys = new ArrayList<Integer>(); // current online user id

    Context mContext;

    private LayoutInflater mInflater;

    public ContractListAdapter(Context context, ArrayList<Integer> personKeys, Map<Integer, Person> childrenMap) {
        this.personKeys = personKeys;
        this.childrenMap = childrenMap;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<Integer> personKeys, Map<Integer, Person> childrenMap) {
        this.personKeys = personKeys;
        this.childrenMap = childrenMap;
    }


    @Override
    public int getCount() {
        return personKeys.size();
    }

    @Override
    public Object getItem(int arg0) {
        Person person = null;

        if (arg0 >= 0 && arg0 < personKeys.size()) {
            int key = personKeys.get(arg0);
            person = childrenMap.get(key);
        }
        return person;
    }

    @Override
    public long getItemId(int arg0) {
        int key = 0;
        if (arg0 >= 0 && arg0 < personKeys.size()) {
            key = personKeys.get(arg0);
        }
        return key;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        ViewHolder holder;
        Log.d(TAG, "getView " + position + ", " + convertView);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.contract_list_item, null);
            holder = new ViewHolder();

            holder.headImageView = (ImageView) convertView.findViewById(R.id.contract_list_item_imageview);
            holder.name = (TextView) convertView.findViewById(R.id.contract_list_item_name_tv);
            convertView.setTag(holder);
        } else {

            Log.d(TAG, "convertView is not null getView " + position);
            holder = (ViewHolder) convertView.getTag();
        }

        if (personKeys.size() > position) {
            int personkey = personKeys.get(position);
            Person person = childrenMap.get(personkey);
            holder.headImageView.setImageResource(person.personHeadIconId);


            int myID = PreferencesData.getIntData(mContext, "myId", 0);
            String personName = person.personNickeName;

            if (myID == personkey) {
                personName = person.personNickeName + "(自己)";
            }

            holder.name.setText(personName);
        }

        Log.d(TAG, "index = " + position);

        return convertView;

    }

    public final class ViewHolder {
        public ImageView headImageView;
        public TextView name;
    }
}
