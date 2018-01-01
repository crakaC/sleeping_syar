package com.crakac.ofuton;


import android.app.Application;

import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.PrefUtil;
import com.crakac.ofuton.util.TwitterUtils;


public class OfutonApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TwitterUtils.init(this);
        AppUtil.init(this);
        PrefUtil.init(this);
        AppUtil.checkTofuBuster();
    }
}