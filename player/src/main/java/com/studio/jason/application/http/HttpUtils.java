package com.studio.jason.application.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.HttpStatus;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.params.CookiePolicy;
import cz.msebera.android.httpclient.client.params.HttpClientParams;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

/**
 * Author: Jason
 * Date: 2015/2/12.
 */
public class HttpUtils {

    public static
    @Nullable
    String getLyricDownloadUrl(String songName) {
        String downloadUrl = null;
        if ("".equals(songName) || songName == null) {
            return null;
        }
        HttpClient httpClient = new DefaultHttpClient();
        HttpClientParams.setCookiePolicy(httpClient.getParams(), CookiePolicy.BROWSER_COMPATIBILITY); // ResponseProcessCookies﹕ Invalid cookie header:
        try {
            songName = java.net.URLEncoder.encode(songName.trim(), "utf-8");
            String url = "http://mp3.baidu.com/dev/api/?tn=getinfo&ct=0&word=" + songName + "&ie=utf-8&format=json";
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String resultJson = EntityUtils.toString(response.getEntity());
                if (resultJson != null && resultJson.length() > 4) {
                    JSONArray rootArray = new JSONArray(resultJson);
                    JSONObject jsonObject = rootArray.getJSONObject(0);
                    String songId = jsonObject.getString("song_id");
                    if (songId != null) {
                        songId = java.net.URLEncoder.encode("<" + songId + ">", "utf-8");
                        String url1 = "http://ting.baidu.com/data/music/links?songIds=" + songId;
                        HttpGet httpGet1 = new HttpGet(url1);
                        HttpResponse response1 = httpClient.execute(httpGet1);
                        String resultJson1 = null;
                        if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            resultJson1 = EntityUtils.toString(response1.getEntity());
                        }
                        if (resultJson1 != null && resultJson.length() > 4) {
                            JSONObject jsonObject1 = new JSONObject(resultJson1);
                            JSONObject jsonDataObject1 = jsonObject1.getJSONObject("data");
                            JSONArray jsonListArray1 = jsonDataObject1.getJSONArray("songList");
                            JSONObject songlistObject1 = jsonListArray1.getJSONObject(0);
                            String lrcLink = songlistObject1.getString("lrcLink");
                            if (lrcLink != null) {
                                System.out.println("000000"+lrcLink);
                                downloadUrl = "http://ting.baidu.com" + lrcLink;
                                System.out.println("000000"+downloadUrl);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            downloadUrl = null;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return downloadUrl;
    }

    public static boolean downLyric(@NonNull String url, @NonNull String path) {
        boolean flag = false;
        System.out.println("00000"+url);
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        InputStream is;
        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if(response.getEntity().getContentLength()<50000) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    is = response.getEntity().getContent();
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    while ((length = is.read(buffer)) != -1) {
                        bos.write(buffer, 0, length);
                    }
                    FileOutputStream fos = new FileOutputStream(path);
                    fos.write(bos.toByteArray());
                    flag = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return flag;
    }

    public static Bitmap getBitmapFromUrl(String url) {
        if (null == url) {
            return null;
        }
        Bitmap bitmap;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        InputStream inputStream;
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            long file_length = httpResponse.getEntity().getContentLength();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                inputStream = httpResponse.getEntity().getContent();
                bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
