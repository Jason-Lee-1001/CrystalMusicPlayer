package com.studio.jason.application.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.studio.jason.application.MyApplication;
import com.studio.jason.application.R;
import com.studio.jason.application.customized_view.ListItem;
import com.studio.jason.application.fragment.home.LocalMusicFragment;
import com.studio.jason.application.fragment.home.OnlineMusicFragment;
import com.studio.jason.application.graphic_tool.ArtWorkHelper;
import com.studio.jason.application.service.MediaPlayBackService;
import com.studio.jason.application.static_data.MediaPlayerState;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class HomeActivity extends FragmentActivity implements PopupMenu.OnMenuItemClickListener {

    //UI components statement
    private ViewPager mViewPager;
    private View mBottomBar;
    private Button mPlayButton;
    private TextView mSongText, mArtistText;
    private ImageView mAlbumView;
    private RadioButton mLocalRadioButton, mOnlineRadioButton;
    //---------------------------------------------------------------------
    //flag statement
    private boolean serviceIsBind;
    private int mPlayerState;
    private ListItem<String,String> mCurrentTrack;
    //---------------------------------------------------------------------
    //system components statement
    private MyApplication mApplication;
    private MediaPlayBackService mService;
    private List<Fragment> mFragmentList;
    private LocalMusicFragment mFragment1;
    private OnlineMusicFragment mFragment2;
    private MyFragmentAdapter mAdapter;
    private MediaPlayBackService.MediaServiceBinder mBinder;
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
            mService.setOnPlayStateChangedListener(new MediaPlayBackService.OnPlayStateChangedListener() {

                @Override
                public void onStateChanged(int state) {
                    mPlayerState = state;
                    switch (mPlayerState) {
                        case MediaPlayerState.STARTED:
                            mPlayButton.setBackgroundResource(R.drawable.control_bar_pause_state_list);
                            break;
                        case MediaPlayerState.PAUSED:
                            mPlayButton.setBackgroundResource(R.drawable.control_bar_play_state_list);
                            break;
                    }
                }

                @Override
                public void onUpdateTrackInfo(ListItem<String, String> currentTrack) {
                    mCurrentTrack = currentTrack;
                    updateUI(currentTrack);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceIsBind = false;
        }
    };

    @Override
    protected void onStart() {
        Intent intent = new Intent(this, MediaPlayBackService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_layout);
        initViewComponents();
    }

    private void initViewComponents(){
        View radioGroup = getLayoutInflater().inflate(R.layout.radio_group,null,false);
        ActionBar actionBar = getActionBar();
//        assert actionBar != null;
        actionBar.setCustomView(radioGroup);
        actionBar.setDisplayShowCustomEnabled(true);
        mLocalRadioButton = (RadioButton) radioGroup.findViewById(R.id.home_local_radioButton);
        mOnlineRadioButton = (RadioButton) radioGroup.findViewById(R.id.home_online_radioButton);
        mViewPager = (ViewPager) this.findViewById(R.id.home_viewpager);

        mBottomBar = findViewById(R.id.control_bar);
        mPlayButton = (Button)mBottomBar.findViewById(R.id.control_bar_play_btn);
        mSongText = (TextView)mBottomBar.findViewById(R.id.control_bar_song);
        mArtistText = (TextView)mBottomBar.findViewById(R.id.control_bar_artist);
        mAlbumView = (ImageView)mBottomBar.findViewById(R.id.control_bar_album_image);

        mPlayButton.setOnClickListener(new ButtonClickListener());
        mLocalRadioButton.setOnClickListener(new ButtonClickListener());
        mOnlineRadioButton.setOnClickListener(new ButtonClickListener());
        mBottomBar.setOnClickListener(new ButtonClickListener());

        mApplication = (MyApplication)getApplication();
        mFragment1 = new LocalMusicFragment();
        mFragment2 = new OnlineMusicFragment();
        mFragmentList = new ArrayList<>();
        mFragmentList.add(mFragment1);
        mFragmentList.add(mFragment2);
        mAdapter = new MyFragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        HomeActivity.this.mLocalRadioButton.setChecked(true);
                        break;
                    case 1:
                        HomeActivity.this.mOnlineRadioButton.setChecked(true);
                        break;
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private int sleepTime = 30;
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.equalizer:
                Intent intent = new Intent();
                ComponentName cm = new ComponentName("com.android.settings","com.android.settings.SoundSettings");
                intent.setComponent(cm);
                intent.setAction("android.intent.action.VIEW");
                startActivityForResult( intent , 0);
                return true;

            case R.id.sleep:
                final Intent sIntent = new Intent(MediaPlayBackService.ACTION_SLEEP_MODE);
                final Bundle bundle = new Bundle();

                if(mService.isInSleepMode()){
                    bundle.putInt("sleep_time", 0);
                    sIntent.putExtras(bundle);
                    sendBroadcast(sIntent);
                    Toast.makeText(HomeActivity.this, getString(R.string.cancel_sleep_mode), Toast.LENGTH_SHORT).show();
                }else if(mService.getPlayerCurrentState() == MediaPlayerState.STARTED){
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
                                }else if (progress > 30) {
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
                                Toast.makeText(HomeActivity.this, sleepTime+getString(R.string.sleep_after), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                }else{
                    Toast.makeText(HomeActivity.this, getString(R.string.open_when_playing), Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return false;
    }


    private class ButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.control_bar_play_btn:
                    sendBroadcast(new Intent(MediaPlayBackService.ACTION_PLAY));
                    break;

                case R.id.home_local_radioButton:
                    HomeActivity.this.mViewPager.setCurrentItem(0);
                    break;

                case R.id.home_online_radioButton:
                    HomeActivity.this.mViewPager.setCurrentItem(1);
                    break;

                case R.id.control_bar:
                    Intent intent = new Intent(HomeActivity.this, NowPlayingActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_in_up,R.anim.activity_out_up);
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        sendBroadcast(new Intent(MediaPlayBackService.ACTION_UPDATE_UI));
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onBackPressed() {
        if (mPlayerState == MediaPlayerState.STARTED) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
//            System.gc();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        if (serviceIsBind) {
            this.unbindService(mConnection);
            serviceIsBind = false;
        }
        LinkedHashMap<String,String> map = new LinkedHashMap<>(0,0.4f,false) ;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(mPlayerState != MediaPlayerState.STARTED){
            mService.stopSelf();
            SharedPreferences pref = getSharedPreferences("list_preference", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("currentTrack_ID",mCurrentTrack.get("song_id")).putString("currentTrack_TITLE",mCurrentTrack.get("title"))
                    .putString("currentTrack_ARTIST",mCurrentTrack.get("artist")).putString("currentTrack_ALBUM",mCurrentTrack.get("album"))
                    .putString("currentTrack_DATA",mCurrentTrack.get("data")).putString("currentTrack_ALBUM_ID",mCurrentTrack.get("album_id"))
                    .putString("currentTrack_DURATION",mCurrentTrack.get("duration"));
            if(mApplication.getCurrentPlayList().size()>0){
                JSONArray jsonPlayList = new JSONArray(mApplication.getCurrentPlayList());
                editor.putString("playlist",jsonPlayList.toString());
            }
            editor.apply();
        }
        super.onDestroy();
    }

    private void updateUI(ListItem<String,String> currentTrack) {
        switch (mPlayerState) {
            case MediaPlayerState.STARTED:
                mPlayButton.setBackgroundResource(R.drawable.control_bar_pause_state_list);
                break;
            case MediaPlayerState.PAUSED:
                mPlayButton.setBackgroundResource(R.drawable.control_bar_play_state_list);
                break;
        }
        if (currentTrack!= null) {
            mSongText.setText(currentTrack.get("title"));
            String artist;
            if ("<unknown>".equals(currentTrack.get("artist"))) {
                artist = getResources().getString(R.string.unknown_artist);
            } else {
                artist = currentTrack.get("artist");
            }
            mArtistText.setText(artist);

            Bitmap album;
            try {
                long id = Long.parseLong(currentTrack.get("album_id"));
                album = ArtWorkHelper.getAlbumArtWorkFromFile(this, id, -1, 100, 100, false);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                album = ArtWorkHelper.getDefaultArtwork(this, true);
            }
            mAlbumView.setImageBitmap(album);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_more:
                PopupMenu popupMenu = new PopupMenu(this,findViewById(R.id.action_more));
                popupMenu.getMenuInflater().inflate(R.menu.more, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyFragmentAdapter extends FragmentPagerAdapter {

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
}
