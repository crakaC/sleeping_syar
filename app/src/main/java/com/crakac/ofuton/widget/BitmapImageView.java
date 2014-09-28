package com.crakac.ofuton.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.crakac.ofuton.OfutonApp;

/**
 * Created by kosukeshirakashi on 2014/09/28.
 */
public class BitmapImageView extends ImageView {
    public BitmapImageView(Context context) {
        super(context);
    }

    public BitmapImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        OfutonApp app = (OfutonApp) getContext().getApplicationContext();
        if (bm != null) {
            app.incrementCount(this);
        } else {
            app.decrementCount(this);
        }
    }
}
