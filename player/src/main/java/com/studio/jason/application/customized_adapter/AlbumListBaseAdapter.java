package com.studio.jason.application.customized_adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.studio.jason.application.R;
import com.studio.jason.application.utils.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jason on 2014/12/14.
 */
public class AlbumListBaseAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    private List<HashMap<String, Object>> list;
    private LayoutInflater mInflater;
    private GridView mGridView;
    private Context mContext;
    private ImageLoader mImageLoader;

    private boolean isFirstEnter = true;
    private int mFirstVisibleItem;
    private int mVisibleItemCount;

    public AlbumListBaseAdapter(Context context, GridView gridView) {
        this.mContext = context;
        this.mGridView = gridView;
        this.mInflater = LayoutInflater.from(context);
        this.mImageLoader = new ImageLoader(context);
        List<HashMap<String, Object>> temp = new ArrayList<>();
        this.setList(temp);
        this.mGridView.setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
            showImage(mFirstVisibleItem, mVisibleItemCount);
        }else{
            cancelTask();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        if(isFirstEnter && visibleItemCount > 0){
            showImage(mFirstVisibleItem,mVisibleItemCount);
            isFirstEnter = false;
        }
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
        String pathString;
        try{
            pathString = list.get(position).get("artWork").toString();
        } catch (Exception e){
            pathString = "NoAlbum";
        }
        String subString = pathString.substring(pathString.lastIndexOf("/")+1);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.library_albumlist_item, parent, false);
            holder.albumImageView = (ImageView) convertView.findViewById(R.id.album_item_imageView);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.album_item_textView1);
            holder.subTitleTextView = (TextView) convertView.findViewById(R.id.album_item_textView2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.titleTextView.setText(this.list.get(position).get("album").toString());
        holder.subTitleTextView.setText(this.list.get(position).get("artist").toString());
        holder.albumImageView.setTag(subString);

        Bitmap bitmap = mImageLoader.showCacheBitmap(subString);
        if(bitmap != null){
            holder.albumImageView.setImageBitmap(bitmap);
        }else{
            holder.albumImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_album_cover));
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView albumImageView;
        TextView titleTextView;
        TextView subTitleTextView;
    }

    private void showImage(int firstVisibleItem, int visibleItemCount){
        Bitmap bitmap = null;
        for(int i=firstVisibleItem; i<firstVisibleItem+visibleItemCount; i++){
            try {
                String mImagepath = list.get(i).get("artWork").toString();
                final ImageView mImageView = (ImageView) mGridView.findViewWithTag(mImagepath.substring(mImagepath.lastIndexOf("/")+1));
                bitmap = mImageLoader.downloadImage(mImagepath, new ImageLoader.onImageLoaderListener() {
                    @Override
                    public void onImageLoader(Bitmap bitmap, String fullpath) {
                        if(mImageView != null && bitmap != null){
                            mImageView.setAnimation(AnimationUtils.loadAnimation(mContext,R.anim.fade));
                            mImageView.setImageBitmap(bitmap);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelTask(){
        mImageLoader.cancelTask();
    }
}
