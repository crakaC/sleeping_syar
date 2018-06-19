package com.crakac.ofuton.util;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.TwitterAPIConfiguration;

public class Util {

    private Util() {
    }

    public static void closeQuietly(@Nullable Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Exception e) {
        }
    }

    static <T extends Serializable> T restoreFile(Context context, String fileName) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        T obj = null;
        try {
            fis = context.openFileInput(fileName);
            ois = new ObjectInputStream(fis);
            obj = (T) ois.readObject();
        } catch (ClassNotFoundException | IOException ioe) {
            ioe.printStackTrace();
        } finally {
            closeQuietly(fis);
            closeQuietly(ois);
        }
        return obj;
    }

    static <T extends Serializable> void saveFile(Context context, T file, String fileName) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(file);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            closeQuietly(fos);
            closeQuietly(oos);
        }
    }

    static int daysPast(long fromMs) {
        int days = (int) ((System.currentTimeMillis() - fromMs) / (1000 * 60 * 60 * 24));
        Log.d("Days Past", days + "days past from " + fromMs);
        return days;
    }

    static final String MATCH_URL_HTTPS = "(https)(:\\/\\/[-_.!~*\\'()a-zA-Z0-9;\\/?:\\@&=+\\$,%#]+)";
    static final String MATCH_URL_HTTP = "(http)(:\\/\\/[-_.!~*\\'()a-zA-Z0-9;\\/?:\\@&=+\\$,%#]+)";

    private static String blanks(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static int getActualTextLength(String text) {
        TwitterAPIConfiguration conf = TwitterUtils.getApiConfiguration();
        text = text.replaceAll(MATCH_URL_HTTP, Util.blanks(conf.getShortURLLength()));
        text = text.replaceAll(MATCH_URL_HTTPS, Util.blanks(conf.getShortURLLengthHttps()));
        return text.length();
    }

    public static boolean checkRuntimePermission(Activity activity, String permission, int requestId) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    requestId);
            return false;
        }
        return true;
    }

    public static boolean checkRuntimePermissions(Activity activity, String[] permissions, int requestId) {
        ArrayList<String> notGrantedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                notGrantedPermissions.add(permission);
            }
        }
        if (notGrantedPermissions.isEmpty()) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity,
                    notGrantedPermissions.toArray(new String[notGrantedPermissions.size()]),
                    requestId);
            return false;
        }

    }


    public static String parseSharedText(Uri data) {
        StringBuilder sb = new StringBuilder();
        boolean isFirstItem = true;
        boolean setLink = false;
        for (String q : new String[]{"text", "url", "original_referer", "via"}) {
            if (hasQuery(data, q)) {
                if (isFirstItem) {
                    isFirstItem = false;
                } else {
                    sb.append(' ');
                }

                if (q.equals("via")) {
                    sb.append("via @");
                }

                if (q.equals("url")) {
                    setLink = true;
                }

                if (q.equals("original_referer") && setLink) {
                    continue;
                }
                sb.append(data.getQueryParameter(q));
            }
        }
        return sb.toString();
    }

    private static boolean hasQuery(Uri data, String query) {
        String text = data.getQueryParameter(query);
        return text != null;
    }

    public static boolean clearFile(File file) {
        return file != null && file.exists() && file.delete();
    }

    public static String getValidVideoUrl(MediaEntity e) {
        for (MediaEntity.Variant v : e.getVideoVariants()) {
            if (v.getContentType().contains("mp4")) {
                return v.getUrl();
            }
        }
        return "";
    }

    public static boolean isPreLollipop() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isAfterOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public static void createNotificationChannel(Context context, String id, String name, int importance){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.enableVibration(false);
        manager.createNotificationChannel(channel);
    }
}