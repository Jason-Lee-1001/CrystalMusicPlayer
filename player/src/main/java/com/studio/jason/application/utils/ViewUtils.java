package com.studio.jason.application.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Author: Jason
 * Date: 2015/2/7.
 */
public class ViewUtils {

    public static void adjustViewSize(View view){
        ViewGroup.LayoutParams params = view.getLayoutParams();
        int w = view.getWidth();
        int h = view.getHeight();
        if (h > w) {
            params.width = w;
            params.height = w;
            view.setLayoutParams(params);
        }
        if (w >= h) {
            params.width = h;
            params.height = h;
            view.setLayoutParams(params);
        }
    }
}
