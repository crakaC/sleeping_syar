package com.crakac.ofuton.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Build;
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
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.crakac.ofuton.BuildConfig;
import com.crakac.ofuton.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NetUtil {
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private static final String ICON_CACHE_DIR = "icon";
    private static final String IMAGE_CACHE_DIR = "image";
    private static final int THREAD_POOL_SIZE_FOR_FETCHING_ICONS = 2;
    private static final int THREAD_POOL_SIZE_FOR_FETCHING_IMAGE = 2;
    public static ImageLoader PREVIEW_LOADER, ICON_LOADER;
    private static Cache sImageDiskCache;

    private static Cache createDiskCache(Context context, String cacheRoot, int cacheSize) {
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
    private static RequestQueue newRequestQueue(Context context, String cacheRoot, int cacheSize, int threadPoolSize) {
        File cacheDir = new File(context.getCacheDir(), cacheRoot);
        return newRequestQueue(context, new DiskBasedCache(cacheDir, cacheSize), threadPoolSize);
    }

    private static RequestQueue newRequestQueue(Context context, Cache cache, int threadPoolSize) {
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
        RequestQueue iconQueue = newRequestQueue(context, ICON_CACHE_DIR, 50 * 1024 * 1024, THREAD_POOL_SIZE_FOR_FETCHING_ICONS);
        ICON_LOADER = new ImageLoader(iconQueue, new ImageLruCache());
        sImageDiskCache = createDiskCache(context, IMAGE_CACHE_DIR, 50 * 1024 * 1024);
        RequestQueue imageQueue = newRequestQueue(context, sImageDiskCache, THREAD_POOL_SIZE_FOR_FETCHING_IMAGE);
        PREVIEW_LOADER = new ImageLoader(imageQueue, new ImageLruCache());
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
        while(it.hasNext()){
            String key = it.next();
            sb.append(key + ":" + headers.get(key).toString() + "\n");
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
        builder.scheme("http");
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
                builder.authority("instagram.com");
            case "instagram.com":
                builder = uri.buildUpon();
                builder.appendEncodedPath("media/?size=l");
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
            case "instagram.com":
            case "instagr.am":
                List<String> segments = uri.getPathSegments();
                StringBuilder sb = new StringBuilder();
                if (BuildConfig.DEBUG) {
                    for (int i = 0; i < segments.size(); i++) {
                        sb.append(i);
                        sb.append(':');
                        sb.append(segments.get(i));
                        sb.append(", ");
                    }
                    Log.d("isMediaUrl:instagram", sb.toString());
                }
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

    public static File download(Context context, String url) {
        byte[] buf = new byte[4096];
        try {
            String expandedUrl = NetUtil.expandUrlIfNecessary(getOriginalImageUrl(url));
            HttpURLConnection conn = (HttpURLConnection) new URL(expandedUrl).openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            //ファイル名を取得するために，先に開く．
            BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
            File downloadedFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), createFileName(conn.getContentType()));
            FileOutputStream os = new FileOutputStream(downloadedFile);
            int length;
            while ((length = is.read(buf)) != -1) {
                os.write(buf, 0, length);
            }
            os.close();
            is.close();
            String[] path = {downloadedFile.getPath()};
            String extension = MimeTypeMap.getFileExtensionFromUrl(downloadedFile.getAbsolutePath());
            String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            String[] mimetypes = {mimetype};
            MediaScannerConnection.scanFile(context.getApplicationContext(), path, mimetypes, null);
            return downloadedFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getOriginalImageUrl(String url) {
        if (!Uri.parse(url).getHost().equals("pbs.twimg.com")) return url;
        int lastIndex = url.lastIndexOf(':');
        if (lastIndex > url.indexOf(':'))
            return url.substring(0, lastIndex).concat(":orig");
        else
            return url.concat(":orig");
    }

    private static String createFileName(String contentType) {
        String now = new SimpleDateFormat("yyyyMMddhhmmss.").format(new Date());
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType);
        return now.concat(extension);
    }
}
