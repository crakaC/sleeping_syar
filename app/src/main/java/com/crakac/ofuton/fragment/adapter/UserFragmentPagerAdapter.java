package com.crakac.ofuton.fragment.adapter;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.crakac.ofuton.fragment.AbstractPtrFragment;
import com.crakac.ofuton.util.AppUtil;

public class UserFragmentPagerAdapter extends SimpleFragmentPagerAdapter<AbstractPtrFragment> {

	int tweets, friends, followers, favs;

    public UserFragmentPagerAdapter(AppCompatActivity context, ViewPager viewPager) {
        super(context, viewPager);
    }

    @Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return AppUtil.shapingNums(tweets) + "\nツイート";
		case 1:
			return AppUtil.shapingNums(friends) + "\nフォロー";
		case 2:
			return AppUtil.shapingNums(followers) + "\nフォロワー";
		case 3:
			return AppUtil.shapingNums(favs) + "\nお気に入り";
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
