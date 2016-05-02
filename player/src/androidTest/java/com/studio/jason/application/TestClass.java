package com.studio.jason.application;

import android.content.ContentValues;
import android.test.InstrumentationTestCase;

import com.studio.jason.application.local_database.DataBaseOperator;

/**
 * Created by Jason on 2015/1/7.
 */
public class TestClass extends InstrumentationTestCase {

    public void testDatabase(){

        DataBaseOperator dbo = new DataBaseOperator(this.getInstrumentation().getTargetContext());
        long start = System.currentTimeMillis();
        for(int i = 0; i<3000; i++) {
            ContentValues cv = new ContentValues();
            cv.put("_ID", "song_id"+i);
            cv.put("TITLE", "title"+i);
            cv.put("ARTIST", "artist"+i);
            cv.put("ALBUM","album"+i);
            cv.put("DATA", "data"+i);
            cv.put("ALBUM_ID","album_id"+i);
            cv.put("DURATION","duration"+i);
            boolean flag = dbo.insert("MYFAVORITE", cv);
        }
        long end = System.currentTimeMillis();
        System.out.println("use time: "+(end-start));
        dbo.closeDatabase();
        dbo = null;
    }

}
