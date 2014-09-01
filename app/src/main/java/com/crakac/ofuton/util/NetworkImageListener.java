package com.crakac.ofuton.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;

public class NetworkImageListener implements ImageListener {
    private ImageView mView;
    private int mDefaultResId = 0;
    private int mErrorResId = 0;

    public NetworkImageListener(ImageView view){
        this(view, android.R.color.transparent, android.R.color.transparent);
    }

    public NetworkImageListener(ImageView view, final int defaultImageResId, final int errorImageResId){
        mView = view;
        mDefaultResId = defaultImageResId;
        mErrorResId = errorImageResId;
    }
    @Override
    public void onErrorResponse(VolleyError error) {
        if(mErrorResId != 0){
            mView.setImageResource(mErrorResId);
        }
        onError(error);
    }

    @Override
    public void onResponse(ImageContainer response, boolean isImmediate) {
        if (response.getBitmap() != null) {
            mView.setImageBitmap(response.getBitmap());
            onBitmap(response.getBitmap());
        } else if (mDefaultResId != 0) {
            mView.setImageResource(mDefaultResId);
        }
    }

    protected void onError(VolleyError error) {
    }

    protected void onBitmap(Bitmap bitmap) {
    }
}
