package com.crakac.ofuton.fragment.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.crakac.ofuton.fragment.AbstractStatusFragment;
import com.crakac.ofuton.fragment.AbstractUserFragment;
import com.crakac.ofuton.util.AppUtil;

public class UserFragmentPagerAdapter extends SimpleFragmentPagerAdapter<Fragment> {

	int tweets, friends, followers, favs;

    public UserFragmentPagerAdapter(FragmentActivity context, ViewPager viewPager) {
        super(context, viewPager);
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
