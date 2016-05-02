package com.studio.jason.application.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;

import com.studio.jason.application.graphic_tool.BitMapCompressTool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private LruCache<String, Bitmap> mMemoryCache;
    private ExecutorService mImageThreadPool = null;

    public ImageLoader(Context context) {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory / 16;
        mMemoryCache = new LruCache<String, Bitmap>(mCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    public ExecutorService getThreadPool() {
        if (mImageThreadPool == null) {
            synchronized (ExecutorService.class) {
                if (mImageThreadPool == null) {
                    mImageThreadPool = Executors.newFixedThreadPool(2);
                }
            }
        }
        return mImageThreadPool;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public Bitmap downloadImage(final String path, final onImageLoaderListener listener) {
        final String subUrl = path.substring(path.lastIndexOf("/") + 1);
        Bitmap bitmap = showCacheBitmap(subUrl);
        if (bitmap != null) {
            return bitmap;
        } else {

            final Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    listener.onImageLoader((Bitmap) msg.obj, path);
                }
            };
            getThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitMapCompressTool.decodeSampledBitmapFromPath(path, 180, 180);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Message msg = handler.obtainMessage();
                    msg.obj = bitmap;
                    handler.sendMessage(msg);
                    addBitmapToMemoryCache(subUrl, bitmap);
                }
            });
        }
        return null;
    }

    public Bitmap showCacheBitmap(String url) {
        if (getBitmapFromMemCache(url) != null) {
            return getBitmapFromMemCache(url);
        }
        return null;
    }

    public synchronized void cancelTask() {
        if (mImageThreadPool != null) {
            mImageThreadPool.shutdownNow();
            mImageThreadPool = null;
        }
    }

    public interface onImageLoaderListener {
        void onImageLoader(Bitmap bitmap, String url);
    }

}