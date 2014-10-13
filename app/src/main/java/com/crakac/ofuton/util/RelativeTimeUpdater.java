package com.crakac.ofuton.util;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.crakac.ofuton.fragment.adapter.SimpleFragmentPagerAdapter;
import com.crakac.ofuton.fragment.timeline.AbstractTimelineFragment;

/**
 * ViewPagerでフラグメントが切り替わったときに，ツイートの相対時間を更新する．
 * @author kosukeshirakashi
 *
 */
public class RelativeTimeUpdater extends ViewPager.SimpleOnPageChangeListener {
    private boolean mIsPageChanged = false;
    private SimpleFragmentPagerAdapter<?> mAdapter;
    public RelativeTimeUpdater(SimpleFragmentPagerAdapter<?> adapter){
        mAdapter = adapter;
    }
    @Override
    public void onPageSelected(int position) {
        mIsPageChanged = true;
    }
    @Override
    public void onPageScrollStateChanged(int state) {
        if(state == ViewPager.SCROLL_STATE_IDLE && mIsPageChanged){
            onPageChanged();
            mIsPageChanged = false;
        }
    }

    protected void onPageChanged(){
        for(Fragment f : mAdapter.getFragments()){
            if (f instanceof AbstractTimelineFragment) {
                ((AbstractTimelineFragment) f).updateDisplayedTime();
            }
        }
    }
}