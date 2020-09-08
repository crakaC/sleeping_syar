package com.crakac.ofuton.fragment.adapter;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.crakac.ofuton.fragment.dm.AbstractDmFragment;

public class DmFragmentPagerAdapter extends SimpleFragmentPagerAdapter<AbstractDmFragment> {

    public DmFragmentPagerAdapter(AppCompatActivity context, ViewPager pager) {
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
