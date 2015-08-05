package com.champion.mipi.ui;

import com.champion.mipi.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class EmergencyActivity extends Activity {

    TextView mEmergencyView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency);

        Intent intent = getIntent();

        String message = intent.getStringExtra("message");
        
        mEmergencyView = (TextView)findViewById(R.id.text_emergency);
        mEmergencyView.setText(message);

        //mHandler.sendEmptyMessageDelayed(0, 2000);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            EmergencyActivity.this.finish();
        }
    };
}
