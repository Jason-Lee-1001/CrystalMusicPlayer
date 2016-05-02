package com.studio.jason.application.ui;

import android.content.Context;
import android.preference.Preference;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import com.studio.jason.application.R;

/**
 * Author: Jason
 * Date: 2015/2/10.
 */
public class CustomSwitchPreference extends Preference {

    public CustomSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwitchPreference(Context context) {
        super(context);
    }

    @Override

    protected void onBindView(View view) {
        super.onBindView(view);
        SwitchCompat switchCompat = (SwitchCompat)view.findViewById(R.id.setting_fragment_switch);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)  {

            }
        });
    }

    @Override
    protected void onClick() {
        super.onClick();
        //Preference的点击事件
    }
}
