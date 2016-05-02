package com.studio.jason.application.service;

import android.app.Service;

import com.studio.jason.application.MyApplication;
import com.studio.jason.application.customized_view.ListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jason on 2014/12/26.
 */
public class PlayControlHelper {

    private ListItem<String, String> mCurrentTrack;
    private List<ListItem<String, String>> mCurrentPlayList;
    private List<Integer> mShuffleOrder;
    private int orderIndex = 0;
    private int shuffleIndex = 0;
    private int listSize;
    private MyApplication mApplication;

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public List<ListItem<String, String>> getCurrentPlayList() {
        return mCurrentPlayList;
    }

    public void setCurrentPlayList(List<ListItem<String, String>> mCurrentPlayList) {
        this.mCurrentPlayList = mCurrentPlayList;
    }

    public void setCurrentTrack(ListItem<String, String> mCurrentTrack) {
        this.mCurrentTrack = mCurrentTrack;
    }

    public ListItem<String, String> getCurrentTrack() {
        return mCurrentTrack;
    }

    public PlayControlHelper(Service service, MyApplication myApp) {
        mApplication = myApp;
        initPlayList();
    }

    public void initPlayList() {
        orderIndex = mApplication.getCurrentIndex();
        mCurrentTrack = mApplication.getCurrentAudio();
        mCurrentPlayList = mApplication.getCurrentPlayList();
        updateIndex();
    }

    public void updateIndex(){
        listSize = mCurrentPlayList.size();
        if(null != mShuffleOrder){
            mShuffleOrder.clear();
            mShuffleOrder = null;
        }
        mShuffleOrder = new ArrayList<>();
        for(int i = 0; i<listSize; i++) {
            mShuffleOrder.add(i);
        }
        Collections.shuffle(mShuffleOrder);
        shuffleIndex = mShuffleOrder.indexOf(orderIndex);
    }

    public ListItem<String, String> getNextTrack(boolean isShuffleMode) {
        orderIndex++;
        shuffleIndex++;
//        if (orderIndex >= listSize) {
//            orderIndex = 0;
//            if(isShuffleMode){
//                setCurrentTrack(mCurrentPlayList.get(mShuffleOrder.get(orderIndex)));
//                return mCurrentPlayList.get(mShuffleOrder.get(orderIndex));
//            }else{
//            setCurrentTrack(mCurrentPlayList.get(orderIndex));
//            return mCurrentPlayList.get(orderIndex);}
//        } else {
//            if(isShuffleMode){
//                setCurrentTrack(mCurrentPlayList.get(mShuffleOrder.get(orderIndex)));
//                return mCurrentPlayList.get(mShuffleOrder.get(orderIndex));
//            }else {
//                setCurrentTrack(mCurrentPlayList.get(orderIndex));
//                return mCurrentPlayList.get(orderIndex);
//            }
//        }
        if(isShuffleMode){
            if(shuffleIndex >= listSize){
                shuffleIndex = 0;
            }
            setCurrentTrack(mCurrentPlayList.get(mShuffleOrder.get(shuffleIndex)));
            return mCurrentPlayList.get(mShuffleOrder.get(shuffleIndex));
        }else{
            if (orderIndex >= listSize) {
                orderIndex = 0;
            }
            setCurrentTrack(mCurrentPlayList.get(orderIndex));
            return mCurrentPlayList.get(orderIndex);
        }
    }

    public ListItem<String, String> getPreviousTrack(boolean isShuffleMode) {
        orderIndex--;
        shuffleIndex--;
        if(isShuffleMode){
            if(shuffleIndex < 0){
                shuffleIndex = listSize-1;
            }
            setCurrentTrack(mCurrentPlayList.get(mShuffleOrder.get(shuffleIndex)));
            return mCurrentPlayList.get(mShuffleOrder.get(shuffleIndex));
        }else{
            if (orderIndex < 0) {
                orderIndex = listSize-1;
            }
            setCurrentTrack(mCurrentPlayList.get(orderIndex));
            return mCurrentPlayList.get(orderIndex);
        }
//        if (orderIndex < 0) {
//            orderIndex = listSize - 1;
//            if(isShuffleMode){
//                setCurrentTrack(mCurrentPlayList.get(mShuffleOrder.get(orderIndex)));
//                return mCurrentPlayList.get(mShuffleOrder.get(orderIndex));
//            }else {
//                setCurrentTrack(mCurrentPlayList.get(orderIndex));
//                return mCurrentPlayList.get(orderIndex);
//            }
//        } else {
//            if(isShuffleMode){
//                setCurrentTrack(mCurrentPlayList.get(mShuffleOrder.get(orderIndex)));
//                return mCurrentPlayList.get(mShuffleOrder.get(orderIndex));
//            }else {
//                setCurrentTrack(mCurrentPlayList.get(orderIndex));
//                return mCurrentPlayList.get(orderIndex);
//            }
//        }
    }

    public ListItem<String, String> getFirstTrack() {
        return mApplication.getCurrentAudio();
    }
}