package com.hlwu.myapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by hlwu on 1/15/18.
 */

public class DailyNewsContentDBManager implements DBManager  {

    private static final String TAG = "flaggg_DailyNewsContentDBManager";

    private SQLiteDatabase mDailyNewsContentDB;
    private DBHelper mDBHelper;
    private static DailyNewsContentDBManager mDailyNewsContentDBManager;

    public DailyNewsContentDBManager(Context context) {
        mDBHelper = DBHelper.getInstance(context);
        mDailyNewsContentDB = mDBHelper.getWritableDatabase();
    }

    public static DailyNewsContentDBManager getInstance(Context context) {
        if (mDailyNewsContentDBManager == null) {
            mDailyNewsContentDBManager = new DailyNewsContentDBManager(context);
        }
        return mDailyNewsContentDBManager;
    }

    @Override
    public long insert(ContentValues contentValues) {
        mDailyNewsContentDB.beginTransaction();
        try {
            long rowId = mDailyNewsContentDB.insertOrThrow(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT, null, contentValues);
            mDailyNewsContentDB.setTransactionSuccessful();
            return rowId;
        } catch (Exception e) {
            Log.d(TAG, "The insert operation failed e: " + e);
        }finally{
            mDailyNewsContentDB.endTransaction();
        }
        return -1;
    }

    @Override
    public Cursor queryBySql(String sql) {
        return null;
    }

    @Override
    public Cursor queryById(String id) {
        String[] args = new String[]{id};
        return mDailyNewsContentDB.query(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT, null, DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT_ID + " = ?", args, null, null, null);
    }

    @Override
    public int update(ContentValues contentValues) {
        return 0;
    }

    public int update(ContentValues contentValues, String whereClause, String[] whereArgs) {
        mDailyNewsContentDB.beginTransaction();
        try {
            int rows = mDailyNewsContentDB.update(DBHelper.MYZHIHU_TABLE_DAILYNEWS_CONTENT, contentValues, whereClause, whereArgs);
            mDailyNewsContentDB.setTransactionSuccessful();
            return rows;
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "The update operation failed e: " + e);
        }finally{
            mDailyNewsContentDB.endTransaction();
        }
        return 0;
    }

    @Override
    public int delete(int id) {
        return 0;
    }

    @Override
    public void execSQL(String sql) {

    }

    @Override
    public void closeDB() {

    }
}
