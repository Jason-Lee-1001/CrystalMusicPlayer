package com.studio.jason.application.fragment.now_playing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;

import com.studio.jason.application.R;
import com.studio.jason.application.activity.NowPlayingActivity;

/**
 * Created by Jason on 2014/12/20.
 */
public class NowPlayingMainFragment extends Fragment{

    private boolean mIsToolBarVisible = false;
    private ImageSwitcher mImageView;
    private Button mOrderMode, mRepeatMode;
    private View mToolBar;
    private OnFragmentIsVisible mOnFragmentIsVisible;

    public NowPlayingMainFragment() {

    }

    public void setOnFragmentIsVisibleListener(OnFragmentIsVisible listener){
        this.mOnFragmentIsVisible = listener;
    }

    public interface OnFragmentIsVisible{
        public void onViewVisible();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mOnFragmentIsVisible != null){
            mOnFragmentIsVisible.onViewVisible();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.now_playing_main_fragment, container, false);
        mImageView = (ImageSwitcher)v.findViewById(R.id.now_playing_main_imageView);
        mOrderMode = (Button)v.findViewById(R.id.tool_bar_order_mode);
        mRepeatMode = (Button)v.findViewById(R.id.tool_bar_repeat_mode);
        mToolBar = v.findViewById(R.id.now_playing_tool_bar);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsToolBarVisible = !mIsToolBarVisible;
                if(mIsToolBarVisible){
                    mToolBar.setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.toolbar_up_appear));
                    mToolBar.setVisibility(View.VISIBLE);
                }else{
                    mToolBar.setAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.toolbar_down_disappear));
                    mToolBar.setVisibility(View.GONE);
                }
                if(((NowPlayingActivity)getActivity()).getBindService().isLooping()) {
                    mRepeatMode.setBackgroundResource(R.drawable.repeat_single_state_list);
                } else {
                    mRepeatMode.setBackgroundResource(R.drawable.repeat_all_state_list);
                }
                if(((NowPlayingActivity)getActivity()).getBindService().getOrderMode()) {
                    mOrderMode.setBackgroundResource(R.drawable.shuffle_state_list);
                } else {
                    mOrderMode.setBackgroundResource(R.drawable.order_play_state_list);
                }
            }
        });
        return v;
    }
}
