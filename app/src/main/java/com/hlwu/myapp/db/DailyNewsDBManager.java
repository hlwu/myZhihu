package com.hlwu.myapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by hlwu on 1/11/18.
 */

public class DailyNewsDBManager implements DBManager {

    private static final String TAG = "flaggg_DailyNewsDBManager";

    private SQLiteDatabase mDailyNewsDB;
    private DBHelper mDBHelper;
    private static DailyNewsDBManager mDailyNewsDBManager;

    public DailyNewsDBManager(Context context) {
        mDBHelper = DBHelper.getInstance(context);
        mDailyNewsDB = mDBHelper.getWritableDatabase();
    }

    public static DailyNewsDBManager getInstance(Context context) {
        if (mDailyNewsDBManager == null) {
            mDailyNewsDBManager = new DailyNewsDBManager(context);
        }
        return mDailyNewsDBManager;
    }

    @Override
    public long insert(ContentValues contentValues) {
        mDailyNewsDB.beginTransaction();
        try {
            long rowId = mDailyNewsDB.insertOrThrow(DBHelper.MYZHIHU_TABLE_DAILYNEWS, null, contentValues);
            mDailyNewsDB.setTransactionSuccessful();
            return rowId;
        } catch (Exception e) {
            Log.d(TAG, "The insert operation failed e: " + e);
        }finally{
            mDailyNewsDB.endTransaction();
        }
        return -1;
    }

    @Override
    public Cursor queryBySql(String sql) {
        return mDailyNewsDB.rawQuery(sql, null);
    }

    @Override
    public Cursor queryById(int id) {
//        return mSqLiteDatabase.query(PERSON_TABLE, null, PERSON_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        return null;
    }

    public Cursor queryByDate(String date) {
        return mDailyNewsDB.query(DBHelper.MYZHIHU_TABLE_DAILYNEWS, /*null*/new String[]{DBHelper.MYZHIHU_TABLE_DAILYNEWS_DATE},
                /*DBHelper.MYZHIHU_TABLE_DAILYNEWS_DATE + "=?"*/null, new String[]{date}, null, null, null);
    }

    public Cursor queryAll() {
        return mDailyNewsDB.query(DBHelper.MYZHIHU_TABLE_DAILYNEWS, null, null, null, null, null, DBHelper.MYZHIHU_TABLE_DAILYNEWS_DATE + " DESC");
    }

    @Override
    public int update(ContentValues contentValues) {
        mDailyNewsDB.beginTransaction();
        try {
            int rows = mDailyNewsDB.update(DBHelper.MYZHIHU_TABLE_DAILYNEWS, contentValues, null, null);
            mDailyNewsDB.setTransactionSuccessful();
            return rows;
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "The update operation failed e: " + e);
        }finally{
            mDailyNewsDB.endTransaction();
        }
        return 0;
    }

    public int update(ContentValues contentValues, String whereClause, String[] whereArgs) {
        mDailyNewsDB.beginTransaction();
        try {
            int rows = mDailyNewsDB.update(DBHelper.MYZHIHU_TABLE_DAILYNEWS, contentValues, whereClause, whereArgs);
            mDailyNewsDB.setTransactionSuccessful();
            return rows;
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "The update operation failed e: " + e);
        }finally{
            mDailyNewsDB.endTransaction();
        }
        return 0;
    }

    @Override
    public int delete(int id) {
        mDailyNewsDB.beginTransaction();
        try {
//            int rows = mDailyNewsDB.delete(PERSON_TABLE, PERSON_ID +"= ?", new String[]{String.valueOf(id)});
            mDailyNewsDB.setTransactionSuccessful();
//            return rows;
        } catch (Exception e) {
            // TODO: handle exception
            Log.e(TAG, "The delete operation failed");
        }finally{
            mDailyNewsDB.endTransaction();
        }
        return 0;
    }

    @Override
    public void execSQL(String sql) {

    }

    @Override
    public void closeDB() {
        mDBHelper.close();
    }
}
