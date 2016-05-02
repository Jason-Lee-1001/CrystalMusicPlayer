package com.studio.jason.application.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Jason on 2015/1/29.
 */
public class FileUtils {

    private static String mSDCachePath = null;
    private static String mCachePath = null;

    private final static String CACHE_FOLDER_NAME = "/Bitmap";

    public FileUtils(Context context){
        if((Environment.MEDIA_MOUNTED).equals(Environment.getExternalStorageState())){
            mSDCachePath = context.getExternalCacheDir().getAbsolutePath();
        }
        mCachePath = context.getCacheDir().getAbsolutePath();
    }

    public String getStorageDirectiory(){
        return (Environment.MEDIA_MOUNTED).equals(Environment.getExternalStorageState()) ?
                mSDCachePath + CACHE_FOLDER_NAME : mCachePath + CACHE_FOLDER_NAME;
    }

    public void saveBitmap(String fileName, Bitmap bitmap) throws IOException{
        if(null == bitmap){
            return;
        }
        String path = getStorageDirectiory();
        File folder = new File(path);
        if(!folder.exists()){
            folder.mkdirs();
        }
        File file = new File(path + File.separator + fileName);
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
        fos.flush();
        fos.close();
    }

    public Bitmap getBitmap(String fileName){
        return BitmapFactory.decodeFile(getStorageDirectiory() + File.separator + fileName);
    }

    public boolean isFileExist(String fileName){
        return new File(getStorageDirectiory() + File.separator + fileName).exists();
    }

    public long getFileSize(String fileName){
        return new File(getStorageDirectiory() + File.separator + fileName).length();
    }

    public void deleteFiles(){
        File dirFile = new File(getStorageDirectiory());
        if(!dirFile.exists()){
            return;
        }
        if(dirFile.isDirectory()){
            String[] strings = dirFile.list();
            for(String str : strings){
                new File(dirFile, str).delete();
            }
        }
        dirFile.delete();
    }

}
