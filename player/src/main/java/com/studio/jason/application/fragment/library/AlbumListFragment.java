package com.studio.jason.application.fragment.library;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.studio.jason.application.R;
import com.studio.jason.application.activity.DetailActivity;
import com.studio.jason.application.content_loader.AlbumListLoader;
import com.studio.jason.application.customized_adapter.AlbumListBaseAdapter;

/**
 * Created by Jason on 2014/12/14.
 */
public class AlbumListFragment extends Fragment {

    private AlbumListBaseAdapter mAdapter;
    private LoaderManager mManager;
    private GridView mGridView;

    public AlbumListFragment() {
    }

    public GridView getGridView() {
        return mGridView;
    }

    public AlbumListBaseAdapter getmAdapter() {
        return mAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.library_album_list_fragment, container, false);
        mGridView = (GridView)v.findViewById(R.id.gridView);
        mAdapter = new AlbumListBaseAdapter(getActivity(),mGridView);
        mGridView.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mManager = getLoaderManager();
        mManager.initLoader(101,null,new AlbumListLoader(getActivity(),mAdapter));
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("flag",2);
                bundle.putLong("id",id);
                bundle.putString("title",mAdapter.getItem(position).get("album").toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        super.onActivityCreated(savedInstanceState);
    }
}