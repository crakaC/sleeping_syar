package com.crakac.ofuton.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;

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

    private final FloatingActionButton.FabOnScrollListener mFabOnScrollListener = new FloatingActionButton.FabOnScrollListener();

    public void setOnLastItemVisibleListener(OnLastItemVisibleListener listener) {
        mListener = listener;
    }

    public void setFab(FloatingActionButton button){
        mFabOnScrollListener.setFloatingActionButton(button);
        mFabOnScrollListener.setListView(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        mFabOnScrollListener.onScrollStateChanged(absListView, i);
        if (i == SCROLL_STATE_IDLE) {
            if (mIsBottomOfLastItemShown) {
                if (mListener != null) mListener.onBottomOfLastItemShown();
                Log.d("OnScroll", "Reach to bottom");
            }
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFabOnScrollListener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);
        if (visibleItemCount == 0) return;
        int lastItem = firstVisibleItem + visibleItemCount;
        if (lastItem == totalItemCount) {
            mIsBottomOfLastItemShown = (absListView.getChildAt(visibleItemCount - 1).getBottom() <= absListView.getHeight());
            if (preLast != lastItem) {
                if (mListener != null) {
                    mListener.onLastItemVisible();
                    Log.d("OnScroll", "Last item is visible");
                }
            }
        } else {
            mIsBottomOfLastItemShown = false;
        }
        preLast = lastItem;
    }

    private static class ScrollDirectionDetector implements OnScrollListener{
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    }

    public static interface OnLastItemVisibleListener {
        public void onBottomOfLastItemShown();

        public void onLastItemVisible();
    }
}