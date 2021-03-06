package com.hlwu.myapp.presenter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.hlwu.myapp.R;
import com.hlwu.myapp.db.DBHelper;
import com.hlwu.myapp.db.DailyNewsContentDBManager;
import com.hlwu.myapp.db.DailyNewsDBManager;
import com.hlwu.myapp.news.DailyStructure;
import com.hlwu.myapp.news.Stories;
import com.hlwu.myapp.news.TopStories;
import com.hlwu.myapp.ui.dailynewslist.DailyNewsItems;
import com.hlwu.myapp.ui.dailynewslist.DailyNewsFragment;
import com.hlwu.myapp.ui.base.Ui;
import com.hlwu.myapp.utils.NetUtil;
import com.lhh.ptrrv.library.PullToRefreshRecyclerView;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by hlwu on 1/8/18.
 */

public class DailyNewsFragmentPresenter extends Presenter<DailyNewsFragmentPresenter.DailyNewsFragmentUI> {

    private static final String TAG = "flaggg_RecyclerViewFragmentPresenter";

    private static final int MSG_GET_NEWS_DONE = 0;
    private static final int MSG_GET_IMAGE_DONE = 1;
    private static final int MSG_STOP_REFRESHING = 2;
    private static final int MSG_DOWNLOAD_NOTHING = 3;
    private static final int MSG_NO_NEW_LATEST_NEWS = 4;
    private static final int MSG_LATEST_NEWS_UPDATED = 5;
    private static final int MSG_TOP_STORIES_UPDATED = 6;
    private static final String URL_LATEST_NEWS = "https://news-at.zhihu.com/api/4/news/latest";
    private static final String URL_BEFORE_NEWS = "https://news-at.zhihu.com/api/4/news/before/";
    public static final int VIEW_TYPE_DATE = 0;
    public static final int VIEW_TYPE_STORIES = 1;
    public static final int VIEW_TYPE_FOOTER = 2;
    public static final int VIEW_TYPE_EMPTY = 3;
    public static final int VIEW_TYPE_BANNER = 4;
    private static final String TASK_SAVE_OR_UPDATE_DAILYNEWS = "save_or_update_dailyNews";
    private static final String TASK_LOAD_DAILYNEWS = "load_dailyNews";
//    private static final String TASK_UPDATE_DAILYNEWS = "update_dailyNews";
    private static final String READED_PREFERENCE = "readed_preference";
    private static final String READED_PREFERENCE_KEY = "readed_stories";

    private ImageLoader mImageLoader;
    private DailyStructure mJustGotNews;    //include just downloaded news and just queried from db news
    private DailyStructure mOldestNews;
    private DailyNewsDBManager mDailyNewsDBManager;
//    private DailyNewsContentDBManager mDailyNewsContentDBManager;
    private LinkedHashMap<String, Stories> mTheStoriesWhichDidNotAddIcon = new LinkedHashMap<>();   //used for matching icons.
    private LinkedHashMap<String, DailyStructure> mNewsMap = new LinkedHashMap<String, DailyStructure>();
    private String mNewsDate = "latest";    //decide which day's news to load;
    private boolean mIsTheFirstOpened = false;
    private boolean mTheShowingContentIsFromNetwork = true;

    @Override
    public void onUiReady(DailyNewsFragmentUI ui) {
        super.onUiReady(ui);
        mDailyNewsDBManager = DailyNewsDBManager.getInstance(ui.getContext());
//        mDailyNewsContentDBManager = DailyNewsContentDBManager.getInstance(ui.getContext());
        initUi();
    }

    @Override
    public void onUiUnready(DailyNewsFragmentUI ui) {
        super.onUiUnready(ui);
    }

    public interface DailyNewsFragmentUI extends Ui {
        List<DailyNewsItems> getmDailyNewsItems();
        List<TopStories> getmTopStoriesItems();
        DailyNewsFragment.DailyNewsAdapter getRecyclerAdapter();
        PullToRefreshRecyclerView getRecyclerView();
        void showNoNewLatestNews();
        void showFootProgressLoading(boolean isLoading);
        void showDownloadNothing();
        void showViewPagerIfNecessary();
        void scrollingToStart();
        void showLocalNewsMsg();
        void showNetworkAvailable();
        Context getContext();
    }

    private void initUi() {     //query DB and update recycler view.
        //if network connected, don't show db's content.
        //because the content stored at db may not updated.
        if (NetUtil.isNetworkConnected(getUi().getContext())) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getUi().getContext().registerReceiver(mNetworkStateReceiver, filter);
        new DBTask().execute(TASK_LOAD_DAILYNEWS);
    }

    private Handler mUiHandler = new Handler() {
        @SuppressLint("LongLogTag")
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handlerMessgae msg.what: " + msg.what);
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_GET_NEWS_DONE:
                    mJustGotNews = (DailyStructure) msg.obj;
                    if (getUi().getmDailyNewsItems().isEmpty()) {
                        getUi().getmDailyNewsItems().add(new DailyNewsItems(null, null, 0, null));  //banner position
                    }
                    getUi().getmDailyNewsItems().add(new DailyNewsItems(null, null, 0, mJustGotNews.getDate()));   // add date
                    for (Stories story : mJustGotNews.getStories()) {     // add stories
                        getUi().getmDailyNewsItems().add(new DailyNewsItems(story.getTitle(),
                                BitmapFactory.decodeResource(getUi().getContext().getResources(), android.R.drawable.stat_sys_download_done),
                                story.getId(), null));
                    }
                    getUi().getRecyclerAdapter().notifyDataSetChanged();
                    getUi().showFootProgressLoading(false);
                    mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(MSG_STOP_REFRESHING), 300);
                    break;
                case MSG_LATEST_NEWS_UPDATED:
                    mJustGotNews = (DailyStructure) msg.obj;
                    if (getUi().getmDailyNewsItems().isEmpty()) {
                        getUi().getmDailyNewsItems().add(new DailyNewsItems(null, null, 0, null));  //banner position
                    }
                    Stories[] latestStories = mJustGotNews.getStories();
                    int oldLatestStoryId = getUi().getmDailyNewsItems().get(1).getId();
                    for (int i = 0; i < latestStories.length; i++) {    //insert the latest stories
                        if (latestStories[i].getId() != oldLatestStoryId) { //the new stories, insert it
                            getUi().getmDailyNewsItems().add(i + 1, new DailyNewsItems(latestStories[i].getTitle(),
                                    BitmapFactory.decodeResource(getUi().getContext().getResources(), android.R.drawable.stat_sys_download_done),
                                    latestStories[i].getId(), null));
                        } else {
                            break;
                        }
                    }
                    getUi().getRecyclerAdapter().notifyDataSetChanged();
                    getUi().showFootProgressLoading(false);
                    mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(MSG_STOP_REFRESHING), 300);
                    break;
                case MSG_GET_IMAGE_DONE:
                    IconObject iconObject = (IconObject) msg.obj;
                    updateImage(mTheStoriesWhichDidNotAddIcon, iconObject.imageUri, iconObject.loadedImage);
                    break;
                case MSG_STOP_REFRESHING:
                    getUi().getRecyclerView().setRefreshing(false);
                    getUi().showViewPagerIfNecessary();
                    break;
                case MSG_DOWNLOAD_NOTHING:
                    getUi().showViewPagerIfNecessary();
                    getUi().getRecyclerView().setRefreshing(false);
                    getUi().showDownloadNothing();
                    getUi().showFootProgressLoading(false);
                    break;
                case MSG_NO_NEW_LATEST_NEWS:
                    getUi().showNoNewLatestNews();
                    mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(MSG_STOP_REFRESHING), 300);
                    break;
                case MSG_TOP_STORIES_UPDATED:
                    mJustGotNews = (DailyStructure) msg.obj;
                    if (mJustGotNews.getTopStories() != null) {
                        getUi().getmTopStoriesItems().clear();
                        for (TopStories topStory : mJustGotNews.getTopStories()) {
                            getUi().getmTopStoriesItems().add(topStory);
                        }
                    }
                    getUi().getRecyclerAdapter().notifyDataSetChanged();
                    break;
                default:
                    Log.d(TAG, "what happened?");
                    return;
            }
        }
    };

    public DailyStructure getmOldestNews() {
        return mOldestNews;
    }

    public int getmNewsMapContentLength() {
        int length = 0;
        Iterator<Map.Entry<String, DailyStructure>> entries = mNewsMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, DailyStructure> entry = entries.next();
            if (entry.getValue() != null) {
                length += (entry.getValue().getStories().length + 1); //stories' length + date length
            }
        }
        return length + 1; //length + banner
    }

    @SuppressLint("LongLogTag")
    public int getViewTypeAtPosition(int position) {
        if (position == 0) {
            return VIEW_TYPE_BANNER;
        }
        int checkLength = 1, lastCheckLength = 1;   //0 is banner.
        Iterator<Map.Entry<String, DailyStructure>> entries = mNewsMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, DailyStructure> entry = entries.next();
            if (entry.getValue() != null) {
                checkLength += (entry.getValue().getStories().length + 1); //stories' length + date length
                if (checkLength -1 >= position) {  //checkLength starts from 1, position starts from 0;
                    if (position == lastCheckLength) {
                        return VIEW_TYPE_DATE;
                    } else {
                        return VIEW_TYPE_STORIES;
                    }
                }
                lastCheckLength = checkLength;
            }
        }
        Log.d(TAG, "some logic is wrong at getViewTypeAtPosition");
        return VIEW_TYPE_STORIES;
    }

    public String getToolbarTitle(int firstVisibleItem, String currentTitle) {
        int itemType = getUi().getRecyclerAdapter().getItemViewType(firstVisibleItem);
        String title = currentTitle;
        if (itemType == VIEW_TYPE_BANNER) {
            title = getUi().getContext().getResources().getString(R.string.toolbar_title_daily_news);
        } else if (itemType == VIEW_TYPE_DATE) {
            title = getDate(firstVisibleItem);
        }
        return title;
    }

    public String getDate(int position) {
        Time today = new Time();
        today.setToNow();
        int year = Integer.valueOf(getUi().getmDailyNewsItems().get(position).getDate().substring(0, 4));
        int month = Integer.valueOf(getUi().getmDailyNewsItems().get(position).getDate().substring(4, 6));
        int day = Integer.valueOf(getUi().getmDailyNewsItems().get(position).getDate().substring(6, 8));
        if (getUi().getmDailyNewsItems().get(position).getDate().equals(today.format("%Y%m%d"))) {
            return getUi().getContext().getResources().getString(R.string.news_today);
        } else {
            String date;
            Time time = new Time();
            time.set(day, (month - 1), year);   //Month [0-11]
            time.normalize(true);
            if (Locale.getDefault().getLanguage() == "zh") {
                date = getUi().getContext().getResources().getString(R.string.date_zh,
                        month, day, weekDayToString(time.weekDay));
            } else {
                date = getUi().getContext().getResources().getString(R.string.date_es,
                        weekDayToString(time.weekDay), monthToString(month), day, year);
            }
            return date;
        }
    }

    public void setStoryAsReaded(int storyId) {
        SharedPreferences prefs = getUi().getContext().getSharedPreferences(READED_PREFERENCE, Context.MODE_PRIVATE);
        Set<String> readedStories = prefs.getStringSet(READED_PREFERENCE_KEY, new android.support.v4.util.ArraySet<String>());
        Set<String> newReadedStories = new android.support.v4.util.ArraySet<String>(readedStories);
        if (!newReadedStories.contains(String.valueOf(storyId))) {
            newReadedStories.add(String.valueOf(storyId));
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.putStringSet(READED_PREFERENCE_KEY, newReadedStories);
            editor.apply();
        }
    }

    public int getTextColor(int stroyId) {
        SharedPreferences prefs = getUi().getContext().getSharedPreferences(READED_PREFERENCE, Context.MODE_PRIVATE);
        Set<String> readedStories = prefs.getStringSet(READED_PREFERENCE_KEY, new android.support.v4.util.ArraySet<String>());

        if (readedStories.contains(String.valueOf(stroyId))) {
            return getUi().getContext().getResources().getColor(android.R.color.darker_gray);
        }
        return getUi().getContext().getResources().getColor(android.R.color.black);
    }

    public void loadNews(String date) {
        mNewsDate = date;
        new Thread(mNetRunnable).start();
    }

    private Runnable mNetRunnable = new Runnable() {
        @TargetApi(Build.VERSION_CODES.N)
        @SuppressLint("LongLogTag")
        @Override
        public void run() {
            Log.d(TAG, "mNetRunnable run start, load " + mNewsDate + " news");

            if (mNewsDate == null) {
                return;
            }

            String url;
            if (mNewsDate.equals("latest")) {
                url = URL_LATEST_NEWS;
            } else {
                url = URL_BEFORE_NEWS + mNewsDate;
            }
            String newsJson = NetUtil.get(url);     //download news
            Gson gson = new Gson();
            DailyStructure news = gson.fromJson(newsJson, DailyStructure.class);
            Log.d(TAG, "newsJson: " + newsJson);

            if (news == null) {
                Message msg = mUiHandler.obtainMessage(MSG_DOWNLOAD_NOTHING);
                msg.obj = news;
                mUiHandler.sendMessage(msg);
                return;
            }

            if (!mTheShowingContentIsFromNetwork) {
                //if the network is unavailable, we'll show db's news.
                //after that, if network is available now, we need update the content from network.
                //clear mNewsMap to not impact the network news.
                mNewsMap.clear();
                getUi().getmDailyNewsItems().clear();
                mTheShowingContentIsFromNetwork = true;
            }
            boolean needToLoadNewIcon = true;
            Time today = new Time();
            today.setToNow();
            Log.d(TAG, "isTopStoriesUpdated: " + news.isTopStoriesUpdated(mNewsMap.get(today.format("%Y%m%d"))));
            if (news.isTopStoriesUpdated(mNewsMap.get(today.format("%Y%m%d")))) {
                Message msg = mUiHandler.obtainMessage(MSG_TOP_STORIES_UPDATED);
                msg.obj = news;
                mUiHandler.sendMessage(msg);
            }

            if (!mNewsMap.containsKey(today.format("%Y%m%d")) && mNewsDate.equals("latest")) { //need add the latest news to map's head
                LinkedHashMap<String, DailyStructure> tmpMap = new LinkedHashMap<String, DailyStructure>();
                tmpMap.put(news.getDate(), news);
                tmpMap.putAll(mNewsMap);
                mNewsMap = tmpMap;
                Message msg = mUiHandler.obtainMessage(MSG_GET_NEWS_DONE);
                msg.obj = news;
                mUiHandler.sendMessage(msg);
                if (mIsTheFirstOpened || NetUtil.isNetworkConnected(getUi().getContext())) {
                    mOldestNews = news;
                }
            } else if (mNewsDate.equals("latest")) {  //update the latest news
                if (!news.isSameWith(mNewsMap.get(today.format("%Y%m%d")))) {
                    mNewsMap.replace(news.getDate(), news);
                    Message msg = mUiHandler.obtainMessage(MSG_LATEST_NEWS_UPDATED);
                    msg.obj = news;
                    mUiHandler.sendMessage(msg);
                } else {
                    mUiHandler.sendEmptyMessage(MSG_NO_NEW_LATEST_NEWS);
                    needToLoadNewIcon = false;
                }
            } else {    //put the before news to end
                mNewsMap.put(news.getDate(), news);
                mOldestNews = news;
                Message msg = mUiHandler.obtainMessage(MSG_GET_NEWS_DONE);
                msg.obj = news;
                mUiHandler.sendMessage(msg);
            }

            if (needToLoadNewIcon) {
                //update UI start
                if (mImageLoader == null) {
                    mImageLoader = ImageLoader.getInstance();
                }
                Stories[] stories = news.getStories();
                for (final Stories story : stories) {
                    mTheStoriesWhichDidNotAddIcon.put(story.getImages()[0], story);
                    ImageSize targetSize = new ImageSize(100, 100); // result Bitmap will be fit to this size
                    mImageLoader.loadImage(story.getImages()[0], targetSize, /*options,*/ mImageLoadingListener);   //download images
                }
                //update UI end

                Log.d(TAG, "mNetRunnable gets " + mNewsDate + " news down");

                //insert or update to daily_news table start
                (new DBTask()).execute(TASK_SAVE_OR_UPDATE_DAILYNEWS, news.getDate(), newsJson);
                //insert or update to daily_news table end
            }
        }
    };

    private class DBTask extends AsyncTask<String, Integer, Boolean> {
        @SuppressLint("LongLogTag")
        @Override
        protected Boolean doInBackground(String... strings) {
            Log.d(TAG, "doInBackground  strings[0]: " + strings[0]);
            if (strings[0] != null) {
                switch (strings[0]) {
                    case TASK_SAVE_OR_UPDATE_DAILYNEWS:
                        if (strings[1] == null || strings[2] == null) {
                            Log.d(TAG, "do save daily news task failed!");
                            return null;
                        }
                        //insert to daily_news table
                        ContentValues dailyNewsCV = new ContentValues();
                        dailyNewsCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_DATE, strings[1]);
                        dailyNewsCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_DETAILS, strings[2]);
                        if (mDailyNewsDBManager.insert(dailyNewsCV) == -1) {    //insert fail, in normal case, this means db already had this news, update it only.
                            ContentValues dailyNewsUpdateCV = new ContentValues();
                            dailyNewsUpdateCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_DETAILS, strings[2]);
                            mDailyNewsDBManager.update(dailyNewsUpdateCV, DBHelper.MYZHIHU_TABLE_DAILYNEWS_DATE + " = ?", new String[]{strings[1]});
                        }

//                        //insert to daily_news_content table
//                        ContentValues dailyNewsContentCV = new ContentValues();
//                        DailyStructure dailyNews = (new Gson()).fromJson(strings[2], DailyStructure.class);
//                        for (Stories story : dailyNews.getStories()) {
//                            dailyNewsContentCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT_ID, story.getId());
//                            dailyNewsContentCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT_TYPE, story.getType());
//                            dailyNewsContentCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT_TITLE, story.getTitle());
//                            mDailyNewsContentDBManager.insert(dailyNewsContentCV);
//                        }
                        break;
//                    case TASK_SAVE_DAILYNEWS:
//                        if (strings[1] == null || strings[2] == null) {
//                            Log.d(TAG, "do save daily news task failed!");
//                            return null;
//                        }
//                        //insert to daily_news table
//                        ContentValues dailyNewsCV = new ContentValues();
//                        dailyNewsCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_DATE, strings[1]);
//                        dailyNewsCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_DETAILS, strings[2]);
//                        mDailyNewsDBManager.insert(dailyNewsCV);
//
//                        //insert to daily_news_content table
//                        ContentValues dailyNewsContentCV = new ContentValues();
//                        DailyStructure dailyNews = (new Gson()).fromJson(strings[2], DailyStructure.class);
//                        for (Stories story : dailyNews.getStories()) {
//                            dailyNewsContentCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT_ID, story.getId());
//                            dailyNewsContentCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT_TYPE, story.getType());
//                            dailyNewsContentCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT_TITLE, story.getTitle());
//                            mDailyNewsContentDBManager.insert(dailyNewsContentCV);
//                        }
//                        break;
//
//                    case TASK_UPDATE_DAILYNEWS:
//                        ContentValues dailyNewsUpdateCV = new ContentValues();
//                        dailyNewsUpdateCV.put(DBHelper.MYZHIHU_TABLE_DAILYNEWS_DETAILS, strings[2]);
//                        mDailyNewsDBManager.update(dailyNewsUpdateCV, DBHelper.MYZHIHU_TABLE_DAILYNEWS_DATE + " = ?", new String[]{strings[1]});
//                        break;

                    case TASK_LOAD_DAILYNEWS:
                        if (mImageLoader == null) {
                            mImageLoader = ImageLoader.getInstance();
                        }

                        Cursor c = mDailyNewsDBManager.queryAll();
                        if (c != null && c.getCount() > 0) {
                            mTheShowingContentIsFromNetwork = false;
                            while (c.moveToNext()) {
                                String detial = c.getString(c.getColumnIndex(DBHelper.MYZHIHU_TABLE_DAILYNEWS_DETAILS));
                                DailyStructure news = (new Gson()).fromJson(detial, DailyStructure.class);
                                Time today = new Time();
                                today.setToNow();
                                if (news.isTopStoriesUpdated(mNewsMap.get(today.format("%Y%m%d")))) {
                                    Message msg = mUiHandler.obtainMessage(MSG_TOP_STORIES_UPDATED);
                                    msg.obj = news;
                                    mUiHandler.sendMessage(msg);
                                }
                                mNewsMap.put(news.getDate(), news);
                                Message msg = mUiHandler.obtainMessage(MSG_GET_NEWS_DONE);
                                msg.obj = news;
                                mUiHandler.sendMessage(msg);
                                mOldestNews = news;

                                Stories[] stories = news.getStories();
                                for (final Stories story : stories) {
                                    mTheStoriesWhichDidNotAddIcon.put(story.getImages()[0], story);
                                    ImageSize targetSize = new ImageSize(100, 100); // result Bitmap will be fit to this size
                                    //we download the images before, so there should load images from memory or disk
                                    mImageLoader.loadImage(story.getImages()[0], targetSize, /*options,*/ mImageLoadingListener);
                                }
                                getUi().showLocalNewsMsg();
                            }
                        } else {
                            mIsTheFirstOpened = true;
                        }
                        break;
                    default:
                        return null;
                }
            }
            return null;
        }
    }

    private InnerImageLoadingListener mImageLoadingListener = new InnerImageLoadingListener();
    private class InnerImageLoadingListener implements ImageLoadingListener {

        @SuppressLint("LongLogTag")
        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            Log.d(TAG, "onLoadingFailed failReason: " + failReason);
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onLoadingStarted(String imageUri, View view) {
            Log.d(TAG, "onLoadingStarted view: " + view);
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            Log.d(TAG, "onLoadingComplete, loadedImage: " + loadedImage);

            Message msg = mUiHandler.obtainMessage(MSG_GET_IMAGE_DONE);
            msg.obj = new IconObject(imageUri, loadedImage);
            mUiHandler.sendMessage(msg);
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            Log.d(TAG, "onLoadingCancelled");
        }
    }

    private void updateImage(LinkedHashMap<String, Stories> stories, String imageUri, Bitmap loadedImage) {
        Stories story = stories.get(imageUri);
        if (story != null) {
            for (int i = 0; i < getUi().getmDailyNewsItems().size(); i++) {
                if (getUi().getmDailyNewsItems().get(i).getId() == story.getId()) {   //get the story's position
                    if (i != -1) {
                        DailyNewsItems item = getUi().getmDailyNewsItems().get(i);
                        item.setPic(loadedImage);
                        getUi().getmDailyNewsItems().set(i, item);
                        getUi().getRecyclerAdapter().notifyItemChanged(i);
                        return;
                    }
                }
            }
        }
    }

    private class IconObject {
        public String imageUri;
        public Bitmap loadedImage;

        public IconObject(String imageUri, Bitmap loadedImage) {
            this.imageUri = imageUri;
            this.loadedImage = loadedImage;
        }
    }

    private String weekDayToString(int num) {
        int resId = -1;
        switch (num) {
            case 0: resId = R.string.weekday_0; break;
            case 1: resId = R.string.weekday_1; break;
            case 2: resId = R.string.weekday_2; break;
            case 3: resId = R.string.weekday_3; break;
            case 4: resId = R.string.weekday_4; break;
            case 5: resId = R.string.weekday_5; break;
            case 6: resId = R.string.weekday_6; break;
        }
        return getUi().getContext().getResources().getString(resId);
    }

    private String monthToString(int num) {
        int resId = -1;
        switch (num) {
            case 1: resId = R.string.month_1; break;
            case 2: resId = R.string.month_2; break;
            case 3: resId = R.string.month_3; break;
            case 4: resId = R.string.month_4; break;
            case 5: resId = R.string.month_5; break;
            case 6: resId = R.string.month_6; break;
            case 7: resId = R.string.month_7; break;
            case 8: resId = R.string.month_8; break;
            case 9: resId = R.string.month_9; break;
            case 10: resId = R.string.month_10; break;
            case 11: resId = R.string.month_11; break;
            case 12: resId = R.string.month_12; break;
        }
        return getUi().getContext().getResources().getString(resId);
    }

    private BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (wifiNetworkInfo.isConnected() || dataNetworkInfo.isConnected()) {
                    getUi().showNetworkAvailable();
                    getUi().getContext().unregisterReceiver(mNetworkStateReceiver);
                }
            } else {
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                Network[] networks = connMgr.getAllNetworks();
                int result = 0;
                for (int i = 0; i < networks.length; i++) {
                    NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                    if (networkInfo.isConnected()) {
                        getUi().showNetworkAvailable();
                        getUi().getContext().unregisterReceiver(mNetworkStateReceiver);
                    }
                }

            }
        }
    };

}
