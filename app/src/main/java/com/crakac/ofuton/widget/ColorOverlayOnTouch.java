package com.crakac.ofuton.widget;

import android.graphics.PorterDuff.Mode;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.crakac.ofuton.R;

public class ColorOverlayOnTouch implements OnTouchListener {

    Mode mMode;
    public ColorOverlayOnTouch(){
        this(Mode.SRC_OVER);
    }

    public ColorOverlayOnTouch(Mode mode){
        mMode = mode;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView iv = ((ImageView) v);
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            iv.setColorFilter(v.getResources().getColor(R.color.image_overlay), mMode);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            iv.clearColorFilter();
        case MotionEvent.ACTION_MOVE:
        }
        return false;
    }

}
