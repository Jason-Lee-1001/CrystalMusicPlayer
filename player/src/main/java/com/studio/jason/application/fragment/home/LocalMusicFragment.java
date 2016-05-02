package com.studio.jason.application.fragment.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.studio.jason.application.R;
import com.studio.jason.application.activity.DetailActivity;
import com.studio.jason.application.activity.LibraryActivity;
import com.studio.jason.application.activity.SettingsActivity;
import com.studio.jason.application.static_data.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jason on 2014/12/20.
 */
public class LocalMusicFragment extends Fragment {

    private SimpleAdapter mAdapter;
    private ListView mListView;
    private TextView mSettingText;
    private List<HashMap<String, Object>> itemList;

    public LocalMusicFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        {
            itemList = new ArrayList<>();

            HashMap<String, Object> map1 = new HashMap<>();
            map1.put("name", getResources().getString(R.string.all_music));
            map1.put("icon", DataSource.iconId[0]);
            itemList.add(map1);

            HashMap<String, Object> map2 = new HashMap<>();
            map2.put("name", getResources().getString(R.string.fav_list));
            map2.put("icon", DataSource.iconId[1]);
            itemList.add(map2);

            HashMap<String, Object> map3 = new HashMap<>();
            map3.put("name", getResources().getString(R.string.add_list));
            map3.put("icon", DataSource.iconId[2]);
            itemList.add(map3);
        }

        mAdapter = new SimpleAdapter(getActivity(), itemList, R.layout.homelist_item,
                new String[]{"icon", "name"}, new int[]{R.id.home_imageView, R.id.home_textView});
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.home_local_music_fragment, container, false);
        mListView = (ListView) v.findViewById(R.id.home_local_list);
        mSettingText = (TextView) v.findViewById(R.id.home_local_settings);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intent1 = new Intent(getActivity(), LibraryActivity.class);
                        startActivity(intent1);
                        break;
                    case 1:
                        Intent intent2 = new Intent(getActivity(), DetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("flag",3);
                        bundle.putLong("id", 0);
                        bundle.putString("title",getString(R.string.fav_list));
                        intent2.putExtras(bundle);
                        startActivity(intent2);
                        break;
                    case 2:
                        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.create_playlist_dialog, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(LocalMusicFragment.this.getActivity());
                        builder.setView(dialogView).setPositiveButton(R.string.dialog_confirm,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(LocalMusicFragment.this.getActivity(),"功能性演示",Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                        break;
                }
            }
        });
        mSettingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mListView.setAdapter(mAdapter);
        super.onActivityCreated(savedInstanceState);
    }
}
