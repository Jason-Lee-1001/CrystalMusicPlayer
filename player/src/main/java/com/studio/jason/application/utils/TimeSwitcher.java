package com.studio.jason.application.utils;

/**
 * Created by Jason on 2014/12/27.
 */
public class TimeSwitcher {

    public static String getNormalTimeFormat(int milliSeconds) {
        if (milliSeconds < 1000) {
            return "00:00";
        }
        int m = milliSeconds / 60000;
        int s = (milliSeconds % 60000) / 1000;
        if (m < 10) {
            if (s < 10) {
                return "0" + m + ":" + "0" + s;
            }else{
                return "0" + m + ":" + s;
            }
        }else{
            if (s < 10) {
                return m + ":" + "0" + s;
            }else{
                return m + ":" + s;
            }
        }
    }
}
