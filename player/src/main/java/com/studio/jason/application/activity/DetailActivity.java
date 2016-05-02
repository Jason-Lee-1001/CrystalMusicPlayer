package com.studio.jason.application.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.studio.jason.application.MyApplication;
import com.studio.jason.application.R;
import com.studio.jason.application.content_loader.DetailListLoader;
import com.studio.jason.application.customized_adapter.TrackDetailAdapter;
import com.studio.jason.application.customized_view.ListItem;
import com.studio.jason.application.local_database.DataBaseOperator;
import com.studio.jason.application.service.MediaPlayBackService;
import com.studio.jason.application.static_data.MediaPlayerState;
import com.studio.jason.application.system_frame.DetailListFragmentMultiChoiceListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends Activity implements PopupMenu.OnMenuItemClickListener{

    private ListView mListView;
    private TextView mActionBarTitle;
    private Button mActionBarBack;
    private TrackDetailAdapter mAdapter;
    private LoaderManager mManager;
    private MyApplication mApplication;
    private MediaPlayBackService.MediaServiceBinder mBinder;
    private MediaPlayBackService mService;
    public View mFavToolBar;
    public View mDetailToolBar;
    public int flag;
    private boolean serviceIsBind = false;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MediaPlayBackService.MediaServiceBinder) service;
            mService = mBinder.getService();
            serviceIsBind = true;
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
        setContentView(R.layout.detail_activity_layout);
        mManager = getLoaderManager();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        long mId = bundle.getLong("id");
        flag = bundle.getInt("flag");
        String title = bundle.getString("title");
        initViewComponents();
        if (null != title && null != getActionBar()) {
            mActionBarTitle.setText(title);
        }
        mManager.initLoader(111, null, new DetailListLoader(this, mAdapter, mId, flag));
    }

    private void initViewComponents(){
        View actionBarView = getLayoutInflater().inflate(R.layout.actionbar_up_header,null,false);
        ActionBar actionBar = getActionBar();
//        assert actionBar != null;
        actionBar.setCustomView(actionBarView);
        actionBar.setDisplayShowCustomEnabled(true);
        mApplication = (MyApplication) getApplication();
        mActionBarBack = (Button)actionBarView.findViewById(R.id.actionbar_back);
        mActionBarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mActionBarTitle = (TextView)actionBarView.findViewById(R.id.actionbar_title);
        mListView = (ListView) findViewById(R.id.detail_list);
        mDetailToolBar = this.findViewById(R.id.library_tool_bar);
        mFavToolBar = this.findViewById(R.id.fav_tool_bar);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new DetailListFragmentMultiChoiceListener(this));
        mAdapter = new TrackDetailAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mApplication.setCurrentPlayList(DetailActivity.this.mAdapter.getList())
                        .setCurrentAudio(DetailActivity.this.mAdapter.getList().get(position))
                        .setCurrentIndex(position);
                mService.playNewList();
            }
        });
    }

    public void toolbar_item_click(View v){
        SparseBooleanArray sba = mListView.getCheckedItemPositions();
        List<ListItem<String, String>> strs = mAdapter.getList();
        List<ListItem<String, String>> tempList = new ArrayList<>();
        for(int i = 0; i < sba.size(); i++){
            if(sba.valueAt(i)){
                mListView.setItemChecked(sba.keyAt(i), false);
                tempList.add(strs.get(sba.keyAt(i)));
            }
        }
        switch (v.getId()){
            case R.id.action_mode_select_all:
                for (int i = 0; i < mListView.getCount(); ++i) {
                    mListView.setItemChecked(i, true);
                }
                break;

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
                DataBaseOperator dbo = new DataBaseOperator(DetailActivity.this);
                for(int i = 0; i< tempList.size(); i++) {
                    ContentValues cv = new ContentValues();
                    cv.put("_ID", tempList.get(i).get("song_id"));
                    cv.put("TITLE", tempList.get(i).get("title"));
                    cv.put("ARTIST",tempList.get(i).get("artist"));
                    cv.put("ALBUM", tempList.get(i).get("album"));
                    cv.put("DATA", tempList.get(i).get("data"));
                    cv.put("ALBUM_ID", tempList.get(i).get("album_id"));
                    cv.put("DURATION", tempList.get(i).get("duration"));
                    dbo.insert("MYFAVORITE", cv);
                }
                dbo.closeDatabase();
                dbo = null;
                Toast.makeText(DetailActivity.this, tempList.size() + getString(R.string.add_songs_to_fav_successfully), Toast.LENGTH_SHORT).show();
                break;

            case R.id.tool_bar_delete:
                showDeletePopupDialog(tempList);
                break;

            case R.id.fav_tool_bar_play:
                mApplication.setCurrentPlayList(tempList).setCurrentAudio(tempList.get(0)).setCurrentIndex(0);
                mService.playNewList();
                break;

            case R.id.fav_tool_bar_add:
                mService.getHelper().getCurrentPlayList().addAll(tempList);
                mApplication.setCurrentPlayList(mService.getHelper().getCurrentPlayList());
                mService.getHelper().updateIndex();
                break;

            case R.id.fav_tool_bar_remove:
                DataBaseOperator dbOperator = new DataBaseOperator(DetailActivity.this);
                mAdapter.getList().removeAll(tempList);
                for(int i = 0; i< tempList.size(); i++) {
                    dbOperator.del("MYFAVORITE", new String[]{tempList.get(i).get("song_id")});
                }
                dbOperator.closeDatabase();
                dbOperator = null;
                break;

            default:
                break;
        }
    }



    private void showDeletePopupDialog(final List<ListItem<String,String>> list){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ContentResolver resolver = getContentResolver();
        builder.setMessage(getString(R.string.dialog_delete_content))
                .setPositiveButton(getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < list.size(); i++) {
                            String ID = list.get(i).get("song_id");
                            resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media._ID + "=?", new String[]{ID});
                            File toBeDelFile = new File(list.get(i).get("data"));
                            boolean flag = toBeDelFile.delete();
                        }
                        Toast.makeText(DetailActivity.this, getString(R.string.delete_successfully), Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        }).show();
    }

    @Override
    protected void onStop() {
        if (serviceIsBind) {
            this.unbindService(mConnection);
            serviceIsBind = false;
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.goto_play:
                Intent intent = new Intent(DetailActivity.this, NowPlayingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_in_up, R.anim.activity_out_up);
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
                    Toast.makeText(DetailActivity.this, getString(R.string.cancel_sleep_mode), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(DetailActivity.this, sleepTime+getString(R.string.sleep_after), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                }else{
                    Toast.makeText(DetailActivity.this, getString(R.string.open_when_playing), Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return false;
    }
}
