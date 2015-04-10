package com.champion.mipi.weather;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WeatherData {
    private static final String TAG = "WeatherData";

    public static final String WEATHER_UPDATE = "com.champion.mipis.WEATHER_UPDATE";

    private Document mDocument;

    private Element mBody;

    private Handler mHandler;

    private int mRetry = 5;

    private int FAILED_UPDATE = 0;

    private int UPDATE = 1;
    
    private List<String> mMixData = new ArrayList<String>();
    
    private String mTemperature;
    // beijing
    private String webHttp = "http://m.weather.com.cn/mweather/101010100.shtml";

    private Context mContext;

    public String getTemperature () {
        return mTemperature;
    }

    public List<String> getMixData() {
        return mMixData;
    }

    public WeatherData(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;

        new Thread(new Runnable() {

            @Override
            public void run() {
                mDocument = getWebpageDoc(webHttp);

                parserDocument(mDocument);
            }

        }).start();
    }

    private void parserDocument(Document doc) {
        if (doc == null) {
            if (mRetry > 0) {
                mRetry--;
                Log.d(TAG, "retry to get the data.");
                getWebpageDoc(webHttp);

            } else {
                if (mHandler != null) {
                    Message msg = mHandler.obtainMessage(FAILED_UPDATE);
                    mHandler.sendMessage(msg);
                }
            }
            return;
        }

        Elements elements = doc.getElementsByTag("body");
        mBody = elements.first();

        Log.d(TAG, "mBody = " + mBody.toString());
        
        //background-image: url("http://i.tq121.com.cn/i/wap/index390/n53.jpg");
        
        /*
<div style="background-image: url(&quot;http://i.tq121.com.cn/i/wap/index390/n53.jpg&quot;);" class="sk">
<h1><i><a href="#" class="fen bsh-mobile-btn bmstyle-button"><img src="http://i.tq121.com.cn/i/wap/fx.png" alt="分享" width="20"></a></i>
<span><a href="/manage/citmani.html" title="更换城市">北京</a>
|<a href="/manage/citmani.html" class="city" title="更换城市">更换城市</a></span></h1>

<table border="0" width="98%">
<tbody><tr>
<td width="20%"><img src="http://i.tq121.com.cn/i/wap/80bai/n53.png" alt="霾" width="70"></td>
<td class="wd" width="50%">9℃</td>
<td width="30%"><span>霾</span><span>无持续风向</span><span>微风</span></td>
</tr>
</tbody></table>
<h2>04月08日（周三）<span>19:55 更新</span></h2>
<h3><a href="/mairport/101010100.shtml" style="border-right:1px solid #93a600;" title="北京机场天气">机场天气</a><a href="/mweather1d/101010100.shtml" title="北京今日详情">今日详情</a></h3>
</div>  
        */

        elements = mBody.getElementsByClass("sk");
        
        Element skBody = elements.first();


        String skBodyStyle = skBody.toString(); //.attr("style");
        int index = skBodyStyle.indexOf("background-image:");
        String background = null;
        if (index != -1) {
            index = skBodyStyle.indexOf("http:");

            if (index != -1) background = skBodyStyle.substring(index);

            index = background.indexOf("&quot;");

            if (index != -1) background = background.substring(0, index);

            Log.d(TAG, "background = " + background);
        }
        Element table = skBody.getElementsByTag("table").first();

        Elements tableRaws = table.getElementsByTag("td");

        if (tableRaws.size() > 2) {
            mTemperature = tableRaws.get(1).text();
            Log.d(TAG, "mTemperature = " + mTemperature);

            Elements mixDatas = tableRaws.get(2).getElementsByTag("span");
            mMixData.clear();
            for (int i = 0; i < mixDatas.size(); i++) {
                mMixData.add(mixDatas.get(i).text());

                Log.d(TAG, "mixData = " + mixDatas.get(i).text());
            }
        }
        notifyDataUpdate(UPDATE);
        

    }

    private void notifyDataUpdate(int UPDATE) {
            Log.d(TAG, "notifyDataUpdate...............");
            Intent intent = new Intent();
            intent.setAction(WEATHER_UPDATE);
            mContext.sendBroadcast(intent);
    }

    private Document getWebpageDoc(String url) {

        Document doc = null;
        try {
            doc = Jsoup.connect(url).timeout(5000).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

}
