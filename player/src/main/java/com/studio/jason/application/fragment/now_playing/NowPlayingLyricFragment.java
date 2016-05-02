package com.studio.jason.application.fragment.now_playing;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.studio.jason.application.R;
import com.studio.jason.application.customized_view.ListItem;
import com.studio.jason.application.http.HttpUtils;
import com.studio.jason.application.ui.LrcContent;
import com.studio.jason.application.ui.LrcProcess;
import com.studio.jason.application.ui.LrcView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NowPlayingLyricFragment extends Fragment {

    private LrcProcess mLrcProcess;	//歌词处理
    private List<LrcContent> lrcList = new ArrayList<>(); //存放歌词列表对象
    private int index = 0;			//歌词检索值
    private LrcView lrcView;
    private ListItem<String,String> mCurrentTrack;
    private String fileName;
    private String folderPath;
//    private Button mDownloadButton;
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private boolean isRunning = false;
    private Thread myThread;

    public NowPlayingLyricFragment() {

    }

    public void setCurrentTrack(ListItem<String, String> mCurrentTrack) {
        this.mCurrentTrack = mCurrentTrack;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            folderPath = Environment.getExternalStorageDirectory().getPath()+"/Crystal";
            mLrcProcess = new LrcProcess(folderPath);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.now_playing_lyric_fragment, container, false);
        lrcView = (LrcView)v.findViewById(R.id.now_playing_lyric_text);
//        mDownloadButton = (Button)v.findViewById(R.id.download_lrc);
        mTextView = (TextView)v.findViewById(R.id.search_internet);
        mProgressBar = (ProgressBar)v.findViewById(R.id.lrc_progressBar);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRunning){
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);
                myThread = new Thread(new MyThread());
                myThread.start();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        updateTrackInfo();
        super.onResume();
    }

    @Override
    public void onPause() {
        if (myThread != null) {
            myThread.interrupt();
            myThread = null;
        }
        super.onPause();
    }

    public void updateTrackInfo(){
        //读取歌词文件
        try {
            if(lrcList != null){
                lrcList.clear();
            }
            fileName = mCurrentTrack.get("title").replaceAll("[^\\w]","")+"_"
                    +mCurrentTrack.get("artist").replaceAll("[^\\w]","")+".lrc";
            boolean hasContent = mLrcProcess.readLRC(fileName);
            lrcList = mLrcProcess.getLrcList();
            lrcView.setmLrcList(lrcList);
            lrcView.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.lyric_anim));
            if(hasContent){
//                mDownloadButton.setVisibility(View.INVISIBLE);
                mTextView.setVisibility(View.INVISIBLE);
            }else {
//                mDownloadButton.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.VISIBLE);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isAdded()) {
                if (msg.what == 1) {
                    mProgressBar.setVisibility(View.INVISIBLE);
//                    mDownloadButton.setVisibility(View.INVISIBLE);
                    mTextView.setVisibility(View.INVISIBLE);
                    updateTrackInfo();
                }
                if (msg.what == 0) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), getString(R.string.lryic_not_found), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private class MyThread implements Runnable{
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName()+"-->start thread");
            boolean flag = false;
            isRunning = true;
            String url = HttpUtils.getLyricDownloadUrl(mCurrentTrack.get("title"));
            if(url != null && url.length()>23){
                flag = HttpUtils.downLyric(url,folderPath + File.separator + fileName);
            }
            Message msg = Message.obtain();
            if(flag) {
                msg.what = 1;
            }else{
                msg.what = 0;
            }
            mHandler.sendMessage(msg);
            isRunning = false;
            System.out.println(Thread.currentThread().getName()+"-->end thread");

        }
    }

    public void updateLyric(int currentTime, int duration) {
        lrcView.setIndex(lrcIndex(currentTime,duration));
        lrcView.invalidate();
    }

    /**
     * 根据时间获取歌词显示的索引值
     * @return
     */
    public int lrcIndex(int currentTime, int duration) {

        if(currentTime < duration) {
            for (int i = 0; i < lrcList.size(); i++) {
                if (i < lrcList.size() - 1) {
                    if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
                        index = i;
                    }
                    if (currentTime > lrcList.get(i).getLrcTime()
                            && currentTime < lrcList.get(i + 1).getLrcTime()) {
                        index = i;
                    }
                }
                if (i == lrcList.size() - 1
                        && currentTime > lrcList.get(i).getLrcTime()) {
                    index = i;
                }
            }
        }
        return index;
    }
}
