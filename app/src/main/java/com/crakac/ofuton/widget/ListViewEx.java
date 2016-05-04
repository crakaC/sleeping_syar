package com.crakac.ofuton.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by kosukeshirakashi on 2014/09/01.
 */
public class ListViewEx extends ListView implements AbsListView.OnScrollListener {
    private boolean mIsBottomOfLastItemShown = false;
    private OnLastItemVisibleListener mListener;

    public ListViewEx(Context c) {
        super(c);
    }

    public ListViewEx(Context c, AttributeSet attrs) {
        super(c, attrs);
    }

    public ListViewEx(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
    }

    private int preLast = -1;

    public void setOnLastItemVisibleListener(OnLastItemVisibleListener listener) {
        mListener = listener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        if (i == SCROLL_STATE_IDLE) {
            if (mIsBottomOfLastItemShown) {
                if (mListener != null) mListener.onBottomOfLastItemShown();
                Log.d("OnScroll", "Reach to bottom");
            }
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount == 0) return;
        int lastItem = firstVisibleItem + visibleItemCount;
        if (lastItem == totalItemCount) {
            mIsBottomOfLastItemShown = (absListView.getChildAt(visibleItemCount - 1).getBottom() <= absListView.getHeight());
            if (preLast != lastItem) {
                if (mListener != null) {
                    mListener.onLastItemVisible();
                }
            }
        } else {
            mIsBottomOfLastItemShown = false;
        }
        preLast = lastItem;
    }

    public interface OnLastItemVisibleListener {
        void onBottomOfLastItemShown();
        void onLastItemVisible();
    }
}