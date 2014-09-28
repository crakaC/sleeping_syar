package com.crakac.ofuton;


import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.PreferenceUtil;
import com.crakac.ofuton.util.TwitterUtils;

import java.util.concurrent.ConcurrentHashMap;


public class OfutonApp extends Application {

    private ConcurrentHashMap<ImageView, String> mBitmapReferenceCount;
    private static Object Lock;

    @Override
    public void onCreate() {
        super.onCreate();
        // The following line triggers the initialization of ACRA
        TwitterUtils.init(this);
        AppUtil.init(this);
        NetUtil.init(this);
        PreferenceUtil.init(this);
        mBitmapReferenceCount = new ConcurrentHashMap<>();
    }

    synchronized public void incrementCount(ImageView iv) {
        String url = getRequestUrl(iv);
        Log.d("BitmapCache", "[increment]" + iv.toString() + ":" + url);
        if (url == null) {
            mBitmapReferenceCount.put(iv, "");
        } else {
            mBitmapReferenceCount.put(iv, url);
        }
    }

    synchronized public void decrementCount(ImageView iv) {
        String url = getRequestUrl(iv);
        Log.d("BitmapCache", "[decrement]" + iv.toString() + ":" + url);
        if (url == null) return;
        String lastUrl = mBitmapReferenceCount.remove(iv);
        if(!url.equals(lastUrl)){
            Log.w("BitmapCache", iv.toString() + url +"->"+lastUrl);
        }
        if (!mBitmapReferenceCount.containsValue(url)) {
            if (NetUtil.shouldRecycle(((ImageLoader.ImageContainer) iv.getTag()))) {
                Log.d("BitmapCache", getBitmap(iv).toString() + " Recycle!!!" + url);
                getBitmap(iv).recycle();
            }
        }
    }

    private Bitmap getBitmap(ImageView iv) {
        ImageLoader.ImageContainer container = (ImageLoader.ImageContainer) iv.getTag();
        return (container == null) ? null : container.getBitmap();
    }

    private String getRequestUrl(ImageView iv) {
        ImageLoader.ImageContainer container = (ImageLoader.ImageContainer) iv.getTag();
        return (container == null) ? null : container.getRequestUrl();
    }
}