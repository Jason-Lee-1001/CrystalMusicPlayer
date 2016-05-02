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
public class SearchTrackAdapter extends BaseAdapter {

    private List<ListItem<String, String>> list;
    private LayoutInflater mInflater;
    private Context mContext;

    public SearchTrackAdapter(Context context) {
        this.mContext = context;
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
            convertView = mInflater.inflate(R.layout.search_list_item, parent, false);
            holder.numberTextView = (TextView) convertView.findViewById(R.id.search_item_textView1);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.search_item_textView2);
            holder.subTitleTextView = (TextView) convertView.findViewById(R.id.search_item_textView3);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(position < 9){
            holder.numberTextView.setText("0"+(position + 1));
        }else{
            holder.numberTextView.setText(""+(position + 1));
        }
        holder.titleTextView.setText(this.list.get(position).get("title"));
        holder.subTitleTextView.setText(this.list.get(position).get("artist"));
        return convertView;
    }

    static class ViewHolder {
        TextView numberTextView;
        TextView titleTextView;
        TextView subTitleTextView;
    }
}
