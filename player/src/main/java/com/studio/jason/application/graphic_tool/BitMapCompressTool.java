package com.studio.jason.application.graphic_tool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.io.FileDescriptor;

/**
 * Created by Jason on 2014/12/28.
 */
public class BitMapCompressTool {

    public static Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, Rect rect, int requestWidth, int requestHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd,rect,options);

        // Calculate inSampleSize
        options.inSampleSize = getBitmapSampleSize(options, requestWidth, requestHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, rect, options);
    }

    public static Bitmap decodeSampledBitmapFromPath(String path, int requestWidth, int requestHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = getBitmapSampleSize(options, requestWidth, requestHeight);
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int getBitmapSampleSize(BitmapFactory.Options options, int requestWidth, int requestHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > requestHeight || width > requestWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > requestHeight
                    && (halfWidth / inSampleSize) > requestWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
