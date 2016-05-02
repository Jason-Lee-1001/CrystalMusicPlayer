package com.studio.jason.application.service;


import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.studio.jason.application.MyApplication;
import com.studio.jason.application.R;
import com.studio.jason.application.activity.NowPlayingActivity;
import com.studio.jason.application.customized_view.ListItem;
import com.studio.jason.application.static_data.MediaPlayerState;


public class MediaPlayBackService extends Service implements OnErrorListener, OnPreparedListener, OnCompletionListener {

    public static final String ACTION_PLAY = "com.studio.jason.crystal.action_play";
    public static final String ACTION_PLAY_NEW_LIST = "com.studio.jason.crystal.action_play_new_list";
    public static final String ACTION_NEXT = "com.studio.jason.crystal.action_next";
    public static final String ACTION_UPDATE_UI = "com.studio.jason.crystal.update_ui";
    public static final String ACTION_PRE = "com.studio.jason.crystal.action_pre";
    public static final String ACTION_MODE = "com.studio.jason.crystal.play_mode";
    public static final String ACTION_REPEAT = "com.studio.jason.crystal.repeat_mode";
    public static final String ACTION_SLEEP_MODE = "com.studio.jason.crystal.sleep_mode";

    private PlayControlHelper mHelper;
    private MediaPlayer mPlayer;
    private MediaServiceBinder mBinder = new MediaServiceBinder();
    private OnPlayStateChangedListener mPlayStateChangedListener;
    private ListItem<String, String> mCurrentTrack;
    private MyApplication mApplication;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mContent;
    private int mCurrentState;
    private int mPausePosition;
    private boolean mOrderMode = false;
    private boolean mIsLooping = false;
    private boolean mIsInSleepMode = false;

    public MediaPlayBackService() {
    }

    public PlayControlHelper getHelper() {
        return mHelper;
    }

    public boolean isLooping() {
        return mIsLooping;
    }

    public void setLooping(boolean isLoop) {
        this.mIsLooping = isLoop;
    }

    public void setCurrentTrack(ListItem<String, String> mCurrentTrack) {
        mApplication.setCurrentAudio(mCurrentTrack);
        this.mCurrentTrack = mCurrentTrack;
    }

    public int getCurrentTrackDuration() {
        if (mCurrentState == MediaPlayerState.STARTED) {
            return mPlayer.getDuration();
        } else {
            return 0;
        }
    }

    public boolean isInSleepMode() {
        return mIsInSleepMode;
    }

    public void setSleepMode(boolean isInSleepMode) {
        this.mIsInSleepMode = isInSleepMode;
    }

    public boolean getOrderMode() {
        return mOrderMode;
    }

    public void setOrderMode(boolean mode) {
        this.mOrderMode = mode;
    }

    private void initMediaPlayer() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            changePlayerState(MediaPlayerState.IDLE);
        } else {
            if (mCurrentState == MediaPlayerState.IDLE
                    || mCurrentState == MediaPlayerState.INITIALIZED
                    || mCurrentState == MediaPlayerState.PREPARED
                    || mCurrentState == MediaPlayerState.STARTED
                    || mCurrentState == MediaPlayerState.PAUSED
                    || mCurrentState == MediaPlayerState.STOPPED
                    || mCurrentState == MediaPlayerState.COMPLETED
                    || mCurrentState == MediaPlayerState.ERROR) {
                mPlayer.reset();
                changePlayerState(MediaPlayerState.IDLE);
            }
        }
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
    }

    public int getPlayerCurrentState() {
        return mCurrentState;
    }

    public void startToPrepare(ListItem<String, String> map) {
        if (map.get("data") == null) {
            return;
        }
        Uri uri = Uri.parse(map.get("data"));
        this.mCurrentTrack = map;
        initMediaPlayer();
        try {
            if (mCurrentState == MediaPlayerState.IDLE) {
                mPlayer.setDataSource(this, uri);
            }
            changePlayerState(MediaPlayerState.INITIALIZED);
            if (mCurrentState != MediaPlayerState.ERROR) {
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            if (mCurrentState == MediaPlayerState.INITIALIZED
                    || mCurrentState == MediaPlayerState.STOPPED) {
                mPlayer.prepareAsync();
                changePlayerState(MediaPlayerState.PREPARING);
            }
        } catch (Exception e) {//IllegalStateException and IOException
            mPlayer.reset();
            startToPrepare(map);
            e.printStackTrace();
        }
    }

    private void doPlay() {
        mPlayer.start();
        mPlayer.setLooping(mIsLooping);
        changePlayerState(MediaPlayerState.STARTED);
        updateNotification();
    }

    private void changePlayerState(int state) {
        mCurrentState = state;
        if (mPlayStateChangedListener != null) {
            mPlayStateChangedListener.onStateChanged(mCurrentState);
        }
    }

    public void setOnPlayStateChangedListener(
            OnPlayStateChangedListener listener) {
        mPlayStateChangedListener = listener;
    }

    public void playOrPause() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                updateNotification();
                if(sleepTimeHandler.hasMessages(4)) {
                    sleepTimeHandler.removeMessages(4);
                    setSleepMode(false);
                }
                mPausePosition = mPlayer.getCurrentPosition();
                changePlayerState(MediaPlayerState.PAUSED);
            } else if (mCurrentState == MediaPlayerState.PAUSED) {
                mPlayer.seekTo(mPausePosition);
                mPlayer.start();
                updateNotification();
                mPausePosition = 0;
                changePlayerState(MediaPlayerState.STARTED);
            }
        } else {
            try {
                setCurrentTrack(mHelper.getFirstTrack());
                startToPrepare(mCurrentTrack);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void pause(){
        if(mPlayer != null){
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                updateNotification();
                if(sleepTimeHandler.hasMessages(4)) {
                    sleepTimeHandler.removeMessages(4);
                    setSleepMode(false);
                }
                mPausePosition = mPlayer.getCurrentPosition();
                changePlayerState(MediaPlayerState.PAUSED);
            }
        }
    }

    public void playNewList() {
        mHelper.initPlayList();
        setCurrentTrack(mHelper.getFirstTrack());
        startToPrepare(mCurrentTrack);
//        mHelper.setOrderIndex(mApplication.getCurrentIndex());
        if (mPlayStateChangedListener != null) {
            mPlayStateChangedListener.onUpdateTrackInfo(mCurrentTrack);
        }
    }

    public void seekTo(int progress) {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.seekTo((mPlayer.getDuration() / 300) * progress);
        }
    }

    public int getMilliSecondProgress() {
        if (mPlayer != null && mCurrentState == 4)
            return mPlayer.getCurrentPosition();
        else {
            return 0;
        }
    }

    public void playNextTrack() {
        try {
            setCurrentTrack(mHelper.getNextTrack(mOrderMode));
            startToPrepare(mCurrentTrack);
            if (null != mPlayStateChangedListener) {
                mPlayStateChangedListener.onUpdateTrackInfo(mCurrentTrack);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void playPreviousTrack() {
        try {
            setCurrentTrack(mHelper.getPreviousTrack(mOrderMode));
            startToPrepare(mCurrentTrack);
            if (null != mPlayStateChangedListener) {
                mPlayStateChangedListener.onUpdateTrackInfo(mCurrentTrack);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private PendingIntent next_PendingIntent() {
        Intent intent = new Intent(ACTION_NEXT);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    private PendingIntent play_PendingIntent() {
        Intent intent = new Intent(ACTION_PLAY);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }


    public void updateNotification() {
        if(mCurrentTrack != null) {
            mContent.setTextViewText(R.id.notification_title, mCurrentTrack.get("title"));
            mContent.setTextViewText(R.id.notification_sub_title, mCurrentTrack.get("artist"));
            mContent.setOnClickPendingIntent(R.id.notification_play, play_PendingIntent());
            mContent.setOnClickPendingIntent(R.id.notification_next, next_PendingIntent());
            if(mPlayer != null && mPlayer.isPlaying()){
                mContent.setImageViewResource(R.id.notification_play,R.drawable.notification_bar_pause);
            }
            if(mPlayer != null && !mPlayer.isPlaying()){
                mContent.setImageViewResource(R.id.notification_play,R.drawable.notification_bar_play);
            }
            mBuilder.setTicker(mCurrentTrack.get("title"));
            startForeground(100, mBuilder.build());
        }
    }

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_UPDATE_UI);
        filter.addAction(ACTION_PRE);
        filter.addAction(ACTION_MODE);
        filter.addAction(ACTION_REPEAT);
        filter.addAction(ACTION_SLEEP_MODE);
        filter.addAction(ACTION_PLAY_NEW_LIST);
//        filter.addAction(Intent.ACTION_MEDIA_BUTTON);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
//        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(MediaPlayBackService.this.mIntentReceiver, filter);

        mApplication = (MyApplication) getApplication();
        mHelper = new PlayControlHelper(this, mApplication);
        mOrderMode = mApplication.getOrderMode();
        mCurrentTrack = mApplication.getCurrentAudio();
        mBuilder = new NotificationCompat.Builder(this);
        mContent = new RemoteViews(getPackageName(), R.layout.remote_view_layout);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 3, new Intent(this, NowPlayingActivity.class), 0);
        mBuilder.setSmallIcon(R.drawable.status_bar_indicator).setAutoCancel(false)
                .setContent(mContent).setContentIntent(pendingIntent);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        player.stop();
        playNextTrack();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        doPlay();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public static interface OnPlayStateChangedListener {
        public void onStateChanged(int state);
        public void onUpdateTrackInfo(ListItem<String, String> currentTrack);
    }

    public class MediaServiceBinder extends Binder {
        public MediaPlayBackService getService() {
            return MediaPlayBackService.this;
        }
    }

    private Handler sleepTimeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 4){
                pause();
                setSleepMode(false);
            }
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ACTION_PLAY.equals(intent.getAction())){
                playOrPause();
            }
            if(ACTION_PLAY_NEW_LIST.equals(intent.getAction())){
                playNewList();
            }
            if(ACTION_NEXT.equals(intent.getAction())){
                playNextTrack();
            }
            if(ACTION_UPDATE_UI.equals(intent.getAction())){
                mPlayStateChangedListener.onUpdateTrackInfo(mCurrentTrack);
            }
            if(ACTION_PRE.equals(intent.getAction())){
                playPreviousTrack();
            }
            if(ACTION_SLEEP_MODE.equals(intent.getAction())){
                Bundle bundle = intent.getExtras();
                int time = bundle.getInt("sleep_time");
                System.out.println("######### receive sleep mode"+time);
                if(time == 0){
                    if(sleepTimeHandler.hasMessages(4)) {
                        sleepTimeHandler.removeMessages(4);
                        setSleepMode(false);
                    }
                }else {
                    Message msg = Message.obtain();
                    msg.what = 4;
                    int arg0 = time * 60 * 1000;
//                    int arg0 = time*1000;
                    setSleepMode(true);
                    sleepTimeHandler.sendMessageDelayed(msg, arg0);
                }
            }
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()) ||
                    TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction()) ||
                    Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
                pause();
            }
//            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())){
////                KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
////                if(event!=null) {
////                    boolean isActionUp = (event.getAction() == KeyEvent.ACTION_UP);
////                    if (isActionUp) {
////                        int keyCode = event.getKeyCode();
////                        switch(keyCode){
////                            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
////                                playOrPause();
////                                break;
////                            case KeyEvent.KEYCODE_MEDIA_NEXT:
////                                    playNextTrack();
////                                break;
////                            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
////                                    playPreviousTrack();
////                                break;
////                        }
////                    }
////                }
//                KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
//                int keycode = event.getKeyCode();
//                System.out.println("keycode:"+keycode);
//                int action = event.getAction();
//
//                //Switch through each event and perform the appropriate action based on the intent that's ben
//                if (keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
//                        || keycode == KeyEvent.KEYCODE_HEADSETHOOK
//                        || keycode == KeyEvent.KEYCODE_MEDIA_PLAY
//                        || keycode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
//                    System.out.println("------play or pause media button receive-----");
//                    if (action == KeyEvent.ACTION_DOWN) {
//                        playOrPause();
//                    }
//                }
//
//                if (keycode == KeyEvent.KEYCODE_MEDIA_NEXT) {
//                    System.out.println("------next media button receive-----");
//                    if (action == KeyEvent.ACTION_DOWN) {
//                        playNextTrack();
//                    }
//                }
//
//                if (keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
//                    System.out.println("------previous media button receive-----");
//                    if (action == KeyEvent.ACTION_DOWN) {
//                        playPreviousTrack();
//                    }
//                }
//                abortBroadcast();
//            }
            if(ACTION_MODE.equals(intent.getAction())){
                setOrderMode(!mOrderMode);
                SharedPreferences pref = getSharedPreferences("preference", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("orderMode",mOrderMode).apply();
                if(mOrderMode) {
                    Toast.makeText(MediaPlayBackService.this,getString(R.string.shuffle_is_on), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MediaPlayBackService.this, getString(R.string.shuffle_is_off), Toast.LENGTH_SHORT).show();
                }
            }
            if(ACTION_REPEAT.equals(intent.getAction())){
                setLooping(!mIsLooping);
                if(null != MediaPlayBackService.this.mPlayer && MediaPlayBackService.this.mCurrentState == MediaPlayerState.STARTED){
                    MediaPlayBackService.this.mPlayer.setLooping(MediaPlayBackService.this.mIsLooping);
                }
                if(mIsLooping) {
                    Toast.makeText(MediaPlayBackService.this, getString(R.string.looping_is_on), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MediaPlayBackService.this, getString(R.string.looping_is_off), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

}
