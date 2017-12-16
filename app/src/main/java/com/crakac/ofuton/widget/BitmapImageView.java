package com.crakac.ofuton.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

/**
 * Created by kosukeshirakashi on 2014/09/28.
 */
public class BitmapImageView extends NetworkImageView {
    public BitmapImageView(Context context) {
        super(context);
    }

    public BitmapImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BitmapImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void cleanUp(){
        if(mImageContainer != null){
            mImageContainer.cancelRequest();
            mImageContainer = null;
        }
        setImageBitmap(null);
    }
}
