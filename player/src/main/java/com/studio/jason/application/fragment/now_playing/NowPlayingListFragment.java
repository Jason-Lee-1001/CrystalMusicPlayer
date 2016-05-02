package com.studio.jason.application.fragment.now_playing;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.studio.jason.application.MyApplication;
import com.studio.jason.application.R;
import com.studio.jason.application.activity.NowPlayingActivity;
import com.studio.jason.application.customized_adapter.NowPlayingListBaseAdapter;


public class NowPlayingListFragment extends Fragment {

    private NowPlayingListBaseAdapter mAdapter;
    private ListView mListView;
    private MyApplication mApplication;

    public NowPlayingListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new NowPlayingListBaseAdapter(getActivity());
        mApplication = (MyApplication)getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.now_playing_list_fragment, container, false);
        mListView = (ListView)v.findViewById(R.id.now_playing_song_list);
        mAdapter.setList(mApplication.getCurrentPlayList());
        mListView.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mApplication.setCurrentAudio(mApplication.getCurrentPlayList().get(position)).setCurrentIndex(position);
                ((NowPlayingActivity) getActivity()).getBindService().playNewList();
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

}
