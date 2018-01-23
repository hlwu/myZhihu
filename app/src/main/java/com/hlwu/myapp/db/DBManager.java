package com.hlwu.myapp.db;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by hlwu on 1/11/18.
 */

public interface DBManager {
    public long insert(ContentValues contentValues);
    public Cursor queryBySql(String sql);
    public Cursor queryById(int id);
    public int update(ContentValues contentValues);
    public int delete(int id);
    public void execSQL(String sql);
    public void closeDB();
}
