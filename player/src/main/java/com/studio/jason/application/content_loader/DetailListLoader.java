package com.studio.jason.application.content_loader;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import com.studio.jason.application.customized_adapter.TrackDetailAdapter;
import com.studio.jason.application.customized_view.ListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2014/12/14.
 */
public class DetailListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;
    private TrackDetailAdapter mAdapter;
    private long mId = 0;
    private int flag;
    private String sizeString;

    public DetailListLoader(Context context, TrackDetailAdapter adapter, long id, int flag) {
        this.mContext = context;
        this.mAdapter = adapter;
        this.mId = id;
        this.flag = flag;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sizeString = sharedPreferences.getString("scan_size","800");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(mContext);
        if(flag == 1) {
            if (mId != 0) {
                String selectArgs = String.valueOf(mId);
                loader.setSelection(MediaStore.Audio.Media.ARTIST_ID + "= ?"+" and "+MediaStore.Audio.Media.SIZE +">?"+" and "+MediaStore.Audio.Media.IS_MUSIC+">?");
                loader.setSelectionArgs(new String[]{selectArgs,sizeString+"000","0"});
                loader.setUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            }
        }else if(flag == 2){
            if (mId != 0) {
                String selectArgs = String.valueOf(mId);
                loader.setSelection(MediaStore.Audio.Media.ALBUM_ID + "= ?"+" and "+MediaStore.Audio.Media.SIZE +">?"+" and "+MediaStore.Audio.Media.IS_MUSIC+">?");
                loader.setSelectionArgs(new String[]{selectArgs,sizeString+"000","0"});
                loader.setUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            }
        }else if(flag == 3){
            loader.setUri(Uri.parse("content://com.studio.jason.cystalplayer/songs"));
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<ListItem<String, String>> mp3Infos = new ArrayList<>();
        if (flag == 3 && cursor.getCount() != 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ListItem<String, String> mp3Info = new ListItem<>();

                String title = cursor.getString((cursor.getColumnIndex("TITLE")));
                mp3Info.put("title", title);

                String artist = cursor.getString(cursor.getColumnIndex("ARTIST"));
                mp3Info.put("artist", artist);

                String album = cursor.getString(cursor.getColumnIndex("ALBUM"));
                mp3Info.put("album", album);

                String data = cursor.getString(cursor.getColumnIndex("DATA"));
                mp3Info.put("data", data);

                String song_id = cursor.getString(cursor.getColumnIndex("_ID"));
                mp3Info.put("song_id", song_id);

                String album_id = cursor.getString(cursor.getColumnIndex("ALBUM_ID"));
                mp3Info.put("album_id", album_id);

                String duration = cursor.getString(cursor.getColumnIndex("DURATION"));
                mp3Info.put("duration", duration);

                mp3Infos.add(mp3Info);
            }
        } else {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ListItem<String, String> mp3Info = new ListItem<>();

                String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                mp3Info.put("title", title);

                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                mp3Info.put("artist", artist);

                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                mp3Info.put("album", album);

                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                mp3Info.put("data", data);

                String song_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                mp3Info.put("song_id", song_id);

                String album_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                mp3Info.put("album_id", album_id);

                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                mp3Info.put("duration", duration);

                mp3Infos.add(mp3Info);
            }
        }
        mAdapter.setList(mp3Infos);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setList(null);
    }
}
