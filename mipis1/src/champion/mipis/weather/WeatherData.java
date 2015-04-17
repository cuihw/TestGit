package champion.mipis.weather;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import champion.mipis.R;

public class WeatherData {

    private static final String TAG = "WeatherData";

    public static final String WEATHER_UPDATE = "com.champion.mipis.WEATHER_UPDATE";

    private Document mDocument;

    private Element mBody;

    private HashMap<String, Integer> weatherIcons = new HashMap<String, Integer> ();

    private int mRetry = 5;

    private static final int FAILED_UPDATE = 0;

    private static final int UPDATE = 1;

    private static final int UPDATE_CMD = 2;

    private List<String> mMixData = new ArrayList<String>();

    private String mTemperature;
    // beijing
    private String webHttp = "http://m.weather.com.cn/mweather/101010100.shtml";

    private Context mContext;
    
    private String mIconName;

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case UPDATE_CMD:
                    updateWeatherData();

                    mHandler.removeMessages(UPDATE_CMD);
                    Message msg1 = mHandler.obtainMessage(UPDATE_CMD);
                    mHandler.sendMessageDelayed(msg1, 60 * 60 * 1000);
                    break;
                    
            }

        }};
    
    public String getTemperature() {
        return mTemperature;
    }

    public List<String> getMixData() {
        return mMixData;
    }
    
    public String getIconName () {
        return mIconName;
    }
    
    public int getIconResid() {
        return weatherIcons.get(mIconName);
    }

    public WeatherData(Context context) {
        mContext = context;
        initWeatherMap();
        updateWeatherData();

        Message msg = mHandler.obtainMessage(UPDATE_CMD);
        mHandler.sendMessageDelayed(msg, 60 * 60 * 1000);
    }

    private void updateWeatherData() {
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

        // background-image:
        // url("http://i.tq121.com.cn/i/wap/index390/n53.jpg");

        /*
         * <div style=
         * "background-image: url(&quot;http://i.tq121.com.cn/i/wap/index390/n53.jpg&quot;);"
         * class="sk"> <h1><i><a href="#"
         * class="fen bsh-mobile-btn bmstyle-button"><img
         * src="http://i.tq121.com.cn/i/wap/fx.png" alt="分享" width="20"></a></i>
         * <span><a href="/manage/citmani.html" title="更换城市">北京</a> |<a
         * href="/manage/citmani.html" class="city"
         * title="更换城市">更换城市</a></span></h1>
         * 
         * <table border="0" width="98%"> <tbody><tr> <td width="20%"><img
         * src="http://i.tq121.com.cn/i/wap/80bai/n53.png" alt="霾"
         * width="70"></td> <td class="wd" width="50%">9℃</td> <td
         * width="30%"><span>霾</span><span>无持续风向</span><span>微风</span></td>
         * </tr> </tbody></table> <h2>04月08日（周三）<span>19:55 更新</span></h2>
         * <h3><a href="/mairport/101010100.shtml"
         * style="border-right:1px solid #93a600;" title="北京机场天气">机场天气</a><a
         * href="/mweather1d/101010100.shtml" title="北京今日详情">今日详情</a></h3>
         * </div>
         */

        elements = mBody.getElementsByClass("sk");

        Element skBody = elements.first();

        String skBodyStyle = skBody.toString(); // .attr("style");
        int index = skBodyStyle.indexOf("background-image:");
        String background = null;

        if (index != -1) {
            index = skBodyStyle.indexOf("http:");

            if (index != -1)
                background = skBodyStyle.substring(index);

            index = background.indexOf("&quot;");

            if (index != -1)
                background = background.substring(0, index);

            Log.d(TAG, "background = " + background);
        }
        Element table = skBody.getElementsByTag("table").first();

        Elements tableRaws = table.getElementsByTag("td");

        if (tableRaws.size() > 2) {
            // get icon index 
            mIconName = getIconIndex(tableRaws.get(0));
            
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

    private String getIconIndex(Element element) {
        // TODO Auto-generated method stub
        Elements imgElements= element.getElementsByTag("img");
        Element imgEle = imgElements.first();
        String imgsrc = imgEle.attr("abs:src");
        Log.d(TAG, "getIconIndex imgsrc = " + imgsrc);
        //http://i.tq121.com.cn/i/wap/80bai/n53.png
        int start = imgsrc.lastIndexOf("/") + 1;
        int end = imgsrc.lastIndexOf(".");
        String fileName = null;
        if (start != -1 && end != -1) {
            fileName = imgsrc.substring(start, end);            
        }

        Log.d(TAG, "getIconIndex fileName = " + fileName);
        return fileName;
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

}
