package com.hlwu.myapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Used for creating or upgrading DB.
 * Created by hlwu on 1/11/18.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "flaggg_DailyNews";
    private static final int MYZHIHU_DATABASE_VERSION = 1;
    private static final String MYZHIHU_NAME = "myZhiHu.db";

    //daily news recycle view table
    protected static final String MYZHIHU_TABLE_DAILYNEWS = "daily_news";
    public static final String MYZHIHU_TABLE_DAILYNEWS_DATE = "date";
    public static final String MYZHIHU_TABLE_DAILYNEWS_DETAILS = "details";

    //daily news content table
    protected static final String MYZHIHU_TABLE_DAILYNEWS_CONTENT = "daily_news_content";
    public static final String MYZHIHU_TABLE_DAILYNEWS_CONTENT_TYPE = "type";
    public static final String MYZHIHU_TABLE_DAILYNEWS_CONTENT_ID = "id";
    public static final String MYZHIHU_TABLE_DAILYNEWS_CONTENT_TITLE = "title";
    public static final String MYZHIHU_TABLE_DAILYNEWS_CONTENT_STORY = "story";
    public static final String MYZHIHU_TABLE_DAILYNEWS_CONTENT_IMAGE = "image";
    public static final String MYZHIHU_TABLE_DAILYNEWS_CONTENT_ISREADED = "isreaded";

    private static DBHelper mDBHelper;
    private Context mContext;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    public static DBHelper getInstance(Context context) {
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(context, MYZHIHU_NAME, null, MYZHIHU_DATABASE_VERSION);
        }
        return mDBHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        String createDailyNewsSql = "CREATE TABLE [" + MYZHIHU_TABLE_DAILYNEWS + "] ("
                + "[" + MYZHIHU_TABLE_DAILYNEWS_DATE + "] VARCHAR(20) PRIMARY KEY,"
                + "[" + MYZHIHU_TABLE_DAILYNEWS_DETAILS + "] TEXT)";

        String createDailyNewsContentSql = "CREATE TABLE [" + MYZHIHU_TABLE_DAILYNEWS_CONTENT + "] ("
                + "[" + MYZHIHU_TABLE_DAILYNEWS_CONTENT_ID + "] INTEGER PRIMARY KEY,"
                + "[" + MYZHIHU_TABLE_DAILYNEWS_CONTENT_IMAGE + "] TEXT,"
                + "[" + MYZHIHU_TABLE_DAILYNEWS_CONTENT_ISREADED + "] INTEGER)";

        db.execSQL(createDailyNewsSql);
        db.execSQL(createDailyNewsContentSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
