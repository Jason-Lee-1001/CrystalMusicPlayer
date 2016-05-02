package com.studio.jason.application.local_database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jason on 2014/12/27.
 */
public class DataBaseCreator extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CRYSTAL_PLAYER_PLAYLISTS";
    private static final int DATABASE_VER = 1;

    public DataBaseCreator(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VER);
    }

    public DataBaseCreator(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql1 = "CREATE TABLE NOWPLAYING (_ID INTEGER primary key, TITLE TEXT, ARTIST TEXT, ALBUM TEXT, ALBUM_ID INTEGER, DURATION INTEGER, DATA TEXT)";
        String sql2 = "CREATE TABLE MYFAVORITE (_ID INTEGER primary key, TITLE TEXT, ARTIST TEXT, ALBUM TEXT, ALBUM_ID INTEGER, DURATION INTEGER, DATA TEXT)";
        db.execSQL(sql1);
        db.execSQL(sql2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion >= newVersion){
            return;
        }
        String delTableSQL1 = "DROP TABLE IF EXISTS NOWPLAYING";
        String delTableSQL2 = "DROP TABLE IF EXISTS MYFAVORITE";
        db.execSQL(delTableSQL1);
        db.execSQL(delTableSQL2);
        this.onCreate(db);
    }
}
