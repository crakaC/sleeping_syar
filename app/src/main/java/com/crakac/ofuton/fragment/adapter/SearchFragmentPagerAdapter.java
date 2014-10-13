package com.crakac.ofuton.fragment.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.crakac.ofuton.fragment.search.TweetSearchFragment;
import com.crakac.ofuton.fragment.search.UserSearchFragment;

import java.util.List;

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
public class SearchFragmentPagerAdapter extends SimpleFragmentPagerAdapter<Fragment>{
    public SearchFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        if (fm.getFragments() != null) {
            for (Fragment f : fm.getFragments()) {
                if (f instanceof UserSearchFragment || f instanceof TweetSearchFragment) {
                    add(f);
                }
            }
        }
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

    public List<Fragment> getFragments(){
        return mFragments;
    }
}
