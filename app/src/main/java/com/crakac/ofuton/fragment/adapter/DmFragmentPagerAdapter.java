package com.crakac.ofuton.fragment.adapter;

import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

public class DmFragmentPagerAdapter extends SimpleFragmentPagerAdapter {

    public DmFragmentPagerAdapter(FragmentActivity context, ViewPager pager) {
        super(context, pager);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Received";
            case 1:
                return "Sent";
        }
        return null;
    }
}
