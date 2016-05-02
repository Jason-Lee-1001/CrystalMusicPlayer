package com.studio.jason.application.customized_view;

/**
 * Created by Jason on 2014/12/31.
 */

import java.io.Serializable;
import java.util.HashMap;


public class ListItem<String, Object> extends HashMap<String, Object> implements Serializable, Comparable<ListItem> {

    @Override
    public int compareTo(ListItem another) {
        if (null == another) return 1;
        else {
            return this.get("index").toString().compareTo(another.get("index").toString());
        }
    }
}
