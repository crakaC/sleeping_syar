package com.crakac.ofuton;


import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter{
	protected ArrayList<Fragment> mFragments;
	
	public SimpleFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragments = new ArrayList<Fragment>();
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