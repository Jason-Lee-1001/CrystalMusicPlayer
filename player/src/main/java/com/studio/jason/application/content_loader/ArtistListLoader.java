package com.studio.jason.application.content_loader;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;

import com.studio.jason.application.customized_adapter.ArtistListBaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jason on 2014/12/14.
 */
public class ArtistListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;
    private ArtistListBaseAdapter mAdapter;

    public ArtistListLoader(Context context, ArtistListBaseAdapter adapter) {
        this.mContext = context;
        this.mAdapter = adapter;
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(mContext);
        loader.setUri(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI);
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        List<HashMap<String, Object>> artistInfos = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            HashMap<String, Object> artistInfo = new HashMap<>();

            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            artistInfo.put("artist", artist);

            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Artists._ID));
            artistInfo.put("id",id);

            int count = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
            artistInfo.put("count", count);

            artistInfos.add(artistInfo);
        }
        mAdapter.setList(artistInfos);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mAdapter.setList(null);
    }
}
