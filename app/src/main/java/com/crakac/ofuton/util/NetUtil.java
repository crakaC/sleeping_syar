package com.crakac.ofuton.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NetUtil {
    private NetUtil() {
    }

    synchronized private static String expandUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        final URLConnection conn = url.openConnection();
        if (!(conn instanceof HttpURLConnection)) {
            return url.toString();
        }
        final HttpURLConnection httpConn = (HttpURLConnection) conn;
        httpConn.setRequestMethod("HEAD");
        httpConn.setInstanceFollowRedirects(false);
        httpConn.connect();

        final String expandUrl;
        final String location = httpConn.getHeaderField("Location");
        Map headers = conn.getHeaderFields();
        Iterator<String> it = httpConn.getHeaderFields().keySet().iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sb.append(key).append(":").append(headers.get(key).toString()).append("\n");
        }
        if (location != null && location.startsWith("http")) {
            final int tResponseCode = httpConn.getResponseCode();
            if (tResponseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || tResponseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                return expandUrl(location);
            }
            expandUrl = location;
        } else {
            expandUrl = httpConn.getURL().toExternalForm();
        }
        return expandUrl;
    }

    public static String convertToImageFileUrl(String url) {
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        Uri.Builder builder = uri.buildUpon();
        switch (host) {
            case "twitpic.com":
            case "img.ly":
                builder.authority(host);
                builder.path("show/full".concat(uri.getPath()));
                break;
            case "gyazo.com":
                builder.authority("i.gyazo.com");
                builder.path(uri.getPath().concat(".png"));
                break;
            case "instagr.am":
            case "www.instagram.com":
                String query = uri.getQuery();
                if (query != null && query.contains("size")) {
                    builder.clearQuery();
                    builder.appendQueryParameter("size", "l");
                } else {
                    builder.appendEncodedPath("media/?size=l");
                }
                break;
            case "p.twipple.jp":
                builder.authority("p.twpl.jp");
                builder.path("show/large".concat(uri.getPath()));
                break;
        }
        return builder.toString();
    }

    public static String expandUrlIfNecessary(String url) throws IOException {
        //twitter公式ならMediaEntity#getMediaUrlでとれるurlでよい
        Uri uri = Uri.parse(url);
        if (uri.getHost().equals("pbs.twimg.com")) {
            return uri.toString();
        } else {
            return expandUrl(url);
        }
    }

    public static boolean isMediaUrl(String url) {
        Uri uri = Uri.parse(url);
        String path = uri.getPath();
        if (TextUtils.isEmpty(path)) return false;
        switch (uri.getHost()) {
            case "twitpic.com":
            case "img.ly":
            case "gyazo.com":
            case "p.twipple.jp":
                return true;
            case "www.instagram.com":
            case "instagr.am":
                List<String> segments = uri.getPathSegments();
                return segments.get(0).equals("p");
            default:
                return false;
        }
    }

    public static long download(Context context, String url) {
        Uri uri = Uri.parse(url);
        String fileName = uri.getLastPathSegment();
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(createFileName(url))
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .allowScanningByMediaScanner();
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        return dm.enqueue(request);

    }

    private static String createFileName(String url) {
        String now = new SimpleDateFormat("yyyyMMddhhmmss.", Locale.getDefault()).format(new Date());
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        return now.concat(extension);
    }
}
