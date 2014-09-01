package com.crakac.ofuton.dm;

import android.support.v4.app.FragmentManager;

import com.crakac.ofuton.SimpleFragmentPagerAdapter;

public class DmFragmentPagerAdapter extends SimpleFragmentPagerAdapter {

	public DmFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "Received";
		case 1:
			return "Sent";
		}
		return null;
	}
}
