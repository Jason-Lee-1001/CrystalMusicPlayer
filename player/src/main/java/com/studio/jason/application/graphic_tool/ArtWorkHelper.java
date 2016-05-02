package com.studio.jason.application.graphic_tool;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.studio.jason.application.R;

import java.io.FileDescriptor;

/**
 * Created by Jason on 2014/12/28.
 */
public class ArtWorkHelper {

    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

    public static Bitmap getAlbumArtWorkFromFile(Context context, long albumId, long songId, int reqWidth, int reqHeight, boolean isBg) {
        FileDescriptor fd = null;
        try {
            if (albumId < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songId + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumId);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            return getDefaultArtwork(context, isBg);
        }
        return BitMapCompressTool.decodeSampledBitmapFromFileDescriptor(fd, null, reqWidth, reqHeight);
    }

    public static Bitmap getAlbumArtWrokByUrl(Context context, String path, int reqWidth, int reqHeight) {
        FileDescriptor fd = null;
        try {
            Uri uri = Uri.parse(path);
            ParcelFileDescriptor pfd = null;

            pfd = context.getContentResolver().openFileDescriptor(uri, "r");

            if (pfd != null) {
                fd = pfd.getFileDescriptor();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultArtwork(context, true);
        }
        return BitMapCompressTool.decodeSampledBitmapFromFileDescriptor(fd, null, reqWidth, reqHeight);
    }


    public static Bitmap getDefaultArtwork(Context context, boolean isBg) {
        if (isBg) {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.now_playing_default_color_bg_blue);
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_cover);
    }

}
