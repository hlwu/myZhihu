package com.hlwu.myapp.ui.dailynewcontent;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.hlwu.myapp.R;
import com.hlwu.myapp.news.StoriesContent;
import com.hlwu.myapp.presenter.DailyNewsFragmentPresenter;
import com.hlwu.myapp.utils.HtmlUtil;
import com.hlwu.myapp.utils.NetUtil;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by hlwu on 1/15/18.
 */

public class DailyNewsContentActivity extends AppCompatActivity {

    private static final String TAG = "DailyNewsContentActivity";
    private static final String URL = "https://news-at.zhihu.com/api/4/news/";

    private static UnlimitedDiskCache mUnlimitedDiskCache;
    private DailyNewsContentTitileCardLayout mTitleCard;
    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daily_news_content);

        initImageLoader(this);

        mTitleCard = (DailyNewsContentTitileCardLayout) findViewById(R.id.title_card);
        mTitleCard.getmImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);

        mWebView = (WebView) findViewById(R.id.news_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        new Thread(mNetRunnable).start();

        Toolbar toolbar = (Toolbar) findViewById(R.id.content_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("asadasdasdasdadsas");
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        }
    }

    public static void initImageLoader(Context context) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.foreground_op1)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        mUnlimitedDiskCache = new UnlimitedDiskCache(new File(DailyNewsFragmentPresenter.ICON_CACHE_PATH));
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options)
                .denyCacheImageMultipleSizesInMemory()
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .threadPoolSize(3)
                .memoryCache(new WeakMemoryCache())
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheFileCount(100)
                .diskCache(mUnlimitedDiskCache)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0); //exit this process, to avoid webview memory leak
    }
}
