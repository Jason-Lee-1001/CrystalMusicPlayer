package com.studio.jason.application.local_database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by Jason on 2015/1/5.
 */
public class LocalContentProvider extends ContentProvider {

    private DataBaseCreator mCreator;
    private final static UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private final static int SONGS = 1;

    static{
        URI_MATCHER.addURI("com.studio.jason.cystalplayer","songs",SONGS);
    }

    @Override
    public boolean onCreate() {
        mCreator = new DataBaseCreator(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        SQLiteDatabase database = mCreator.getReadableDatabase();
        cursor = database.query("MYFAVORITE",projection,selection,selectionArgs,null,null,sortOrder);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
