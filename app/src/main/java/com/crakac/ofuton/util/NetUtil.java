package com.crakac.ofuton.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.crakac.ofuton.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class NetUtil {
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private static final String ICON_CACHE_DIR = "icon";
    private static final String IMAGE_CACHE_DIR = "image";
    private static final int THREAD_POOL_SIZE_FOR_FETCHING_ICONS = 4;
    private static final int THREAD_POOL_SIZE_FOR_FETCHING_IMAGE = 3;
    private static ImageLoader sImageLoader, sIconLoader;
    private static Cache sImageDiskCache;

    private static Cache createDiskCache(Context context, String cacheRoot, int cacheSize){
        File cacheDir = new File(context.getCacheDir(), cacheRoot);
        return new DiskBasedCache(cacheDir, cacheSize);
    }
    /**
     * @param context
     * @param cacheRoot
     * @param cacheSize
     * @param threadPoolSize
     * @return
     */
    private static RequestQueue newRequestQueue(Context context, String cacheRoot, int cacheSize, int threadPoolSize){
        File cacheDir = new File(context.getCacheDir(), cacheRoot);
        return  newRequestQueue(context, new DiskBasedCache(cacheDir, cacheSize), threadPoolSize);
    }

    private static RequestQueue newRequestQueue(Context context, Cache cache, int threadPoolSize){
        HttpStack stack;
        String userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (NameNotFoundException e) {
        }

        if (Build.VERSION.SDK_INT >= 9) {
            stack = new HurlStack();
        } else {
            // Prior to Gingerbread, HttpUrlConnection was unreliable.
            // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
            stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
        }

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(cache, network, threadPoolSize);
        queue.start();

        return queue;
    }

    public static void init(Context context) {
        RequestQueue iconQueue = newRequestQueue(context, ICON_CACHE_DIR, 30*1024*1024, THREAD_POOL_SIZE_FOR_FETCHING_ICONS);
        sIconLoader = new ImageLoader(iconQueue, new ImageLruCache());
        sImageDiskCache = createDiskCache(context, IMAGE_CACHE_DIR, 20*1024*1024);
        RequestQueue imageQueue = newRequestQueue(context, sImageDiskCache, THREAD_POOL_SIZE_FOR_FETCHING_IMAGE);
        sImageLoader = new ImageLoader(imageQueue, new ImageLruCache());
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
        return sIconLoader.get(requestUrl, listener);
    }

    public static ImageContainer fetchNetworkImageAsync(ImageView targetView, String requestUrl) {
        return fetchNetworkImageAsync(targetView, requestUrl, android.R.color.transparent, android.R.color.transparent);
    }

    public static ImageContainer fetchNetworkImageAsync(ImageView targetView, String requestUrl, int defaultImageResId,
            int errorImageResId) {
        ImageListener listener = ImageLoader.getImageListener(targetView, defaultImageResId, errorImageResId);
        return fetchNetworkImageAsync(requestUrl, listener);
    }

    public static ImageContainer fetchNetworkImageAsync(String requestUrl, ImageListener listener){
        return sImageLoader.get(requestUrl, listener);
    }

    private static boolean isCached(String requestUrl) {
        return sImageLoader.isCached(requestUrl, 0, 0) || sImageLoader.isCached(requestUrl, 0, 0);
    }

    public static boolean shouldRecycle(ImageContainer container){
        return isCached(container.getRequestUrl()) && container.getBitmap() != null && !container.getBitmap().isRecycled();
    }


    public static String expandUrl(String urlString) throws IOException {
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

    public static String convertUriToImageUrl(Uri uri) {
        String host = uri.getHost();
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        if (host.equals("twitpic.com") || host.equals("img.ly")) {
            builder.authority(host);
            builder.path("show/full".concat(uri.getPath()));
        } else if (host.equals("gyazo.com")) {
            builder.authority("i.gyazo.com");
            builder.path(uri.getPath().concat(".png"));
        } else if (host.equals("instagram.com")) {
            builder = uri.buildUpon();
            builder.appendEncodedPath("media/?size=l");
        } else if (host.equals("p.twipple.jp")) {
            builder.authority("p.twpl.jp");
            builder.path("show/large".concat(uri.getPath()));
        }
        return builder.toString();
    }

    public static String expandUrlIfNecessary(Uri uri) throws IOException{
        //twitter公式ならMediaEntity#getMediaUrlでとれるurlでよい
        if(uri.getHost().equals("pbs.twimg.com")){
            return uri.toString();
        } else {
            return expandUrl(convertUriToImageUrl(uri));
        }
    }

    public static byte[] getCache(String key){
        Cache.Entry entry = sImageDiskCache.get(key);
        if(entry == null){
            return null;
        } else {
            return entry.data;
        }
    }

    public static File download(Context context, String url) {
        byte[] buf = new byte[4096];
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(NetUtil.expandUrl(url)).openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            //ファイル名を取得するために，先に開く．
            BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
            File cacheFile = new File(context.getCacheDir(), guessFileName(url));
            FileOutputStream os = new FileOutputStream(cacheFile);
            int length;
            while ((length = is.read(buf)) != -1) {
                os.write(buf, 0, length);
            }
            os.close();
            is.close();
            return cacheFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String guessFileName(String url){
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(url);
        String fileName = URLUtil.guessFileName(url, null, fileExtension);
        return fileName;
    }
}
