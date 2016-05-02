package com.studio.jason.application.content_loader;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;

import com.studio.jason.application.customized_adapter.AlbumListBaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jason on 2014/12/14.
 */
public class AlbumListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;
    private AlbumListBaseAdapter mAdapter;

    public AlbumListLoader(Context context, AlbumListBaseAdapter adapter) {
        this.mContext = context;
        this.mAdapter = adapter;
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(mContext);
        loader.setUri(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI);
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        List<HashMap<String, Object>> albumInfos = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            HashMap<String, Object> albumInfo = new HashMap<>();

            String artWork = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
            albumInfo.put("artWork", artWork);

            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
            albumInfo.put("album", album);

            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));
            albumInfo.put("id",id);

            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST));
            albumInfo.put("artist", artist);
            albumInfos.add(albumInfo);
        }
        mAdapter.setList(albumInfos);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mAdapter.setList(null);
    }
}
