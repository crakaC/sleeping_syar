package com.crakac.ofuton.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by kosukeshirakashi on 2014/09/01.
 */
public class ListViewEx extends ListView implements AbsListView.OnScrollListener{
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
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int lastItem = absListView.getLastVisiblePosition();
        if ( lastItem == absListView.getAdapter().getCount() -1 &&
                absListView.getChildAt(absListView.getChildCount() - 1).getBottom() <= absListView.getHeight())
        {
            if (mListener != null) mListener.onLastItemVisible();
        }
    }

    public static interface OnLastItemVisibleListener {
        public void onLastItemVisible();
    }
}