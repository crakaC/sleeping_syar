package com.crakac.ofuton.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.crakac.ofuton.SimpleFragmentPagerAdapter;
import com.crakac.ofuton.timeline.AbstractStatusFragment;
import com.crakac.ofuton.user.AbstractUserFragment;

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
                return "ツイート";
            case 1:
                return "ユーザー";
            case 2:
                return "画像";
        }
        return "";
    }
}
