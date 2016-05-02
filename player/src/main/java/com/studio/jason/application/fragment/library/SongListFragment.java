package com.studio.jason.application.fragment.library;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.studio.jason.application.MyApplication;
import com.studio.jason.application.R;
import com.studio.jason.application.activity.LibraryActivity;
import com.studio.jason.application.content_loader.SongListLoader;
import com.studio.jason.application.customized_adapter.SongListBaseAdapter;
import com.studio.jason.application.customized_view.ListItem;
import com.studio.jason.application.static_data.DataSource;
import com.studio.jason.application.system_frame.SongListFragmentMultiChoiceListener;
import com.studio.jason.application.utils.Pinyin4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SongListFragment extends Fragment {

    private SongListBaseAdapter mAdapter;
    private LoaderManager mManager;
    private ListView mListView;
    private LinearLayout mListIndex;
    private MyApplication mApplication;
    private TextView indexIndicator;
    private Cursor mResultCursor;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(1 == msg.what){
                indexIndicator.setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fade_out_scale));
                indexIndicator.setVisibility(View.GONE);
            }
            if(2 == msg.what){
                mAdapter.setList((List<ListItem<String, String>>)msg.obj);
            }
            super.handleMessage(msg);
        }
    };

    public SongListFragment() {
    }

    public void setResultCursor(Cursor result) {
        this.mResultCursor = result;
        new Thread(new MyThread()).start();
    }

    public ListView getListView() {
        return mListView;
    }

    public SongListBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.library_song_list_fragment, container, false);
        mListView = (ListView)v.findViewById(R.id.song_list);
        mListIndex = (LinearLayout)v.findViewById(R.id.list_index);
        mAdapter = new SongListBaseAdapter(getActivity());
        indexIndicator = (TextView)v.findViewById(R.id.songlist_index_indicator);
        mListView.setAdapter(mAdapter);
        mListIndex.setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.fade_in));
        mListIndex.setVisibility(View.VISIBLE);
        mListIndex.setOnTouchListener(new MyTouchListener());
        return v;
    }

    private class MyTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float y = event.getY();
            int index = (int) (y / ((LibraryActivity)getActivity()).indexCharHeight);
            if (index > -1 && index < 27) {
                String key = DataSource.indexLowerCaseAlphabet[index];
                if (mAdapter.getSelector().containsKey(key)) {
                    int pos = mAdapter.getSelector().get(key);
                    if (mListView.getHeaderViewsCount() > 0) {
                        mListView.setSelectionFromTop(
                                pos + mListView.getHeaderViewsCount(), 0);
                    } else {
                        mListView.setSelectionFromTop(pos, 0);
                    }
                    indexIndicator.setVisibility(View.VISIBLE);
                    indexIndicator.setText(DataSource.indexUpperCaseAlphabet[index]);
                }
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mListIndex.setBackgroundColor(getActivity().getResources().getColor(R.color.alpha_black));
                    break;

                case MotionEvent.ACTION_MOVE:

                    break;
                case MotionEvent.ACTION_UP:
                    mListIndex.setBackgroundColor(getActivity().getResources().getColor(R.color.alpha));
                    mHandler.sendEmptyMessageDelayed(1,2000);
                    break;
            }
            return true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(null != mListIndex){
            mListIndex.setBackgroundColor(getActivity().getResources().getColor(R.color.alpha));
        }
        mHandler.removeMessages(1);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mManager = getLoaderManager();
        mManager.initLoader(100,null,new SongListLoader(this,mAdapter));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mApplication = (MyApplication)(getActivity().getApplication());
                mApplication.setCurrentPlayList(SongListFragment.this.getAdapter().getList())
                        .setCurrentAudio(SongListFragment.this.getAdapter().getList().get(position))
                        .setCurrentIndex(position);
                ((LibraryActivity)getActivity()).getBindService().playNewList();
            }
        });
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new SongListFragmentMultiChoiceListener(this));
        super.onActivityCreated(savedInstanceState);
    }



    private class MyThread implements Runnable{
        @Override
        public void run() {
            List<ListItem<String, String>> mp3Infos = new ArrayList<>();
            for (mResultCursor.moveToFirst(); !mResultCursor.isAfterLast(); mResultCursor.moveToNext()) {
                ListItem<String, String> mp3Info = new ListItem<>();

                if(mResultCursor.getLong(mResultCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)) > 300000 && mResultCursor
                        .getInt(mResultCursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)) != 0) {

                    String title = mResultCursor.getString((mResultCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    mp3Info.put("title", title);

                    String artist = mResultCursor.getString(mResultCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    mp3Info.put("artist", artist);

                    String data = mResultCursor.getString(mResultCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    mp3Info.put("data", data);

                    String album = mResultCursor.getString(mResultCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    mp3Info.put("album", album);

                    String song_id = mResultCursor.getString(mResultCursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    mp3Info.put("song_id", song_id);

                    String album_id = mResultCursor.getString(mResultCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    mp3Info.put("album_id", album_id);

                    String duration = mResultCursor.getString(mResultCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    mp3Info.put("duration", duration);

                    try {
                        String index = String.valueOf(Pinyin4j.getHanyuPinyin(mp3Info.get("title")).charAt(0));
                        mp3Info.put("index", index);
                    } catch(Exception e){
                        mp3Info.put("index", "#");
                    }
                    mp3Infos.add(mp3Info);
                }
            }
            Collections.sort(mp3Infos);
            Message msg = Message.obtain();
            msg.obj = mp3Infos;
            msg.what = 2;
            mHandler.sendMessage(msg);
        }
    }
}
