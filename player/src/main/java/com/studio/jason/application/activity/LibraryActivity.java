package com.studio.jason.application.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
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
import com.studio.jason.application.customized_adapter.SongListBaseAdapter;
import com.studio.jason.application.customized_view.ListItem;
import com.studio.jason.application.fragment.library.AlbumListFragment;
import com.studio.jason.application.fragment.library.ArtistListFragment;
import com.studio.jason.application.fragment.library.SongListFragment;
import com.studio.jason.application.graphic_tool.ArtWorkHelper;
import com.studio.jason.application.local_database.DataBaseOperator;
import com.studio.jason.application.service.MediaPlayBackService;
import com.studio.jason.application.static_data.MediaPlayerState;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LibraryActivity extends FragmentActivity implements PopupMenu.OnMenuItemClickListener {

    //UI components statement
    public View mToolBar;
    public View mBottomBar;
    public int indexCharHeight;
    private ViewPager mPager;
    private RadioButton mSongRadioButton, mArtistRadioButton, mAlbumRadioButton;
    private Button mPlayButton;
    private TextView mSongText;
    private TextView mArtistText;
    private ImageView mAlbumView;
    //---------------------------------------------------------------------
    //flag statement
    private boolean serviceIsBind;
    private int mPlayerState;
    //---------------------------------------------------------------------
    //system components statement
    private MyApplication mApplication;
    private ActionMode mMode;
    private MediaPlayBackService mService;
    private List<Fragment> mListFragment;
    private SongListFragment mFragment1;
    private ArtistListFragment mFragment2;
    private AlbumListFragment mFragment3;
    private MyPagerAdapter mAdapter;
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
                    updateButtonState();
                }

                @Override
                public void onUpdateTrackInfo(ListItem<String, String> currentTrack) {
                    updateUI(currentTrack);
                }
            });
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
    protected void onStart() {
        Intent intent = new Intent(this, MediaPlayBackService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_activity_layout);
        mApplication = (MyApplication) getApplication();
        initViewComponents();
    }

    private void initViewComponents() {
        //initialize the components
        View actionBarView = getLayoutInflater().inflate(R.layout.actionbar_up_header, null, false);
        ActionBar actionBar = getActionBar();
//        assert actionBar != null;
        actionBar.setCustomView(actionBarView);
        actionBar.setDisplayShowCustomEnabled(true);
        ((TextView) actionBarView.findViewById(R.id.actionbar_title)).setText(getString(R.string.library));
        actionBarView.findViewById(R.id.actionbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSongRadioButton = (RadioButton) this.findViewById(R.id.library_song_radio_btn);
        mArtistRadioButton = (RadioButton) this.findViewById(R.id.library_artist_radio_btn);
        mAlbumRadioButton = (RadioButton) this.findViewById(R.id.library_album_radio_btn);
        mPager = (ViewPager) this.findViewById(R.id.library_viewpager);
        mPager.setOffscreenPageLimit(2);
        mBottomBar = findViewById(R.id.control_bar);
        mToolBar = findViewById(R.id.library_tool_bar);
        mPlayButton = (Button) mBottomBar.findViewById(R.id.control_bar_play_btn);
        mSongText = (TextView) mBottomBar.findViewById(R.id.control_bar_song);
        mArtistText = (TextView) mBottomBar.findViewById(R.id.control_bar_artist);
        mAlbumView = (ImageView) mBottomBar.findViewById(R.id.control_bar_album_image);
        //---------------------------------------------------------------------
        //deal with viewpager
        mFragment1 = new SongListFragment();
        mFragment2 = new ArtistListFragment();
        mFragment3 = new AlbumListFragment();
        mListFragment = new ArrayList<>();
        mListFragment.add(mFragment1);
        mListFragment.add(mFragment2);
        mListFragment.add(mFragment3);
        mAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        //---------------------------------------------------------------------
        //set listener
        mPlayButton.setOnClickListener(new ButtonClickListener());
        mSongRadioButton.setOnClickListener(new ButtonClickListener());
        mArtistRadioButton.setOnClickListener(new ButtonClickListener());
        mAlbumRadioButton.setOnClickListener(new ButtonClickListener());
        mBottomBar.setOnClickListener(new ButtonClickListener());
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mMode != null) {
                    mMode.finish();
                }
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        LibraryActivity.this.mSongRadioButton.setChecked(true);
                        break;
                    case 1:
                        LibraryActivity.this.mArtistRadioButton.setChecked(true);
                        break;
                    case 2:
                        LibraryActivity.this.mAlbumRadioButton.setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
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
                    Toast.makeText(LibraryActivity.this, getString(R.string.cancel_sleep_mode), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(LibraryActivity.this, sleepTime + getString(R.string.sleep_after), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                } else {
                    Toast.makeText(LibraryActivity.this, getString(R.string.open_when_playing), Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        sendBroadcast(new Intent(MediaPlayBackService.ACTION_UPDATE_UI));
        indexCharHeight = mPager.getHeight() / 27;
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onPause() {
        if (this.mMode != null) {
            mMode.finish();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (serviceIsBind) {
            this.unbindService(mConnection);
            serviceIsBind = false;
        }
        super.onStop();
    }

    private void updateUI(ListItem<String, String> currentTrack) {
        updateButtonState();
        if (currentTrack != null) {
            HashMap<String, String> map = mService.getHelper().getCurrentTrack();
            mSongText.setText(map.get("title"));
            String artist;
            if ("<unknown>".equals(map.get("artist"))) {
                artist = getResources().getString(R.string.unknown_artist);
            } else {
                artist = map.get("artist");
            }
            mArtistText.setText(artist);

            Bitmap album;
            try {
                long id = Long.parseLong(map.get("album_id"));
                album = ArtWorkHelper.getAlbumArtWorkFromFile(this, id, -1, 100, 100, false);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                album = ArtWorkHelper.getDefaultArtwork(this, true);
            }
            mAlbumView.setImageBitmap(album);
        }
    }

    private void updateButtonState() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.library_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(LibraryActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_more:
                PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_more));
                popupMenu.getMenuInflater().inflate(R.menu.more, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setActionMode(ActionMode mode) {
        this.mMode = mode;
    }

    public void toolbar_item_click(View v) {
        SparseBooleanArray sba = mFragment1.getListView().getCheckedItemPositions();
        SongListBaseAdapter myAdpter = mFragment1.getAdapter();
        List<ListItem<String, String>> strs = myAdpter.getList();
        List<ListItem<String, String>> tempList = new ArrayList<>();
        for (int i = 0; i < sba.size(); ++i) {
            if (sba.valueAt(i)) {
                mFragment1.getListView().setItemChecked(sba.keyAt(i), false);
                tempList.add(strs.get(sba.keyAt(i)));
            }
        }
        switch (v.getId()) {
            case R.id.tool_bar_play:
                mApplication.setCurrentPlayList(tempList).setCurrentAudio(tempList.get(0)).setCurrentIndex(0);
                mService.playNewList();
                break;
            case R.id.tool_bar_add:
                mService.getHelper().getCurrentPlayList().addAll(tempList);
                mApplication.setCurrentPlayList(mService.getHelper().getCurrentPlayList());
                mService.getHelper().updateIndex();
                break;
            case R.id.tool_bar_like:
                DataBaseOperator dbo = new DataBaseOperator(LibraryActivity.this);
                for (int i = 0; i < tempList.size(); i++) {
                    ContentValues cv = new ContentValues();
                    cv.put("_ID", tempList.get(i).get("song_id"));
                    cv.put("TITLE", tempList.get(i).get("title"));
                    cv.put("ARTIST", tempList.get(i).get("artist"));
                    cv.put("ALBUM", tempList.get(i).get("album"));
                    cv.put("DATA", tempList.get(i).get("data"));
                    cv.put("ALBUM_ID", tempList.get(i).get("album_id"));
                    cv.put("DURATION", tempList.get(i).get("duration"));
                    dbo.insert("MYFAVORITE", cv);
                }
                dbo.closeDatabase();
                dbo = null;
                Toast.makeText(LibraryActivity.this, tempList.size() + getString(R.string.add_songs_to_fav_successfully), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tool_bar_delete:
                showDeletePopupDialog(tempList);
                break;
            case R.id.action_mode_select_all:
                int j = mFragment1.getListView().getCount();
                for (int i = 0; i < j; ++i)
                    mFragment1.getListView().setItemChecked(i, true);
                break;

            default:
                break;
        }
    }

    private void showDeletePopupDialog(final List<ListItem<String, String>> list) {
        final ContentResolver resolver = getContentResolver();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.action_delete)).setMessage(getString(R.string.dialog_delete_content))
                .setPositiveButton(getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < list.size(); i++) {
                            String ID = list.get(i).get("song_id");
                            resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + "=?", new String[]{ID});
                            File toBeDelFile = new File(list.get(i).get("data"));
                            boolean flag = toBeDelFile.delete();
                        }
                        Toast.makeText(LibraryActivity.this, getString(R.string.delete_successfully), Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(getString(R.string.dialog_cancel), null).show();
//        SnackBar snackBar = new SnackBar(this, , getString(R.string.dialog_confirm), new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for (int i = 0; i < list.size(); i++) {
//                    String ID = list.get(i).get("song_id");
//                    resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + "=?", new String[]{ID});
//                    File toBeDelFile = new File(list.get(i).get("data"));
//                    boolean flag = toBeDelFile.delete();
//                }
//                Toast.makeText(LibraryActivity.this, getString(R.string.delete_successfully), Toast.LENGTH_SHORT).show();
//            }
//        });
//        snackBar.setIndeterminate(true);
//        snackBar.show();
    }

    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.control_bar_play_btn:
                    sendBroadcast(new Intent(MediaPlayBackService.ACTION_PLAY));
                    break;
                case R.id.library_song_radio_btn:
                    LibraryActivity.this.mPager.setCurrentItem(0);
                    break;
                case R.id.library_artist_radio_btn:
                    LibraryActivity.this.mPager.setCurrentItem(1);
                    break;
                case R.id.library_album_radio_btn:
                    LibraryActivity.this.mPager.setCurrentItem(2);
                    break;
                case R.id.control_bar:
                    Intent intent = new Intent(LibraryActivity.this, NowPlayingActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_in_up, R.anim.activity_out_up);
                    break;
            }
        }
    }

    class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mListFragment.get(position);
        }

        @Override
        public int getCount() {
            return mListFragment.size();
        }
    }
}
