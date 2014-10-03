package com.crakac.ofuton;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.crakac.ofuton.timeline.AbstractTimelineFragment;

/**
 * ViewPagerでフラグメントが切り替わったときに，ツイートの相対時間を更新する．
 * @author kosukeshirakashi
 *
 */
public class RelativeTimeUpdater extends ViewPager.SimpleOnPageChangeListener {
    private boolean mIsPageChanged = false;
    private int mPositioin;
    private FragmentStatePagerAdapter mAdapter;
    public RelativeTimeUpdater(FragmentStatePagerAdapter adapter){
        mAdapter = adapter;
    }
    @Override
    public void onPageSelected(int position) {
        mIsPageChanged = true;
        mPositioin = position;
    }
    @Override
    public void onPageScrollStateChanged(int state) {
        if(state == ViewPager.SCROLL_STATE_IDLE && mIsPageChanged){
            onScrollFinished(mPositioin);
            mIsPageChanged = false;
        }
    }

    protected void onScrollFinished(int position){
        Fragment f = mAdapter.getItem(position);
        if(f instanceof AbstractTimelineFragment){
            ((AbstractTimelineFragment)f).updateDisplayedTime();
        }
    }
}