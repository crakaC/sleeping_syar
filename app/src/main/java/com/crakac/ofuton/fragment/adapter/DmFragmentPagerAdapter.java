package com.crakac.ofuton.fragment.adapter;

import android.support.v4.app.FragmentManager;

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
