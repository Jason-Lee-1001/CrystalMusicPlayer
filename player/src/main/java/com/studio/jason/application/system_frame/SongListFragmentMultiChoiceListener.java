package com.studio.jason.application.system_frame;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;

import com.studio.jason.application.R;
import com.studio.jason.application.activity.LibraryActivity;
import com.studio.jason.application.fragment.library.SongListFragment;

/**
 * Created by Jason on 2014/12/14.
 */
public class SongListFragmentMultiChoiceListener implements AbsListView.MultiChoiceModeListener {

    private SongListFragment mFragment;
    private ActionMode mMode;
    private LibraryActivity mLibraryActivity;

    public SongListFragmentMultiChoiceListener(SongListFragment fragment) {
        this.mFragment = fragment;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        this.mMode = mode;
        mode.setCustomView(mFragment.getActivity().getLayoutInflater().inflate(R.layout.library_action_view,null,false));
        mLibraryActivity = (LibraryActivity)mFragment.getActivity();
        mLibraryActivity.mBottomBar.setAnimation(AnimationUtils.loadAnimation(mLibraryActivity,R.anim.toolbar_down_disappear));
        mLibraryActivity.mBottomBar.setVisibility(View.GONE);
        mLibraryActivity.mToolBar.setAnimation(AnimationUtils.loadAnimation(mLibraryActivity,R.anim.toolbar_up_appear));
        mLibraryActivity.mToolBar.setVisibility(View.VISIBLE);
        ((LibraryActivity)mFragment.getActivity()).setActionMode(mMode);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        /*switch (item.getItemId()) {
            case R.id.setting:
                Toast.makeText(mFragment.getActivity(), "do your add function here", Toast.LENGTH_SHORT).show();
                mFragment.getAdapter().notifyDataSetChanged();
                mode.finish();
                break;

            default:
                break;
        }*/
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mLibraryActivity.mToolBar.setAnimation(AnimationUtils.loadAnimation(mLibraryActivity,R.anim.toolbar_down_disappear));
        mLibraryActivity.mToolBar.setVisibility(View.GONE);
        mLibraryActivity.mBottomBar.setAnimation(AnimationUtils.loadAnimation(mLibraryActivity,R.anim.toolbar_up_appear));
        mLibraryActivity.mBottomBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
    }
}
