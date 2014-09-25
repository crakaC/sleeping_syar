package com.crakac.ofuton;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter{
	protected ArrayList<Fragment> mFragments;

	public SimpleFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragments = new ArrayList<>();
	}
	public Fragment getItem(int cnt) {
		return mFragments.get(cnt);
	}
	@Override
	public int getCount() {
		return mFragments.size();
	}

	public void add(Fragment fragment){
		mFragments.add(fragment);
	}
}