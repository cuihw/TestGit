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
    private HashMap<String, Integer> weatherIcons = new HashMap<String, Integer> ();

    private int millisUntilFinished = 4 * 60; // 秒

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mVideoFragment = inflater.inflate(R.layout.fragment_video, container, false);

        mWebView = (WebView) mVideoFragment.findViewById(R.id.video_webView);

        Log.d(TAG, "VideoFragment");
        this.getActivity().setTitle("视频");
        return mVideoFragment;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mWebView.loadData("loading", "text/html", "UTF-8");
    }

    @Override
    public void onResume() {

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
        
        initTrainArrive();
        super.onResume();
    }

    private void initTrainArrive() {

        initWeatherMap();
        Message msg = timeHandler.obtainMessage(UPDATE_TIMES);
        timeHandler.sendMessageDelayed(msg, 1000);
    }

    private void initWeatherMap() {
        // TODO Auto-generated method stub

        weatherIcons.put("d00", R.drawable.d00);
        weatherIcons.put("d01", R.drawable.d01);
        weatherIcons.put("d02", R.drawable.d02);
        weatherIcons.put("d03", R.drawable.d03);
        weatherIcons.put("d04", R.drawable.d04);
        weatherIcons.put("d05", R.drawable.d05);
        weatherIcons.put("d06", R.drawable.d06);
        weatherIcons.put("d07", R.drawable.d07);
        weatherIcons.put("d08", R.drawable.d08);
        weatherIcons.put("d09", R.drawable.d09);
        weatherIcons.put("d10", R.drawable.d10);
        weatherIcons.put("d11", R.drawable.d11);
        weatherIcons.put("d12", R.drawable.d12);
        weatherIcons.put("d13", R.drawable.d13);
        weatherIcons.put("d14", R.drawable.d14);
        weatherIcons.put("d15", R.drawable.d15);
        weatherIcons.put("d16", R.drawable.d16);
        weatherIcons.put("d17", R.drawable.d17);
        weatherIcons.put("d18", R.drawable.d18);
        weatherIcons.put("d19", R.drawable.d19);
        weatherIcons.put("d20", R.drawable.d20);
        weatherIcons.put("d21", R.drawable.d21);
        weatherIcons.put("d22", R.drawable.d22);
        weatherIcons.put("d23", R.drawable.d23);
        weatherIcons.put("d24", R.drawable.d24);
        weatherIcons.put("d25", R.drawable.d25);
        weatherIcons.put("d26", R.drawable.d26);
        weatherIcons.put("d27", R.drawable.d27);
        weatherIcons.put("d28", R.drawable.d28);
        weatherIcons.put("d29", R.drawable.d29);
        weatherIcons.put("d30", R.drawable.d30);
        weatherIcons.put("d31", R.drawable.d31);
        weatherIcons.put("d32", R.drawable.d32);
        weatherIcons.put("d33", R.drawable.d33);
        weatherIcons.put("d49", R.drawable.d49);
        weatherIcons.put("d53", R.drawable.d53);
        weatherIcons.put("d54", R.drawable.d54);
        weatherIcons.put("d55", R.drawable.d55);
        weatherIcons.put("d56", R.drawable.d56);
        weatherIcons.put("d58", R.drawable.d58);
        weatherIcons.put("d58", R.drawable.d58);

        weatherIcons.put("n00", R.drawable.n00);
        weatherIcons.put("n01", R.drawable.n01);
        weatherIcons.put("n02", R.drawable.n02);
        weatherIcons.put("n03", R.drawable.n03);
        weatherIcons.put("n04", R.drawable.n04);
        weatherIcons.put("n05", R.drawable.n05);
        weatherIcons.put("n06", R.drawable.n06);
        weatherIcons.put("n07", R.drawable.n07);
        weatherIcons.put("n08", R.drawable.n08);
        weatherIcons.put("n09", R.drawable.n09);
        weatherIcons.put("n10", R.drawable.n10);
        weatherIcons.put("n11", R.drawable.n11);
        weatherIcons.put("n12", R.drawable.n12);
        weatherIcons.put("n13", R.drawable.n13);
        weatherIcons.put("n14", R.drawable.n14);
        weatherIcons.put("n15", R.drawable.n15);
        weatherIcons.put("n16", R.drawable.n16);
        weatherIcons.put("n17", R.drawable.n17);
        weatherIcons.put("n18", R.drawable.n18);
        weatherIcons.put("n19", R.drawable.n19);
        weatherIcons.put("n20", R.drawable.n20);
        weatherIcons.put("n21", R.drawable.n21);
        weatherIcons.put("n22", R.drawable.n22);
        weatherIcons.put("n23", R.drawable.n23);
        weatherIcons.put("n24", R.drawable.n24);
        weatherIcons.put("n25", R.drawable.n25);
        weatherIcons.put("n26", R.drawable.n26);
        weatherIcons.put("n27", R.drawable.n27);
        weatherIcons.put("n28", R.drawable.n28);
        weatherIcons.put("n29", R.drawable.n29);
        weatherIcons.put("n30", R.drawable.n30);
        weatherIcons.put("n31", R.drawable.n31);
        weatherIcons.put("n32", R.drawable.n32);
        weatherIcons.put("n33", R.drawable.n33);
        weatherIcons.put("n49", R.drawable.n49);
        weatherIcons.put("n53", R.drawable.n53);
        weatherIcons.put("n54", R.drawable.n54);
        weatherIcons.put("n55", R.drawable.n55);
        weatherIcons.put("n56", R.drawable.n56);
        weatherIcons.put("n58", R.drawable.n58);
        weatherIcons.put("n58", R.drawable.n58);
    }

    private void getWeatherData() {

        mWeatherData = ConnectService.getInstence().getWeatherData();
        if (mWeatherData != null) {

            String temperature = mWeatherData.getTemperature();
            List<String> mixData = mWeatherData.getMixData();
            String micon = mWeatherData.getIconName();
            Log.d(TAG, "micon = " + micon);
            if (!TextUtils.isEmpty(temperature) && !TextUtils.isEmpty(micon)) {
                int resid = weatherIcons.get(micon);
                updateWeatherUI(temperature, mixData, resid);
            }
        }
    }

    private void updateWeatherUI(String temperature, List<String> mixData, int resId) {

        if (!TextUtils.isEmpty(temperature)) {
            String temp = "北京   气温： " + temperature;
            mTemperature.setText(temp);
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
        }
        mWeatherImageView.setImageResource(resId);
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
                millisUntilFinished--;
                if (millisUntilFinished < 0) {
                    millisUntilFinished = 4 * 60;
                }
                String time = getTimeFormat(millisUntilFinished);
                mTimeTextview.setText(time + " 秒后到站");

                Message message = timeHandler.obtainMessage(UPDATE_TIMES);
                timeHandler.sendMessageDelayed(message, 1000);
                getWeatherData();
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
