package com.champion.mipis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.champion.mipis.services.ConnectService;
import com.champion.mipis.splash.DragFlashLayout;
import com.champion.mipis.splash.OnViewChangeListener;

public class SplashActivity extends Activity implements OnViewChangeListener {

    protected static final String TAG = "SplashActivity";
    private DragFlashLayout mDragFlashLayout;
    private ImageView[] imgs;
    private int count;
    private int currentItem;

    private LinearLayout pointLLayout;
    private LinearLayout leftLayout;
    private LinearLayout rightLayout;

    TextView mStart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_activity);
        initView();
        startServices();
    }

    private void initView() {
        mDragFlashLayout = (DragFlashLayout) findViewById(R.id.ScrollLayout);
        pointLLayout = (LinearLayout) findViewById(R.id.llayout);

        leftLayout = (LinearLayout) findViewById(R.id.leftLayout);
        rightLayout = (LinearLayout) findViewById(R.id.rightLayout);
        count = mDragFlashLayout.getChildCount();
        imgs = new ImageView[count];
        for (int i = 0; i < count; i++) {
            imgs[i] = (ImageView) pointLLayout.getChildAt(i);
            imgs[i].setEnabled(true);
            imgs[i].setTag(i);
        }
        currentItem = 0;
        imgs[currentItem].setEnabled(false);
        mDragFlashLayout.SetOnViewChangeListener(this);
        mStart = (TextView)findViewById(R.id.start_textview);
        mStart.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                startApps();
            }});
    }

    @SuppressLint("ResourceAsColor")
    private void startApps() {
        leftLayout.setVisibility(View.GONE);
        rightLayout.setVisibility(View.GONE);
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
        overridePendingTransition(R.anim.zoom_out_enter, R.anim.zoom_out_exit);
    }

    @Override
    public void OnViewChange(int position) {
        setcurrentPoint(position);
    }

    private void setcurrentPoint(int position) {

        if (position < 0 || position > count - 1 || currentItem == position) {
            return;
        }
        imgs[currentItem].setEnabled(true);
        imgs[position].setEnabled(false);
        currentItem = position;
        if (position == count - 1) {
            mStart.setText("开  始");
        }
    }

    private void startServices() {
        Intent intentStartServices = new Intent();
        intentStartServices.setClass(this, ConnectService.class);
        Log.d(TAG, "login startServices..............");
        startService(intentStartServices);
    }
}
