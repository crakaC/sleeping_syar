package com.crakac.ofuton.util;

import android.support.v4.util.LruCache;

/**
 * Created by kosukeshirakashi on 2014/10/07.
 */
public class UrlCache extends LruCache<String, String> {
    static int cacheSize(){
        int max = (int)(Runtime.getRuntime().maxMemory());
        return max / 16;
    }

    public UrlCache(){
        this(cacheSize());
    }

    public UrlCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, String value) {
        return key.getBytes().length + value.getBytes().length;
    }
}