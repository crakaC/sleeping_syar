package com.crakac.ofuton.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.crakac.ofuton.R;

/**
 * Created by kosukeshirakashi on 2014/09/26.
 */
public class PreviewNavigation extends FrameLayout implements View.OnClickListener {
    private View mRotateLeft, mRotateRight, mDownload;
    private NavigationListener mListener;
    public PreviewNavigation(Context context) {
        super(context);
    }

    public PreviewNavigation(Context context, AttributeSet attrs) {
        super(context, attrs);
        View layout = LayoutInflater.from(context).inflate(R.layout.preview_nav, this);
        mRotateLeft = layout.findViewById(R.id.rotateLeft);
        mRotateRight = layout.findViewById(R.id.rotateRight);
        mDownload = layout.findViewById(R.id.download);
        setClickListener(mRotateLeft, mRotateRight, mDownload);
    }

    public PreviewNavigation(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onClick(View v) {
        if(mListener == null) return;
        switch(v.getId()){
            case R.id.rotateLeft:
                mListener.onRotateLeftClick();
                break;
            case R.id.rotateRight:
                mListener.onRotateRightClick();
                break;
            case R.id.download:
                mListener.onDownloadClick();
                break;
        }
    }

    public void setNavigationListener(NavigationListener listener){
        mListener = listener;
    }

    private void setClickListener(View... views){
        for(View v : views){
            v.setOnClickListener(this);
        }
    }

    public static interface NavigationListener {
        void onDownloadClick();
        void onRotateLeftClick();
        void onRotateRightClick();
    }
}
