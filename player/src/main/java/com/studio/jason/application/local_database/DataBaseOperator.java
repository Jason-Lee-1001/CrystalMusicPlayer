package com.studio.jason.application.local_database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Jason on 2014/12/27.
 */
public class DataBaseOperator {

    private DataBaseCreator mCreator;
    private SQLiteDatabase mDatabase;

    public DataBaseOperator(Context context){
        mCreator = new DataBaseCreator(context);
        mDatabase = mCreator.getWritableDatabase();
    }

    public void closeDatabase(){
        if(mDatabase != null){
            this.mCreator.close();
        }
    }

    /**
     * table name: NOWPLAYING
     *_ID INTEGER primary key
     * TITLE TEXT
     * ARTIST TEXT
     * ALBUM TEXT
     * ALBUM_ID INTEGER
     * DURATION INTEGER
     * DATA TEXT
     */
    public boolean insert(String tableName, ContentValues values) {
        boolean flag = false;
        try {
            mDatabase.insert(tableName, null, values);
            flag = true;
        } catch (Exception err) {
            Log.e("flag", "---->DBOperator.insert()");
        }
        return flag;
    }

    public boolean del(String tableName, String[] id) {
        boolean flag = false;
        try {
            mDatabase.delete(tableName, "_ID = ?", id);
            flag = true;
        } catch (Exception err) {
            Log.e("flag", "---->DBOperator.del()");
        }
        return flag;
    }

    public boolean update(String tableName, ContentValues values, String whereClause, String[] whereArgs){
        boolean flag = false;
        try {
            mDatabase.update(tableName, values, whereClause, whereArgs);
            flag = true;
        } catch (Exception err) {
            Log.e("flag", "---->DBOperator.del()");
        }
        return flag;
    }

    public Cursor query(String tableName) {
        Cursor mCursor = null;
        try {
            mCursor = mDatabase.query(tableName, null, null, null, null, null, null);
        } catch (Exception err) {
            Log.e("flag", "---->DBOperator.query()");
        }
        return mCursor;
    }

}
