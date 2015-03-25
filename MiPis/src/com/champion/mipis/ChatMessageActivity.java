package com.champion.mipis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.champion.mipis.services.ConnectService;
import com.champion.mipis.util.Constant;
import com.champion.mipis.util.LocalMessage;
import com.champion.mipis.util.Person;

@SuppressLint("NewApi")
public class ChatMessageActivity extends Activity {

    protected static final String TAG = "ChatMessageActivity";

    private static final int PICKUP_PICTURE = 1;

    ListView mListView;

    ConnectService mConnectService;

    Intent mServiceIntent;

    Map<Integer, List<LocalMessage>> mMsgContainerMap;

    List<LocalMessage> mMessageList;

    private Person mPerson = null;

    private Person me = null;

    Button mSendButton;

    EditText mEditText;

    MessageListAdapter mMessageListAdapter;

    MessageChangeReceviver mMessageChangeReceviver;

    IntentFilter mIntentFilter;

    ImageView mPlugFileImage;

    LinearLayout mAddMediaFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chat_message);

        initConnectServer();

        Intent intent = getIntent();

        mPerson = (Person) intent.getExtras().getSerializable("person");

        me = (Person) intent.getExtras().getSerializable("me");

        setTitle(mPerson.personNickeName);

        mSendButton = (Button) findViewById(R.id.send_message_btn);
        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                sendMessage();
            }
        });

        mEditText = (EditText) findViewById(R.id.message_edittext);
        
        mEditText.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                mAddMediaFile.setVisibility(View.GONE);
            }});

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(mOnItemClickListener);

        mPlugFileImage = (ImageView) findViewById(R.id.plug_file);

        mPlugFileImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showPlugFiles();
            }
        });

        mAddMediaFile = (LinearLayout)findViewById(R.id.plug_file_layout); 
        mAddMediaFile.setVisibility(View.GONE);
    }
    
    OnItemClickListener mOnItemClickListener = new OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            if (mMessageList !=null && mMessageList.size() > arg2) {
                LocalMessage message = mMessageList.get(arg2);
                if (message.type == LocalMessage.PIC) {
                    Intent it = new Intent(Intent.ACTION_VIEW); 
                    Log.d(TAG, "open file: " + message.msg);
                    Uri uri = Uri.parse("file://" + message.msg); 
                    it.setDataAndType(uri, "image/*"); 
                    startActivity(it); 
                }
            }
        }};

    @SuppressLint("NewApi")
    public void selectFile(View view) {
        if (view.getId() == R.id.plugImage_layout) {
            // pick image
//            Intent intent = new Intent(Intent.ACTION_PICK, null);
//            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    "image/*");
            Intent intent=new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);  

            startActivityForResult(intent, PICKUP_PICTURE);
        }
    }

    private void showPlugFiles() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (mAddMediaFile.isShown()) {
            mAddMediaFile.setVisibility(View.GONE);
        } else {
            mAddMediaFile.setVisibility(View.VISIBLE);
        }

    }

    private void registerReceiver() {
        mMessageChangeReceviver = new MessageChangeReceviver();

        mIntentFilter = new IntentFilter();

        mIntentFilter.addAction(Constant.hasMsgUpdatedAction);
        this.registerReceiver(mMessageChangeReceviver, mIntentFilter);
    }


    private void unRegisterReceiver() {
        unregisterReceiver(mMessageChangeReceviver);
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

    private void sendMessage() {
        if (mConnectService != null) {
            String msg = mEditText.getText().toString();
            if (!TextUtils.isEmpty(msg)) {
                mConnectService.sendMsg(mPerson.personId, msg);
                mEditText.setText("");
            }
        }
    }

    private void updateListView() {

        if (mConnectService != null) {
            mMsgContainerMap = mConnectService.getMsgContainer();
            mMessageList = mMsgContainerMap.get(mPerson.personId);

            mMessageListAdapter = new MessageListAdapter();
            mListView.setAdapter(mMessageListAdapter);
            if (mMessageList != null) {
                mListView.setSelection(mMessageList.size());
            }
        }
    }

    /**
     * ConnectService bind to activity connect.
     */
    private ServiceConnection sConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mConnectService = ((ConnectService.ServiceBinder) service).getService();
            Log.d(TAG, "Service connected to activity...");
            updateListView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mConnectService = null;
            Log.d(TAG, "Service disconnected to activity...");
        }
    };

    private void initConnectServer() {
        Log.d(TAG, "Service disconnected to activity...");
        mServiceIntent = new Intent(this, ConnectService.class);
        bindService(mServiceIntent, sConnection, BIND_AUTO_CREATE);
        startService(mServiceIntent);
    }

    private class MessageListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        @Override
        public int getCount() {
            if (mMessageList != null) {
                return mMessageList.size();
            }
            return 0;
        }

        public MessageListAdapter() {
            mInflater = LayoutInflater.from(ChatMessageActivity.this);
        }

        @Override
        public Object getItem(int arg0) {

            return mMessageList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }


        @Override
        public int getItemViewType(int position) {
            LocalMessage message = mMessageList.get(position);
            return message.fromTo;            
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            ViewHolder holder;
            Log.d(TAG, "getView() message " + arg0 );
            if (mMessageList.size() > arg0) {
                LocalMessage message = mMessageList.get(arg0);
                Log.d(TAG, "getView() message " + arg0 + " : " + message + ", view count" + getCount());

                if (arg1 == null) {
                    holder = new ViewHolder();
                    if (message.fromTo == LocalMessage.FROM) {
                        arg1 = mInflater.inflate(R.layout.reveive_msg, null);
                        holder.fromto = message.fromTo;
                    } else {
                        arg1 = mInflater.inflate(R.layout.send_msg, null);
                        holder.fromto = message.fromTo;
                    }

                    holder.headImageView = (ImageView) arg1.findViewById(R.id.sended_head_imageview);
                    holder.timeTextView = (TextView) arg1.findViewById(R.id.time_stamp);
                    holder.msgTextView = (TextView) arg1.findViewById(R.id.sended_text_view);
                    holder.msg_image_view = (ImageView) arg1.findViewById(R.id.msg_image_view);
                    arg1.setTag(holder);

                } else {
                    holder = (ViewHolder) arg1.getTag();
                    Log.d(TAG, "holder is " + ((holder.fromto == LocalMessage.FROM) ? "received" : "send"));
                }


                if (message.fromTo == LocalMessage.FROM) {
                    holder.headImageView.setImageResource(mPerson.personHeadIconId);
                } else {
                    holder.headImageView.setImageResource(me.personHeadIconId);
                }

                holder.timeTextView.setText(message.getTimeString());

                Log.d(TAG, "message.type " + " : " + message.type);
                if (message.type == LocalMessage.TEXT) {
                    Log.d(TAG, "message.type == Message.TEXT");
                    holder.msg_image_view.setVisibility(View.GONE);
                    holder.msgTextView.setVisibility(View.VISIBLE);
                    holder.msgTextView.setText(message.msg);
                } else if (message.type == LocalMessage.PIC) {
                    Log.d(TAG, "message.type == Message.PIC");
                    holder.msg_image_view.setVisibility(View.VISIBLE);
                    holder.msgTextView.setVisibility(View.GONE);
                    Bitmap bitmap = getBitmap(message.msg);
                    holder.msg_image_view.setImageBitmap(bitmap);
                }
            }

            return arg1;
        }

        public final class ViewHolder {
            public ImageView headImageView;
            public TextView timeTextView;
            public TextView msgTextView;
            public int fromto;
            public ImageView msg_image_view;
        }
    }

    @Override
    protected void onResume() {
        updateListView();
        registerReceiver();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unRegisterReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (mConnectService != null) {
            unbindService(sConnection);
        }
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == PICKUP_PICTURE) {
            Uri uri = data.getData();
            String filename = getRealPathFromURI(uri);
            
            mConnectService.sendFile(mPerson.personId, filename);
            
        }
    }
    @SuppressLint("NewApi")
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    final int IMAGE_MAX_SIZE = 64;

    private Bitmap getBitmap(String path) {

        Uri uri = getImageUri(path);
        InputStream in = null;
        ContentResolver mContentResolver = ChatMessageActivity.this.getContentResolver();
        try {
            in = mContentResolver.openInputStream(uri);

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = mContentResolver.openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            in.close();

            return b;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "file " + path + " not found");
        } catch (IOException e) {
            Log.e(TAG, "file " + path + " not found");
        }
        return null;
    }

    private Uri getImageUri(String path) {

        return Uri.fromFile(new File(path));
    }
}
