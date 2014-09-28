package com.crakac.ofuton.user;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.crakac.ofuton.SimpleFragmentPagerAdapter;
import com.crakac.ofuton.timeline.AbstractStatusFragment;
import com.crakac.ofuton.util.AppUtil;

public class UserFragmentPagerAdapter extends SimpleFragmentPagerAdapter<Fragment> {

	int tweets, friends, followers, favs;

	public UserFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
        if(fm.getFragments() != null){
            for(Fragment f : fm.getFragments()){
                if(f instanceof AbstractStatusFragment || f instanceof  AbstractUserFragment){
                    add(f);
                }
            }
        }
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return AppUtil.shapingNums(tweets) + " ツイート";
		case 1:
			return AppUtil.shapingNums(friends) + " フォロー";
		case 2:
			return AppUtil.shapingNums(followers) + " フォロワー";
		case 3:
			return AppUtil.shapingNums(favs) + " お気に入り";
		}
		return null;
	}

	public void setCounts(int statusCounts, int friends, int followers, int favs) {
		this.tweets = statusCounts;
		this.friends = friends;
		this.followers = followers;
		this.favs = favs;
	}
}
