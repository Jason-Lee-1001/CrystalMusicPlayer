package com.studio.jason.application.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.studio.jason.application.MyApplication;
import com.studio.jason.application.R;
import com.studio.jason.application.customized_adapter.SearchTrackAdapter;
import com.studio.jason.application.customized_view.ListItem;
import com.studio.jason.application.service.MediaPlayBackService;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mEditText;
    private ListView mListView;
    private String queryKeyWord;
    private SearchTrackAdapter mAdapter;
    private LoaderManager mManager;
    private MyApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity_layout);
        mApplication = (MyApplication) getApplication();
        mEditText = (EditText) this.findViewById(R.id.search_activity_editText);
        mListView = (ListView) this.findViewById(R.id.search_result_list);
        mAdapter = new SearchTrackAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mApplication.setCurrentPlayList(SearchActivity.this.mAdapter.getList())
                        .setCurrentAudio(SearchActivity.this.mAdapter.getList().get(position))
                        .setCurrentIndex(position);
                sendBroadcast(new Intent(MediaPlayBackService.ACTION_PLAY_NEW_LIST));
            }
        });
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                queryKeyWord = mEditText.getText().toString().replaceAll("[^\\w]", "");
                if (null != queryKeyWord && !"".equals(queryKeyWord)) {
                    if(null == mManager.getLoader(124)) {
                        mManager.initLoader(124, null, SearchActivity.this);
                    }else{
                        mManager.restartLoader(124, null, SearchActivity.this);
                    }
                }
            }
        });
        mManager = getLoaderManager();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(SearchActivity.this);
        loader.setSelection(MediaStore.Audio.Media.ARTIST + " like ?" + " or " + MediaStore.Audio.Media.TITLE + " like ?");
        loader.setSelectionArgs(new String[]{"%"+queryKeyWord+"%","%"+queryKeyWord+"%"});
        loader.setUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        List<ListItem<String, String>> mp3Infos = new ArrayList<>();
        System.out.println(cursor.getCount());
        if (cursor.getCount() != 0) {
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
    }

//    private class MyAsyncTask extends AsyncTask<String,Void,List<ListItem<String, String>>>{
//
//        @Override
//        protected List<ListItem<String, String>> doInBackground(String... params) {
//            ContentResolver resolver = getContentResolver();
//            Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
//                    MediaStore.Audio.Media.TITLE+"like", new String[]{"%"+params[0]+"%"}, null);
//            List<ListItem<String, String>> mp3Infos = new ArrayList<>();
//            System.out.println(cursor.getCount());
//            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
//                ListItem<String, String> mp3Info = new ListItem<>();
//
//                String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
//                mp3Info.put("title", title);
//
//                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                mp3Info.put("artist", artist);
//
//                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
//                mp3Info.put("data", data);
//
//                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
//                mp3Info.put("album", album);
//
//                String song_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
//                mp3Info.put("song_id", song_id);
//
//                String album_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//                mp3Info.put("album_id", album_id);
//
//                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
//                mp3Info.put("duration", duration);
//
//                mp3Infos.add(mp3Info);
//            }
//            cursor.close();
//            return mp3Infos;
//        }
//
//        @Override
//        protected void onPostExecute(List<ListItem<String, String>> listItems) {
//            mAdapter.setList(listItems);
//            super.onPostExecute(listItems);
//        }
//    }

}
