package com.crakac.ofuton.dm;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import com.astuetz.PagerSlidingTabStrip;
import com.crakac.ofuton.R;

public class DmActivity extends ActionBarActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// レイアウトとタイトルの設定
		setContentView(R.layout.activity_dm);
		DmFragmentPagerAdapter pagerAdapter = new DmFragmentPagerAdapter(getSupportFragmentManager());
		pagerAdapter.add(new DmReceivedFragment());
		pagerAdapter.add(new DmSentFragment());
		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(pagerAdapter);

		PagerSlidingTabStrip tab = (PagerSlidingTabStrip)findViewById(R.id.tab);
		tab.setViewPager(pager);
	}
}