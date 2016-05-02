package com.studio.jason.application.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.studio.jason.application.R;
import com.studio.jason.application.customized_view.ListItem;
import com.studio.jason.application.fragment.now_playing.NowPlayingListFragment;
import com.studio.jason.application.fragment.now_playing.NowPlayingLyricFragment;
import com.studio.jason.application.fragment.now_playing.NowPlayingMainFragment;
import com.studio.jason.application.graphic_tool.ArtWorkHelper;
import com.studio.jason.application.graphic_tool.FastBlur;
import com.studio.jason.application.local_database.DataBaseOperator;
import com.studio.jason.application.service.MediaPlayBackService;
import com.studio.jason.application.static_data.MediaPlayerState;
import com.studio.jason.application.utils.TimeSwitcher;
import com.studio.jason.application.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;


public class NowPlayingActivity extends FragmentActivity implements PopupMenu.OnMenuItemClickListener, ViewSwitcher.ViewFactory {

    public static final String TAG = "NowPlayingActivity";
    //UI components statement
    private ViewPager mViewPager;
    private View mHeadView, mFootView;
    private ImageView mImageBackground, mImageIndicator;
    private ImageSwitcher mAlbumImage;
    private Button mPlayBtn, mPreviousBtn, mNextBtn, mBackBtn, mLikeBtn;
    private TextView mMarginTitle, mSubTitle, mCurrentPosition, mDuration;
    private SeekBar mSeekBar;
    //---------------------------------------------------------------------
    //flag statement
    private boolean serviceIsBind;
    private int mPlayerState;
    private boolean mActivityIsFront = false;
    private boolean mSeekedByUser = false;
    //---------------------------------------------------------------------
    //handler statement for dealing with seek bar progress
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mPlayerState == MediaPlayerState.STARTED) {
                mDuration.setText(TimeSwitcher.getNormalTimeFormat(msg.arg2));
                mCurrentPosition.setText(TimeSwitcher.getNormalTimeFormat(msg.arg1));
                if (!mSeekedByUser) {
                    mSeekBar.setProgress(msg.arg1 * 300 / mService.getCurrentTrackDuration());
                }
            }
            if (mViewPager.getCurrentItem() == 2) {
                mFragmentLyric.updateLyric(msg.arg1, msg.arg2);
            }
            super.handleMessage(msg);
        }
    };
    //---------------------------------------------------------------------
    //system components statement
    private MediaPlayBackService mService;
    private FragmentManager mManager;
    private List<Fragment> mFragmentList;
    private NowPlayingMainFragment mFragmentMain;
    private NowPlayingListFragment mFragmentPlaylist;
    private NowPlayingLyricFragment mFragmentLyric;
    private MyFragmentAdapter mAdapter;
    private MediaPlayBackService.MediaServiceBinder mBinder;
    private Thread myThread;
    //---------------------------------------------------------------------
    //service connection statement
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MediaPlayBackService.MediaServiceBinder) service;
            mService = mBinder.getService();
            serviceIsBind = true;
            mPlayerState = mService.getPlayerCurrentState();
            sendBroadcast(new Intent(MediaPlayBackService.ACTION_UPDATE_UI));
            if (mPlayerState == MediaPlayerState.STARTED && mActivityIsFront) {
                myThread = new Thread(new MyThread());
                myThread.start();
            }
            mService.setOnPlayStateChangedListener(new MediaPlayBackService.OnPlayStateChangedListener() {

                @Override
                public void onStateChanged(int state) {
                    mPlayerState = state;
                    if (state == MediaPlayerState.STARTED && mActivityIsFront) {
                        if (myThread != null) {
                            myThread.interrupt();
                            myThread = null;
                        }
                        myThread = new Thread(new MyThread());
                        myThread.start();
                    }
                    updateButtonState();
                }

                @Override
                public void onUpdateTrackInfo(ListItem<String, String> currentTrack) {
                    updateUI(currentTrack);
                    mFragmentLyric.setCurrentTrack(currentTrack);
                    mFragmentLyric.updateTrackInfo();
                }
            });
            updateButtonState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceIsBind = false;
        }
    };
    private int sleepTime = 30;

    public MediaPlayBackService getBindService() {
        return mService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.now_playing_activity_layout);
        //initialize components
        initViewComponents();
        mFragmentMain.setOnFragmentIsVisibleListener(new NowPlayingMainFragment.OnFragmentIsVisible() {
            @Override
            public void onViewVisible() {
                if (mAlbumImage == null) {
                    mAlbumImage = (ImageSwitcher) mFragmentMain.getView().findViewById(R.id.now_playing_main_imageView);
                    mAlbumImage.setFactory(NowPlayingActivity.this);
                    mAlbumImage.setInAnimation(NowPlayingActivity.this, R.anim.slide_in);
                    mAlbumImage.setOutAnimation(NowPlayingActivity.this, R.anim.slide_out);
                }
                sendBroadcast(new Intent(MediaPlayBackService.ACTION_UPDATE_UI));
            }
        });
    }

    private void initViewComponents() {
        mHeadView = this.findViewById(R.id.now_playing_header);
        mFootView = this.findViewById(R.id.now_playing_bottom);
        mViewPager = (ViewPager) this.findViewById(R.id.now_playing_viewpager);
        mViewPager.setOffscreenPageLimit(2);
        //---------------------------------------------------------------------
        mImageBackground = (ImageView) this.findViewById(R.id.now_playing_bg);
//        mImageBackground.setFactory(this);
//        mImageBackground.setInAnimation(this,R.anim.slow_fade_in);
//        mImageBackground.setOutAnimation(this,R.anim.slow_fade_out);
        mImageIndicator = (ImageView) this.findViewById(R.id.now_playing_indicator);
        mPlayBtn = (Button) this.findViewById(R.id.now_playing_play);
        mPreviousBtn = (Button) this.findViewById(R.id.now_playing_pre);
        mNextBtn = (Button) this.findViewById(R.id.now_playing_next);
        mBackBtn = (Button) this.findViewById(R.id.now_playing_back);
        mLikeBtn = (Button) this.findViewById(R.id.now_playing_like);
        mMarginTitle = (TextView) this.findViewById(R.id.now_playing_big_title);
        mSubTitle = (TextView) this.findViewById(R.id.now_playing_sub_title);
        mCurrentPosition = (TextView) this.findViewById(R.id.now_playing_start_time);
        mDuration = (TextView) this.findViewById(R.id.now_playing_end_time);
        mSeekBar = (SeekBar) this.findViewById(R.id.now_playing_seekBar);
        //---------------------------------------------------------------------
        mManager = getSupportFragmentManager();
        mFragmentList = new ArrayList<>();
        mFragmentMain = new NowPlayingMainFragment();
        mFragmentPlaylist = new NowPlayingListFragment();
        mFragmentLyric = new NowPlayingLyricFragment();
        mAdapter = new MyFragmentAdapter(mManager);
        mFragmentList.add(mFragmentPlaylist);
        mFragmentList.add(mFragmentMain);
        mFragmentList.add(mFragmentLyric);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(1);
        //---------------------------------------------------------------------
        mHeadView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.now_playing_header_appear));
        mViewPager.setAnimation(AnimationUtils.loadAnimation(this, R.anim.now_playing_viewpager_appear));
        mFootView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.now_playing_bottom_appear));
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                System.out.println("position:"+position+",positionOffset:"+positionOffset+"positionOffsetPixels:"+positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mImageIndicator.setImageResource(R.drawable.indicator1);
                        break;
                    case 1:
                        mImageIndicator.setImageResource(R.drawable.indicator2);
                        break;
                    case 2:
                        mImageIndicator.setImageResource(R.drawable.indicator3);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mPlayBtn.setOnClickListener(new ButtonClickListener());
        mPreviousBtn.setOnClickListener(new ButtonClickListener());
        mNextBtn.setOnClickListener(new ButtonClickListener());
        mPreviousBtn.setOnClickListener(new ButtonClickListener());
        mBackBtn.setOnClickListener(new ButtonClickListener());
        mLikeBtn.setOnClickListener(new ButtonClickListener());
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int pro, boolean fromUser) {
                if (fromUser) {
                    mSeekedByUser = true;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekedByUser = false;
                NowPlayingActivity.this.mService.seekTo(mSeekBar.getProgress());
            }
        });
    }

    @Override
    public View makeView() {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundColor(getResources().getColor(R.color.alpha));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return imageView;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.i(TAG, "onWindowFocusChanged");
        super.onWindowFocusChanged(hasFocus);
        ViewUtils.adjustViewSize(mViewPager);
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        mActivityIsFront = true;
        Intent intent = new Intent(this, MediaPlayBackService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("keep_screen_on", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        mActivityIsFront = false;
        if (myThread != null) {
            myThread.interrupt();
        }
        if (serviceIsBind) {
            this.unbindService(mConnection);
            serviceIsBind = false;
        }
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.activity_out_down, R.anim.activity_in_down);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateUI(ListItem<String, String> currentTrack) {
        updateButtonState();
        if (null != currentTrack) {
            try {
                mMarginTitle.setText(currentTrack.get("title"));
                String artist;
                if ("<unknown>".equals(currentTrack.get("artist"))) {
                    artist = getResources().getString(R.string.unknown_artist);
                } else {
                    artist = currentTrack.get("artist");
                }
                mSubTitle.setText(artist + " | " + currentTrack.get("album"));
                mDuration.setText(TimeSwitcher.getNormalTimeFormat(mService.getCurrentTrackDuration()));
            } catch (Exception e) {
                mMarginTitle.setText("");
                mSubTitle.setText("");
                mDuration.setText("00:00");
            }

            Bitmap album;
            Bitmap background;
            try {
                long id = Long.parseLong(currentTrack.get("album_id"));
                album = ArtWorkHelper.getAlbumArtWorkFromFile(this, id, -1, 400, 400, false);
                background = ArtWorkHelper.getAlbumArtWorkFromFile(this, id, -1, 40, 40, true);
            } catch (Exception e) {
                e.printStackTrace();
                album = ArtWorkHelper.getDefaultArtwork(this, false);
                background = BitmapFactory.decodeResource(getResources(), R.drawable.now_playing_default_color_bg);
            }
            mAlbumImage.setImageDrawable(new BitmapDrawable(getResources(), album));
            Bitmap newbm = FastBlur.doBlur(background, 4, false);
//            mImageBackground.setImageDrawable(new BitmapDrawable(getResources(),newbm));
            mImageBackground.setImageBitmap(newbm);
            mImageBackground.setAnimation(AnimationUtils.loadAnimation(NowPlayingActivity.this, R.anim.fast_fade_in));
        }
    }

    private void updateButtonState() {
        switch (mPlayerState) {
            case MediaPlayerState.STARTED:
                mPlayBtn.setBackgroundResource(R.drawable.pause_btn_state_list);
                break;
            case MediaPlayerState.PAUSED:
                mPlayBtn.setBackgroundResource(R.drawable.play_btn_state_list);
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.equalizer:
                Intent intent = new Intent();
                ComponentName cm = new ComponentName("com.android.settings", "com.android.settings.SoundSettings");
                intent.setComponent(cm);
                intent.setAction("android.intent.action.VIEW");
                startActivityForResult(intent, 0);
                return true;

            case R.id.sleep:
                final Intent sIntent = new Intent(MediaPlayBackService.ACTION_SLEEP_MODE);
                final Bundle bundle = new Bundle();

                if (mService.isInSleepMode()) {
                    bundle.putInt("sleep_time", 0);
                    sIntent.putExtras(bundle);
                    sendBroadcast(sIntent);
                    Toast.makeText(NowPlayingActivity.this, getString(R.string.cancel_sleep_mode), Toast.LENGTH_SHORT).show();
                } else if (mService.getPlayerCurrentState() == MediaPlayerState.STARTED) {
                    View dialogView = getLayoutInflater().inflate(R.layout.create_sleep_time_dialog, null);
                    final SeekBar seekBar = (SeekBar) dialogView.findViewById(R.id.sleep_dialog_seekbar);
                    final TextView textView = (TextView) dialogView.findViewById(R.id.sleep_time_dialog_textView);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                if (progress <= 30) {
                                    sleepTime = progress;
                                    textView.setText(progress + getString(R.string.sleep_after));
                                } else if (progress > 30) {
                                    sleepTime = (progress - 30) * 3 + 30;
                                    textView.setText(sleepTime + getString(R.string.sleep_after));
                                }
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(dialogView).setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (seekBar.getProgress() != 0 && mService.getPlayerCurrentState() == MediaPlayerState.STARTED) {
                                //send broadcast
                                bundle.putInt("sleep_time", sleepTime);
                                sIntent.putExtras(bundle);
                                sendBroadcast(sIntent);
                                Toast.makeText(NowPlayingActivity.this, sleepTime + getString(R.string.sleep_after), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                } else {
                    Toast.makeText(NowPlayingActivity.this, getString(R.string.open_when_playing), Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return false;
    }

    public void toolBarClick(View button) {
        switch (button.getId()) {
            case R.id.tool_bar_order_mode:
                sendBroadcast(new Intent(MediaPlayBackService.ACTION_MODE));
                if (mService.getOrderMode()) {
                    button.setBackgroundResource(R.drawable.order_play_state_list);
                } else {
                    button.setBackgroundResource(R.drawable.shuffle_state_list);
                }
                break;

            case R.id.tool_bar_repeat_mode:
                sendBroadcast(new Intent(MediaPlayBackService.ACTION_REPEAT));
                if (mService.isLooping()) {
                    button.setBackgroundResource(R.drawable.repeat_all_state_list);
                } else {
                    button.setBackgroundResource(R.drawable.repeat_single_state_list);
                }
                break;

            case R.id.tool_bar_equalizer:
                Intent intent = new Intent();
                ComponentName cm = new ComponentName("com.android.settings", "com.android.settings.SoundSettings");
                intent.setComponent(cm);
                intent.setAction("android.intent.action.VIEW");
                startActivityForResult(intent, 0);
                break;

            case R.id.tool_bar_more:
                PopupMenu popupMenu = new PopupMenu(this, button);
                popupMenu.getMenuInflater().inflate(R.menu.more, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
                break;
        }
    }

    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.now_playing_back:
                    NowPlayingActivity.this.finish();
                    NowPlayingActivity.this.overridePendingTransition(R.anim.activity_out_down, R.anim.activity_in_down);
                    break;
                case R.id.now_playing_like:
                    DataBaseOperator dbo = new DataBaseOperator(NowPlayingActivity.this);
                    ContentValues cv = new ContentValues();
                    ListItem<String, String> li = NowPlayingActivity.this.getBindService().getHelper().getCurrentTrack();
                    cv.put("_ID", li.get("song_id"));
                    cv.put("TITLE", li.get("title"));
                    cv.put("ARTIST", li.get("artist"));
                    cv.put("ALBUM", li.get("album"));
                    cv.put("DATA", li.get("data"));
                    cv.put("ALBUM_ID", li.get("album_id"));
                    cv.put("DURATION", li.get("duration"));
                    boolean flag = dbo.insert("MYFAVORITE", cv);
                    Toast.makeText(NowPlayingActivity.this, flag ? getString(R.string.add_fav_successful) : getString(R.string.add_fav_not_successful), Toast.LENGTH_SHORT).show();
                    dbo.closeDatabase();
                    dbo = null;
                    break;
                case R.id.now_playing_play:
                    sendBroadcast(new Intent(MediaPlayBackService.ACTION_PLAY));
                    break;
                case R.id.now_playing_pre:
                    sendBroadcast(new Intent(MediaPlayBackService.ACTION_PRE));
                    break;
                case R.id.now_playing_next:
                    sendBroadcast(new Intent(MediaPlayBackService.ACTION_NEXT));
                    break;
            }
        }
    }

    private class MyFragmentAdapter extends FragmentStatePagerAdapter {

        public MyFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }

    class MyThread implements Runnable {
        int i = 0;

        @Override
        public void run() {
            while (mPlayerState == MediaPlayerState.STARTED && mActivityIsFront) {
                if (i == 0) {
                    i = mService.getCurrentTrackDuration();
                }
                try {
                    Thread.sleep(300);
                    System.out.println(Thread.currentThread().toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                if (mPlayerState != MediaPlayerState.STARTED)
                    break;
                Message msg = Message.obtain();
                msg.arg1 = mService.getMilliSecondProgress();
                msg.arg2 = i;
                myHandler.sendMessage(msg);
            }
        }
    }
}
