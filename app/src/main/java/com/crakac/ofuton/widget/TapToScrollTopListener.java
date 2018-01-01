package com.crakac.ofuton.widget;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.crakac.ofuton.fragment.AbstractPtrFragment;
import com.crakac.ofuton.fragment.adapter.SimpleFragmentPagerAdapter;
import com.crakac.ofuton.fragment.timeline.AbstractTimelineFragment;

/**
 * Created by Kosuke on 2017/12/23.
 */

public class TapToScrollTopListener implements TabLayout.OnTabSelectedListener {

    SimpleFragmentPagerAdapter<AbstractPtrFragment> mAdapter;
    ViewPager mPager;
    public TapToScrollTopListener(SimpleFragmentPagerAdapter<AbstractPtrFragment> pagerAdapter, ViewPager pager){
        mAdapter = pagerAdapter;
        mPager = pager;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        AbstractPtrFragment f = (AbstractPtrFragment) mAdapter.instantiateItem(mPager, tab.getPosition());
        if (f instanceof AbstractTimelineFragment) {
                ((AbstractTimelineFragment) f).updateDisplayedTime();
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        if(mAdapter == null || mPager == null)
            return;
        AbstractPtrFragment f = (AbstractPtrFragment) mAdapter.instantiateItem(mPager, tab.getPosition());
        f.scrollToTop();
    }
}
