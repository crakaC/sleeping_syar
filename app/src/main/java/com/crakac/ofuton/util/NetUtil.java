package com.crakac.ofuton.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.crakac.ofuton.R;

import java.io.File;
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
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private static final String ICON_CACHE_DIR = "icon";
    private static final String IMAGE_CACHE_DIR = "image";
    private static final int THREAD_POOL_SIZE_FOR_FETCHING_ICONS = 5;
    private static final int THREAD_POOL_SIZE_FOR_FETCHING_IMAGE = 5;
    public static ImageLoader PREVIEW_LOADER, INLINE_PREVIEW_LOADER, ICON_LOADER;
    private static Cache sImageDiskCache;

    private NetUtil() {
    }

    private static Cache createDiskCache(Context context, String cacheRoot, int cacheSize) {
        File cacheDir = new File(context.getCacheDir(), cacheRoot);
        return new DiskBasedCache(cacheDir, cacheSize);
    }

    private static RequestQueue newRequestQueue(Context context, String cacheRoot, int cacheSize, int threadPoolSize) {
        File cacheDir = new File(context.getCacheDir(), cacheRoot);
        return newRequestQueue(context, new DiskBasedCache(cacheDir, cacheSize), threadPoolSize);
    }

    private static RequestQueue newRequestQueue(Context context, Cache cache, int threadPoolSize) {
        HttpStack stack = new HurlStack();

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(cache, network, threadPoolSize);
        queue.start();

        return queue;
    }

    public static void init(Context context) {
        RequestQueue iconQueue = newRequestQueue(context, ICON_CACHE_DIR, 5 * 1024 * 1024, THREAD_POOL_SIZE_FOR_FETCHING_ICONS);
        ICON_LOADER = new ImageLoader(iconQueue, new ImageLruCache());
        sImageDiskCache = createDiskCache(context, IMAGE_CACHE_DIR, 50 * 1024 * 1024);
        RequestQueue imageQueue = newRequestQueue(context, sImageDiskCache, THREAD_POOL_SIZE_FOR_FETCHING_IMAGE);
        INLINE_PREVIEW_LOADER = new ImageLoader(imageQueue, new ImageLruCache());
        PREVIEW_LOADER = new ImageLoader(newRequestQueue(context, sImageDiskCache, 3), new ImageLruCache());
    }

    public static ImageContainer fetchIconAsync(ImageView targetView, String requestUrl) {
        return fetchIconAsync(targetView, requestUrl, android.R.color.transparent, R.drawable.ic_syar);
    }

    public static ImageContainer fetchIconAsync(ImageView targetView, String requestUrl, int defaultImageResId,
                                                int errorImageResId) {
        ImageListener listener = ImageLoader.getImageListener(targetView, defaultImageResId, errorImageResId);
        return fetchIconAsync(requestUrl, listener);
    }

    public static ImageContainer fetchIconAsync(String requestUrl, ImageListener listener) {
        return ICON_LOADER.get(requestUrl, listener);
    }

    public static ImageContainer fetchNetworkImageAsync(ImageView targetView, String requestUrl) {
        return fetchNetworkImageAsync(targetView, requestUrl, android.R.color.transparent, android.R.color.transparent);
    }

    public static ImageContainer fetchNetworkImageAsync(ImageView targetView, String requestUrl, int defaultImageResId,
                                                        int errorImageResId) {
        ImageListener listener = ImageLoader.getImageListener(targetView, defaultImageResId, errorImageResId);
        return fetchNetworkImageAsync(requestUrl, listener);
    }

    public static ImageContainer fetchNetworkImageAsync(String requestUrl, ImageListener listener) {
        return INLINE_PREVIEW_LOADER.get(requestUrl, listener);
    }

    public static ImageContainer fetchPreviewImageAsync(String requestUrl, ImageListener listener) {
        return PREVIEW_LOADER.get(requestUrl, listener);
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
        Log.d("ExpandUrl Header", sb.toString());
        if (location != null && location.startsWith("http")) {
            Log.d("ExpandUrl", String.format("%s -> %s", urlString, location));
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

    public static byte[] getCache(String key) {
        Cache.Entry entry = sImageDiskCache.get(key);
        if (entry == null) {
            return null;
        } else {
            return entry.data;
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
