package com.studio.jason.application.customized_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.studio.jason.application.R;
import com.studio.jason.application.customized_view.ListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2014/12/14.
 */
public class NowPlayingListBaseAdapter extends BaseAdapter {

    private List<ListItem<String, String>> list;
    private LayoutInflater mInflater;

    public NowPlayingListBaseAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        List<ListItem<String, String>> temp = new ArrayList<>();
        this.setList(temp);
    }

    public List<ListItem<String, String>> getList() {
        return list;
    }

    public void setList(List<ListItem<String, String>> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public ListItem<String, String> getItem(int position) {
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
            convertView = mInflater.inflate(R.layout.now_playing_songlist_item, parent, false);
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
