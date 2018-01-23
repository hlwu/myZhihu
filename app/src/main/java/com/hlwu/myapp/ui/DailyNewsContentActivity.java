package com.hlwu.myapp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.hlwu.myapp.R;
import com.hlwu.myapp.news.StoriesContent;
import com.hlwu.myapp.utils.HtmlUtil;
import com.hlwu.myapp.utils.NetUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.LinkedList;

/**
 * Created by hlwu on 1/15/18.
 */

public class DailyNewsContentActivity extends Activity {

    private static final String TAG = "DailyNewsContentActivity";
    private static final String URL = "https://news-at.zhihu.com/api/4/news/";

    private DailyNewsContentTitileCardLayout mTitleCard;
    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daily_news_content);

        mTitleCard = (DailyNewsContentTitileCardLayout) findViewById(R.id.title_card);
        mTitleCard.getmImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);

        mWebView = (WebView) findViewById(R.id.news_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        new Thread(mNetRunnable).start();
    }

    private Runnable mNetRunnable = new Runnable() {
        @Override
        public void run() {
            int storyId = getIntent().getIntExtra("id", 0);
            String newsJson = NetUtil.get(URL + storyId);     //download news
            Gson gson = new Gson();
            StoriesContent news = gson.fromJson(newsJson, StoriesContent.class);

            if (news != null) {
                Message msg = mUiHandler.obtainMessage(1);
                msg.obj = news;
                mUiHandler.sendMessage(msg);
            }

        }
    };

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    StoriesContent sc = (StoriesContent) msg.obj;
                    String mime = "text/html; charset=utf-8";
                    String encoding = "utf-8";
                    String html = sc.getBody();
                    LinkedList<String> css = new LinkedList<>();
                    css.add(sc.getCss()[0]);
                    String htmlData = HtmlUtil.createHtmlData(html, css, new LinkedList<String>());
                    mWebView.loadData(htmlData, mime, encoding);

                    mTitleCard.getmTitleText().setText(sc.getTitle());
                    mTitleCard.getmImageSource().setText(sc.getImage_source());
                    ImageLoader.getInstance().displayImage(sc.getImage(), mTitleCard.getmImageView());
                    break;
            }
        }
    };

}
