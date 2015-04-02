package com.champion.mipi.ui.fragment;

import com.champion.mipi.R;
import com.champion.mipi.ui.FragmentBase;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class VideoFragment extends FragmentBase {

    private static final String TAG = "VideoFragment";

    WebView mWebView;

    private View mVideoFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mVideoFragment = inflater.inflate(R.layout.video_fragment, container, false);

        mWebView = (WebView) mVideoFragment.findViewById(R.id.video_webView);

        Log.d(TAG, "VideoFragment");
        this.getActivity().setTitle("สำฦต");
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
        super.onResume();
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
}
