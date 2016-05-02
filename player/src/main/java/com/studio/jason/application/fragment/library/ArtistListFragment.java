package com.studio.jason.application.fragment.library;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.studio.jason.application.R;
import com.studio.jason.application.activity.DetailActivity;
import com.studio.jason.application.content_loader.ArtistListLoader;
import com.studio.jason.application.customized_adapter.ArtistListBaseAdapter;

/**
 * Created by Jason on 2014/12/14.
 */
public class ArtistListFragment extends Fragment {

    private ArtistListBaseAdapter mAdapter;
    private LoaderManager mManager;
    private ListView mListView;

    public ArtistListFragment() {
    }

    public ListView getListView() {
        return mListView;
    }

    public ArtistListBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ArtistListBaseAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.library_artist_list_fragment, container, false);
        mListView = (ListView)v.findViewById(R.id.artist_list);
        mListView.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mManager = getLoaderManager();
        mManager.initLoader(101,null,new ArtistListLoader(getActivity(),mAdapter));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("id",id);
                bundle.putInt("flag",1);
                bundle.putString("title",mAdapter.getItem(position).get("artist").toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        super.onActivityCreated(savedInstanceState);
    }
}