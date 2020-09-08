package com.crakac.ofuton.fragment.adapter;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
public class SearchFragmentPagerAdapter extends SimpleFragmentPagerAdapter<Fragment>{
    public SearchFragmentPagerAdapter(AppCompatActivity context, ViewPager viewPager) {
        super(context, viewPager);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "ローカル";
            case 1:
                return "すべて";
            case 2:
                return "ユーザー";
            case 3:
                return "画像";
            case 4:
                return "動画";
        }
        return "";
    }
}
