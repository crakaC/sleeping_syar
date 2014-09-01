package com.crakac.ofuton.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by kosukeshirakashi on 2014/09/01.
 */
public class ListViewEx extends ListView implements AbsListView.OnScrollListener{
    private boolean mIsLastItemVisible = false;
    private OnLastItemVisibleListener mListener;
    public ListViewEx(Context c){
        super(c);
    }
    public ListViewEx(Context c, AttributeSet attrs){
        super(c, attrs);
    }
    public ListViewEx(Context c, AttributeSet attrs, int defStyle){
        super(c, attrs, defStyle);
    }

    public void setOnLastItemVisibleListener(OnLastItemVisibleListener listener){
        mListener = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        if( i == SCROLL_STATE_IDLE && mIsLastItemVisible){
            if (mListener != null) mListener.onLastItemVisible();
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastItem = firstVisibleItem + visibleItemCount;
        mIsLastItemVisible = ( lastItem == totalItemCount &&
            absListView.getChildAt(visibleItemCount - 1).getBottom() <= absListView.getHeight());
    }

    public static interface OnLastItemVisibleListener {
        public void onLastItemVisible();
    }
}