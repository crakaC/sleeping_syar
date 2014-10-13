package com.crakac.ofuton.fragment.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.crakac.ofuton.fragment.search.TweetSearchFragment;
import com.crakac.ofuton.fragment.search.UserSearchFragment;

import java.util.List;

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
public class SearchFragmentPagerAdapter extends SimpleFragmentPagerAdapter<Fragment>{
    public SearchFragmentPagerAdapter(FragmentActivity context, ViewPager viewPager) {
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
        }
        return "";
    }
}
