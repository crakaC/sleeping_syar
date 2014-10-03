package com.crakac.ofuton;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class SimpleFragmentPagerAdapter<T extends Fragment> extends FragmentPagerAdapter{
	protected ArrayList<T> mFragments;

	public SimpleFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragments = new ArrayList<>();
	}
	public T getItem(int cnt) {
		return mFragments.get(cnt);
	}
	@Override
	public int getCount() {
		return mFragments.size();
	}

	public void add(T fragment){
		mFragments.add(fragment);
	}
}