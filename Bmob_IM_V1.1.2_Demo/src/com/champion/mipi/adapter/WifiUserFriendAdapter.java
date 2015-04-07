package com.champion.mipi.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.champion.mipi.R;
import com.champion.mipi.bean.User;
import com.champion.mipi.util.ImageLoadOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
  * @ClassName: WifiUserFriendAdapter
  * @Description: TODO
  * @author
  * @date
  */
@SuppressLint("DefaultLocale")
public class WifiUserFriendAdapter extends BaseAdapter {
	private Context ct;
	private List<User> data;

	public WifiUserFriendAdapter(Context ct, List<User> datas) {
		this.ct = ct;
		this.data = datas;
	}

	public void updateListView(List<User> list) {
		this.data = list;
		notifyDataSetChanged();
	}

	public void remove(User user){
		this.data.remove(user);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(ct).inflate(
					R.layout.item_wifi_user_friend, null);
			viewHolder = new ViewHolder();

			viewHolder.name = (TextView) convertView
					.findViewById(R.id.tv_friend_name);
			viewHolder.avatar = (ImageView) convertView
					.findViewById(R.id.img_friend_avatar);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		User friend = data.get(position);
		String name = friend.getNick();

		if (TextUtils.isEmpty(friend.getNick())) {
	        name = friend.getUsername();
		}

		final String avatar = friend.getAvatar();

		if (!TextUtils.isEmpty(avatar)) {
			ImageLoader.getInstance().displayImage(avatar, viewHolder.avatar, ImageLoadOptions.getOptions());
		} else {
			viewHolder.avatar.setImageDrawable(ct.getResources().getDrawable(R.drawable.head));
		}
		viewHolder.name.setText(name);

		return convertView;
	}

	static class ViewHolder {
		ImageView avatar;
		TextView name;
	}




}