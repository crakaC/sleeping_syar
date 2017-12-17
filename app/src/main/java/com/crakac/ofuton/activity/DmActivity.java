package com.crakac.ofuton.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.crakac.ofuton.R;
import com.crakac.ofuton.fragment.adapter.DmFragmentPagerAdapter;
import com.crakac.ofuton.fragment.dm.DmReceivedFragment;
import com.crakac.ofuton.fragment.dm.DmSentFragment;

public class DmActivity extends FinishableActionbarActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// レイアウトとタイトルの設定
		setContentView(R.layout.activity_simple_tab);
        ViewPager pager = findViewById(R.id.pager);
        DmFragmentPagerAdapter pagerAdapter = new DmFragmentPagerAdapter(this, pager);
		pagerAdapter.add(DmReceivedFragment.class, 0);
		pagerAdapter.add(DmSentFragment.class, 1);
        pagerAdapter.notifyDataSetChanged();

		TabLayout tab = findViewById(R.id.tab);
		tab.setupWithViewPager(pager);
	}
}