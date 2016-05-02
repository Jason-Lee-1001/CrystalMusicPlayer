package com.studio.jason.application.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.studio.jason.application.R;
import com.studio.jason.application.utils.SystemBarTintManager;

/**
 * Author: Jason
 * Date: 2015/2/7.
 */
public class InfoActivity extends ActionBarActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar_info_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        setSystemTintColor();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setSystemTintColor(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.water_blue));
        }
    }

}
