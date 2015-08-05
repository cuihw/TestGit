package com.champion.mipi.ui.fragment;

import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.champion.mipi.R;
import com.champion.mipi.ui.FragmentBase;
import com.champion.mipi.weather.WeatherData;
import com.champion.mipi.wifiServices.ConnectService;

public class VideoFragment extends FragmentBase {

    private static final String TAG = "VideoFragment";

    private static final int UPDATE_TIMES = 0;

    WebView mWebView;

    private View mVideoFragment;

    private WeatherData mWeatherData;

    private TextView mTemperature;
    private TextView mMixData;
    private TextView mTimeTextview;
    private ImageView mWeatherImageView;

    private int millisUntilFinished = 4 * 60; // 秒
    
    private boolean isPause = false;

    private boolean mWeatherUpdated = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mVideoFragment = inflater.inflate(R.layout.fragment_video, container, false);

        mWebView = (WebView) mVideoFragment.findViewById(R.id.video_webView);

        Log.d(TAG, "VideoFragment");

        this.getActivity().setTitle("视频");

        initTrainArrive();

        return mVideoFragment;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mWebView.loadData("loading", "text/html", "UTF-8");
        isPause = true;
    }

    @Override
    public void onResume() {
        isPause = false;
        
        String youkuUrl = "http://m.youku.com/";

        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadData("loading", "text/html", "UTF-8");

        mWebView.getSettings().setPluginState(PluginState.ON);

        WebSettings settings = mWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setPluginState(PluginState.ON);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);

        settings.setDefaultTextEncodingName("UTF-8");
        mWebView.setBackgroundColor(0);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.setWebViewClient(new VideoClient());

        mWebView.loadUrl(youkuUrl);

        mTemperature = (TextView) findViewById(R.id.temp_textview);
        mMixData = (TextView) findViewById(R.id.mixdata_textview);
        mTimeTextview = (TextView) findViewById(R.id.time_textview);
        mWeatherImageView = (ImageView) findViewById(R.id.weather_icon);

        super.onResume();
    }

    private void initTrainArrive() {

        Message msg = timeHandler.obtainMessage(UPDATE_TIMES);
        timeHandler.sendMessageDelayed(msg, 1000);
    }


    private void getWeatherData() {
        if (mWeatherUpdated) {
            return;
        }
        mWeatherData = ConnectService.getInstence().getWeatherData();
        if (mWeatherData != null) {

            String temperature = mWeatherData.getTemperature();
            List<String> mixData = mWeatherData.getMixData();
            int resid = mWeatherData.getIconResid();

            if (!TextUtils.isEmpty(temperature)) {

                updateWeatherUI(temperature, mixData, resid);
            }
        }
    }

    private void updateWeatherUI(String temperature, List<String> mixData, int resId) {

        if (!TextUtils.isEmpty(temperature)) {
            String temp = "北京 气温： " + temperature;
            mTemperature.setText(temp);
        } else {
            return;
        }

        if (mixData != null) {
            String mixdataArray = "";
            for (int i = 0; i < mixData.size(); i++) {
                if (i == mixData.size() - 1) {
                    mixdataArray = mixdataArray + mixData.get(i);
                } else {
                    mixdataArray = mixdataArray + mixData.get(i) + ", ";
                }
            }

            mMixData.setText(mixdataArray);
        }else {
            return;
        }

        if (resId != 0) {
            mWeatherImageView.setImageResource(resId);
            mWeatherUpdated  = true;
        } else {
            mWeatherImageView.setImageResource(R.drawable.n00);
        }

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    class VideoClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    public boolean webViewGoBack() {

        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    Handler timeHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_TIMES) {
                if (!isPause) {
                    millisUntilFinished--;
                    if (millisUntilFinished < 0) {
                        millisUntilFinished = 4 * 60;
                    }
                    String time = getTimeFormat(millisUntilFinished);
                    mTimeTextview.setText(time + " 秒后到站");

                    getWeatherData();
                }

                Message message = timeHandler.obtainMessage(UPDATE_TIMES);
                timeHandler.sendMessageDelayed(message, 1000);
            }
        }
    };

    protected String getTimeFormat(int millis) {
        int minute = millis / 60;
        int second = millis % 60;
        String time = minute + ":" + second;
        return time;
    }
}
