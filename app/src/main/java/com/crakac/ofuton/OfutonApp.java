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

    @Override
    public void onCreate() {
        super.onCreate();
        // The following line triggers the initialization of ACRA
        TwitterUtils.init(this);
        AppUtil.init(this);
        NetUtil.init(this);
        PreferenceUtil.init(this);
    }
}