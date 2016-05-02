package com.studio.jason.application.customized_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.studio.jason.application.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jason on 2014/12/14.
 */
public class ArtistListBaseAdapter extends BaseAdapter {

    private List<HashMap<String, Object>> list;
    private LayoutInflater mInflater;
    private Context mContext;

    public ArtistListBaseAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        List<HashMap<String, Object>> temp = new ArrayList<>();
        this.setList(temp);
    }

    public List<HashMap<String, Object>> getList() {
        return list;
    }

    public void setList(List<HashMap<String, Object>> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public HashMap<String, Object> getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (Long)list.get(position).get("id");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.library_artistlist_item, parent, false);
//            holder.artistImageView = (ImageView) convertView.findViewById(R.id.imageView);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.textView3);
            holder.subTitleTextView = (TextView) convertView.findViewById(R.id.textView4);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        holder.artistImageView.setImageBitmap();
        holder.titleTextView.setText(this.list.get(position).get("artist").toString());
        holder.subTitleTextView.setText(this.list.get(position).get("count") + this.mContext.getResources().getString(R.string.songs_count));
        return convertView;
    }

    static class ViewHolder {
        ImageView artistImageView;
        TextView titleTextView;
        TextView subTitleTextView;
    }
}
