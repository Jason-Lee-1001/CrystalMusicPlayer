package com.studio.jason.application.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.studio.jason.application.MyApplication;
import com.studio.jason.application.R;
import com.studio.jason.application.customized_view.ListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BootUpScreenActivity extends Activity {

    private ProgressBar mProgressBar;
    private ImageView mImageLogo;
    private MyApplication mApplication;

    private Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                mProgressBar.setAnimation(AnimationUtils.loadAnimation(BootUpScreenActivity.this, R.anim.fade_out));
                mProgressBar.setVisibility(View.GONE);
            }
            if (msg.what == 2) {
                Intent intent = new Intent(BootUpScreenActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        android.os.Debug.startMethodTracing("Entertainment");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boot_up_activity_layout);
        mImageLogo = (ImageView) this.findViewById(R.id.boot_up_icon_image);
        mProgressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        mApplication = (MyApplication) getApplication();
        LoadingTask mTask = new LoadingTask();
        mTask.execute();
    }

    @Override
    protected void onDestroy() {
//        android.os.Debug.stopMethodTracing();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    private void createListFromDB(List<ListItem<String, String>> mp3Infos, String sizeString) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.SIZE + ">?" + " and " + MediaStore.Audio.Media.IS_MUSIC + ">?", new String[]{sizeString + "000", "0"}, null);
        System.out.println(cursor.getCount());
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            ListItem<String, String> mp3Info = new ListItem<>();

            String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            mp3Info.put("title", title);

            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            mp3Info.put("artist", artist);

            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            mp3Info.put("data", data);

            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            mp3Info.put("album", album);

            String song_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            mp3Info.put("song_id", song_id);

            String album_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            mp3Info.put("album_id", album_id);

            String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            mp3Info.put("duration", duration);

            mp3Infos.add(mp3Info);
        }
        cursor.close();
    }

    private class LoadingTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mImageLogo.setAnimation(AnimationUtils.loadAnimation(BootUpScreenActivity.this, R.anim.up_appear_anim));
            mProgressBar.setAnimation(AnimationUtils.loadAnimation(BootUpScreenActivity.this, R.anim.fade_in));
            mProgressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //read pref
            SharedPreferences pref = getSharedPreferences("list_preference", Context.MODE_PRIVATE);
            SharedPreferences preferences = getSharedPreferences("preference", Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BootUpScreenActivity.this);
            String sizeString = sharedPreferences.getString("scan_size", "800");

            mApplication.setOrderMode(preferences.getBoolean("orderMode", false));
            //create folder
            String externalStorageState = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(externalStorageState)) {
                File rootPath = Environment.getExternalStorageDirectory();
                File lyricPath = new File(rootPath, "Crystal");
                if (!lyricPath.exists()) {
                    lyricPath.mkdirs();
                }
            }
            //create playlist
            List<ListItem<String, String>> mp3Infos = new ArrayList<>();
            String playlist = pref.getString("playlist", null);
            if (null != playlist && !"".equals(playlist)) {
                try {
                    JSONArray jsonPlayList = new JSONArray(playlist);
                    for (int i = 0; i < jsonPlayList.length(); i++) {
                        JSONObject jsonListItem = jsonPlayList.getJSONObject(i);
                        ListItem<String, String> mp3Info = new ListItem<>();

                        String title = jsonListItem.getString("title");
                        mp3Info.put("title", title);

                        String artist = jsonListItem.getString("artist");
                        mp3Info.put("artist", artist);

                        String data = jsonListItem.getString("data");
                        mp3Info.put("data", data);

                        String album = jsonListItem.getString("album");
                        mp3Info.put("album", album);

                        String song_id = jsonListItem.getString("song_id");
                        mp3Info.put("song_id", song_id);

                        String album_id = jsonListItem.getString("album_id");
                        mp3Info.put("album_id", album_id);

                        String duration = jsonListItem.getString("duration");
                        mp3Info.put("duration", duration);

                        mp3Infos.add(mp3Info);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    createListFromDB(mp3Infos, sizeString);
                }
            } else {
                createListFromDB(mp3Infos, sizeString);
            }
            Random random = new Random();
            int s;
            try {
                s = random.nextInt(mp3Infos.size());
            } catch (Exception e) {
                s = 0;
            }
            ListItem<String, String> tempTrack = new ListItem<>();
            tempTrack.put("song_id", pref.getString("currentTrack_ID", null));
            tempTrack.put("artist", pref.getString("currentTrack_ARTIST", null));
            tempTrack.put("album", pref.getString("currentTrack_ALBUM", null));
            tempTrack.put("title", pref.getString("currentTrack_TITLE", null));
            tempTrack.put("album_id", pref.getString("currentTrack_ALBUM_ID", null));
            tempTrack.put("duration", pref.getString("currentTrack_DURATION", null));
            tempTrack.put("data", pref.getString("currentTrack_DATA", null));
            if (tempTrack.get("song_id") != null && tempTrack.get("title") != null) {
                mApplication.setCurrentPlayList(mp3Infos).setCurrentAudio(tempTrack).setCurrentIndex(s);
            } else {
                if (mp3Infos.size() == 0) {
                    mp3Infos.add(new ListItem<String, String>());
                }
                mApplication.setCurrentPlayList(mp3Infos).setCurrentAudio(mp3Infos.get(s)).setCurrentIndex(s);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void none) {
            myHandler.sendEmptyMessageDelayed(1, 1500);
            myHandler.sendEmptyMessageDelayed(2, 2000);
            super.onPostExecute(none);
        }
    }

}
