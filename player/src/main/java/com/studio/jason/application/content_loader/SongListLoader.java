package com.studio.jason.application.content_loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;

import com.studio.jason.application.customized_adapter.SongListBaseAdapter;
import com.studio.jason.application.fragment.library.SongListFragment;

/**
 * Created by Jason on 2014/12/14.
 */
public class SongListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;
    private SongListBaseAdapter mAdapter;
    private SongListFragment mFragment;
    private String sizeString;

    public SongListLoader(SongListFragment fragment, SongListBaseAdapter adapter) {
        this.mFragment = fragment;
        this.mContext = fragment.getActivity();
        this.mAdapter = adapter;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
        sizeString = sharedPreferences.getString("scan_size","800");
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(mContext);
        loader.setUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        loader.setSelection(MediaStore.Audio.Media.SIZE +">?"+" and "+MediaStore.Audio.Media.IS_MUSIC+">?");
        loader.setSelectionArgs(new String[]{sizeString+"000","0"});
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor cursor) {
        mFragment.setResultCursor(cursor);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        mAdapter.setList(null);
    }
}
