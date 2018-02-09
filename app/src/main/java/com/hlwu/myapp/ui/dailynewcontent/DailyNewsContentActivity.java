package com.hlwu.myapp.ui.dailynewcontent;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.github.xiaozhucdj.sildbacklibrary.SlideBaseActivity;
import com.google.gson.Gson;
import com.hlwu.myapp.R;
import com.hlwu.myapp.db.DBHelper;
import com.hlwu.myapp.db.DailyNewsContentDBManager;
import com.hlwu.myapp.news.StoriesContent;
import com.hlwu.myapp.utils.HtmlUtil;
import com.hlwu.myapp.utils.NetUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.LinkedList;

/**
 * Created by hlwu on 1/15/18.
 */

public class DailyNewsContentActivity extends SlideBaseActivity {

    private static final String TAG = "DailyNewsContentActivity";
    private static final String URL = "https://news-at.zhihu.com/api/4/news/";

    private DailyNewsContentTitileCardLayout mTitleCard;
    private WebView mWebView;
    private DailyNewsContentDBManager mDailyNewsContentDBManager;

    private static final String TASK_SAVE_DAILYNEWS_CONTENT = "save_dailynews_content";
    private static final String TASK_LOAD_DAILYNEWS_CONTENT = "load_dailynews_content";

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

            //insert or update to daily news content table start
            (new DBTask()).execute(TASK_SAVE_DAILYNEWS_CONTENT, newsJson);
            //insert or update to daily news content table end

            Log.d("flaggg", "query: " + mDailyNewsContentDBManager.queryById(news.getId()));

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

    @Override
    public int setContentViewId() {
        return R.layout.daily_news_content;
    }

    @Override
    public void initView(View contentView) {
        mTitleCard = (DailyNewsContentTitileCardLayout) contentView.findViewById(R.id.title_card);
        mTitleCard.getmImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);

        mWebView = (WebView) contentView.findViewById(R.id.news_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        new Thread(mNetRunnable).start();

        Toolbar toolbar = (Toolbar) contentView.findViewById(R.id.content_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setTitle(R.string.toolbar_news_content);
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        }

        mDailyNewsContentDBManager = DailyNewsContentDBManager.getInstance(this);
    }

    private class DBTask extends AsyncTask<String, Integer, Boolean> {
        @SuppressLint("LongLogTag")
        @Override
        protected Boolean doInBackground(String... strings) {
            Log.d(TAG, "doInBackground  strings[0]: " + strings[0]);
            if (strings[0] != null) {
                switch (strings[0]) {
                    case TASK_SAVE_DAILYNEWS_CONTENT:
                        if (strings[1] == null) {
                            Log.d(TAG, "do save daily news task failed!");
                            return null;
                        }

                        Gson gson = new Gson();
                        StoriesContent news = gson.fromJson(strings[1], StoriesContent.class);

                        //insert to daily_news_content table
                        ContentValues dailyNewsContentCV = new ContentValues();
                        dailyNewsContentCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT_ID, news.getId());
                        dailyNewsContentCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT_IMAGE, news.getImage());
                        dailyNewsContentCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT_ISREADED, 1);
                        mDailyNewsContentDBManager.insert(dailyNewsContentCV);
                        break;

                    case TASK_LOAD_DAILYNEWS_CONTENT:
//                        if (mImageLoader == null) {
//                            mImageLoader = ImageLoader.getInstance();
//                        }
//
//                        Cursor c = mDailyNewsDBManager.queryAll();
//                        if (c != null && c.getCount() > 0) {
//                            mTheShowingContentIsFromNetwork = false;
//                            while (c.moveToNext()) {
//                                String detial = c.getString(c.getColumnIndex(DBHelper.MYZHIHU_TABLE_DAILYNEWS_DETAILS));
//                                DailyStructure news = (new Gson()).fromJson(detial, DailyStructure.class);
//                                Time today = new Time();
//                                today.setToNow();
//                                if (news.isTopStoriesUpdated(mNewsMap.get(today.format("%Y%m%d")))) {
//                                    Message msg = mUiHandler.obtainMessage(MSG_TOP_STORIES_UPDATED);
//                                    msg.obj = news;
//                                    mUiHandler.sendMessage(msg);
//                                }
//                                mNewsMap.put(news.getDate(), news);
//                                Message msg = mUiHandler.obtainMessage(MSG_GET_NEWS_DONE);
//                                msg.obj = news;
//                                mUiHandler.sendMessage(msg);
//                                mOldestNews = news;
//
//                                Stories[] stories = news.getStories();
//                                for (final Stories story : stories) {
//                                    mTheStoriesWhichDidNotAddIcon.put(story.getImages()[0], story);
//                                    ImageSize targetSize = new ImageSize(100, 100); // result Bitmap will be fit to this size
//                                    //we download the images before, so there should load images from memory or disk
//                                    mImageLoader.loadImage(story.getImages()[0], targetSize, /*options,*/ mImageLoadingListener);
//                                }
//                            }
//                        } else {
//                            mIsTheFirstOpened = true;
//                        }
                        break;
                    default:
                        return null;
                }
            }
            return null;
        }
    }
}