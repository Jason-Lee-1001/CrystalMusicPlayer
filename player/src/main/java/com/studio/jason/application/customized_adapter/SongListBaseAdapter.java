package com.studio.jason.application.customized_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.studio.jason.application.R;
import com.studio.jason.application.customized_view.ListItem;
import com.studio.jason.application.static_data.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jason on 2014/12/14.
 */
public class SongListBaseAdapter extends BaseAdapter {

    private List<ListItem<String, String>> list;
    private LayoutInflater mInflater;
    private HashMap<String, Integer> selector;
    private Context mContext;

    public SongListBaseAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        List<ListItem<String, String>> temp = new ArrayList<>();
        this.setList(temp);
        this.mContext = context;
    }

    public HashMap<String, Integer> getSelector() {
        return selector;
    }

    public List<ListItem<String, String>> getList() {
        return list;
    }

    public void setList(List<ListItem<String, String>> list) {
        this.list = list;
        if (null != list) {
            String[] index = DataSource.indexLowerCaseAlphabet;
            selector = new HashMap<>();
            for (String anIndex : index) {
                for (int i = list.size() - 1; i > 0; i--) {
                    if (list.get(i).get("index").equals(anIndex))
                        selector.put(anIndex, i);
                }
            }
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public HashMap<String, String> getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.library_songlist_item, parent, false);
            holder.TitleTextView = (TextView) convertView.findViewById(R.id.textView);
            holder.SubTitleTextView = (TextView) convertView.findViewById(R.id.textView2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.TitleTextView.setText(this.list.get(position).get("title"));
        holder.SubTitleTextView.setText(this.list.get(position).get("artist"));
        return convertView;
    }

    static class ViewHolder {
        TextView TitleTextView;
        TextView SubTitleTextView;
    }
}
