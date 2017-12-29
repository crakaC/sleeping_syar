package com.crakac.ofuton.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Created by Kosuke on 2017/12/30.
 */
@GlideModule
public class OfutonGlide extends AppGlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDefaultTransitionOptions(Drawable.class, DrawableTransitionOptions.withCrossFade());
        builder.setDefaultTransitionOptions(Bitmap.class, BitmapTransitionOptions.withCrossFade());
    }
}
