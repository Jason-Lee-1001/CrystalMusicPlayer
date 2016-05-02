package com.studio.jason.application.system_frame;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;

import com.studio.jason.application.R;
import com.studio.jason.application.activity.DetailActivity;

/**
 * Created by Jason on 2015/02/9.
 */
public class DetailListFragmentMultiChoiceListener implements AbsListView.MultiChoiceModeListener {

    private DetailActivity mActivity;

    public DetailListFragmentMultiChoiceListener(DetailActivity activity) {
        this.mActivity = activity;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.setCustomView(mActivity.getLayoutInflater().inflate(R.layout.library_action_view, null, false));
        if(mActivity.flag != 0) {
            switch (mActivity.flag){
                case 1:
                case 2:
                    mActivity.mDetailToolBar.setAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.toolbar_up_appear));
                    mActivity.mDetailToolBar.setVisibility(View.VISIBLE);
                    break;

                case 3:
                    mActivity.mFavToolBar.setAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.toolbar_up_appear));
                    mActivity.mFavToolBar.setVisibility(View.VISIBLE);
                    break;

                default:
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if(mActivity.mDetailToolBar.getVisibility() == View.VISIBLE){
            mActivity.mDetailToolBar.setAnimation(AnimationUtils.loadAnimation(mActivity,R.anim.toolbar_down_disappear));
            mActivity.mDetailToolBar.setVisibility(View.GONE);
        }
        if(mActivity.flag == 3 && mActivity.mFavToolBar.getVisibility() == View.VISIBLE){
            mActivity.mFavToolBar.setAnimation(AnimationUtils.loadAnimation(mActivity,R.anim.toolbar_down_disappear));
            mActivity.mFavToolBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {}
}
