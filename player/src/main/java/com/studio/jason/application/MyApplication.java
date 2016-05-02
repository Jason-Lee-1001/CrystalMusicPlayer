package com.studio.jason.application;

import android.app.Application;

import com.studio.jason.application.customized_view.ListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2014/12/27.
 */
public class MyApplication extends Application {
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    private ListItem<String,String> mCurrentAudio;
    private List<ListItem<String,String>> mCurrentPlayList;
    private int mCurrentIndex = 0;
    private boolean orderMode = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mCurrentAudio = new ListItem<>();
        mCurrentPlayList = new ArrayList<>();
    }

    public List<ListItem<String, String>> getCurrentPlayList() {
        return mCurrentPlayList;
    }

    public MyApplication setCurrentPlayList(List<ListItem<String, String>> mCurrentPlayList) {
        this.mCurrentPlayList = mCurrentPlayList;
        return MyApplication.this;
    }

    public ListItem<String, String> getCurrentAudio() {
        return mCurrentAudio;
    }

    public MyApplication setCurrentAudio(ListItem<String, String> mCurrentAudio) {
        this.mCurrentAudio = mCurrentAudio;
        return MyApplication.this;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public boolean getOrderMode() {
        return orderMode;
    }

    public void setOrderMode(boolean orderMode) {
        this.orderMode = orderMode;
    }

    public MyApplication setCurrentIndex(int mCurrentIndex) {
        this.mCurrentIndex = mCurrentIndex;
        return MyApplication.this;
    }

}
