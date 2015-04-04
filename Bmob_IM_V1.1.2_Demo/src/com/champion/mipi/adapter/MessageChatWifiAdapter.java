package com.champion.mipi.adapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;



import cn.bmob.im.config.BmobConfig;

import com.champion.mipi.R;

import com.champion.mipi.adapter.base.BaseListAdapter;
import com.champion.mipi.adapter.base.ViewHolder;
import com.champion.mipi.ui.SetMyInfoActivity;
import com.champion.mipi.util.ChatMessage;
import com.champion.mipi.util.ImageLoadOptions;
import com.champion.mipi.util.TimeUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class MessageChatWifiAdapter extends BaseListAdapter<ChatMessage> {

	// 8��Item������
	// �ı�
	private final int TYPE_RECEIVER_TXT = 0;
	private final int TYPE_SEND_TXT = 1;
	// ͼƬ
	private final int TYPE_SEND_IMAGE = 2;
	private final int TYPE_RECEIVER_IMAGE = 3;
	// λ��
	private final int TYPE_SEND_LOCATION = 4;
	private final int TYPE_RECEIVER_LOCATION = 5;
	// ����
	private final int TYPE_SEND_VOICE = 6;
	private final int TYPE_RECEIVER_VOICE = 7;

	DisplayImageOptions options;

	public MessageChatWifiAdapter(Context context, List<ChatMessage> list) {
		super(context, list);

		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.showImageOnFail(R.drawable.ic_launcher)
				.resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(300)).build();

	}

	@Override
	public int getItemViewType(int position) {
		ChatMessage msg = list.get(position);
		return msg.type;
	}

	private View createViewByType(ChatMessage message, int position) {
		int type = message.type;

		if (type == TYPE_RECEIVER_IMAGE) {// pic
			return mInflater.inflate(R.layout.item_chat_received_image, null);
		} else if (type == TYPE_SEND_IMAGE) {
			return mInflater.inflate(R.layout.item_chat_sent_image, null);
		} else if (type == TYPE_RECEIVER_LOCATION) {
			return mInflater.inflate(R.layout.item_chat_received_location, null);
		} else if (type == TYPE_SEND_LOCATION) {
			return mInflater.inflate(R.layout.item_chat_sent_location, null);
		} else if (type == TYPE_RECEIVER_VOICE) {
			return mInflater.inflate(R.layout.item_chat_received_voice, null);
		} else if (type == TYPE_SEND_VOICE) {
			return mInflater.inflate(R.layout.item_chat_sent_voice, null);
		} else if (type == TYPE_RECEIVER_TXT) {
			return mInflater.inflate(R.layout.item_chat_received_message, null);
		} else if (type == TYPE_SEND_TXT) {
			return mInflater.inflate(R.layout.item_chat_sent_message, null);
		}
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 8;
	}

	@Override
	public View bindView(int position, View convertView, ViewGroup parent) {

		final ChatMessage item = list.get(position);

		if (convertView == null) {
			convertView = createViewByType(item, position);
		}

		ImageView iv_avatar = ViewHolder.get(convertView, R.id.iv_avatar);
		final ImageView iv_fail_resend = ViewHolder.get(convertView, R.id.iv_fail_resend);//ʧ���ط�
		final TextView tv_send_status = ViewHolder.get(convertView, R.id.tv_send_status);//����״̬
		TextView tv_time = ViewHolder.get(convertView, R.id.tv_time);
		TextView tv_message = ViewHolder.get(convertView, R.id.tv_message);
		//ͼƬ
		ImageView iv_picture = ViewHolder.get(convertView, R.id.iv_picture);
		final ProgressBar progress_load = ViewHolder.get(convertView, R.id.progress_load);//������
		//λ��
		TextView tv_location = ViewHolder.get(convertView, R.id.tv_location);
		//����
		final ImageView iv_voice = ViewHolder.get(convertView, R.id.iv_voice);
		//��������
		final TextView tv_voice_length = ViewHolder.get(convertView, R.id.tv_voice_length);

		String avatar = item.mAvatar;
		if(avatar!=null && !avatar.equals("")){//����ͷ��-Ϊ�˲�ÿ�ζ�����ͷ��
			ImageLoader.getInstance().displayImage(avatar, iv_avatar, ImageLoadOptions.getOptions(),animateFirstListener);
		} else {
			iv_avatar.setImageResource(R.drawable.head);
		}

		iv_avatar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent =new Intent(mContext,SetMyInfoActivity.class);
				if(item.type == TYPE_RECEIVER_TXT 
						||item.type == TYPE_RECEIVER_IMAGE
				        ||item.type==TYPE_RECEIVER_LOCATION
				        ||item.type==TYPE_RECEIVER_VOICE){
					intent.putExtra("from", "other");
					intent.putExtra("username", item.user.getUsername());
				}else{
					intent.putExtra("from", "me");
				}
				mContext.startActivity(intent);
			}
		});
		
		tv_time.setText(TimeUtil.getChatTime(item.mMills));

		if(getItemViewType(position)==TYPE_SEND_TXT
//				||getItemViewType(position)==TYPE_SEND_IMAGE//ͼƬ��������
				
				||getItemViewType(position)==TYPE_SEND_LOCATION
				||getItemViewType(position)==TYPE_SEND_VOICE){//ֻ���Լ����͵���Ϣ�����ط�����
			//״̬����
 			{//���ͳɹ�
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				if(item.type == TYPE_SEND_VOICE){
					tv_send_status.setVisibility(View.GONE);
					tv_voice_length.setVisibility(View.VISIBLE);
				}else{
					tv_send_status.setVisibility(View.VISIBLE);
					tv_send_status.setText("�ѷ���");
				}
			}
		}

		final String text = item.msg ;
		
		return convertView;
	}

	
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
}
