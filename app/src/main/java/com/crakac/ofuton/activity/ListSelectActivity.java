package com.crakac.ofuton.activity;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.crakac.ofuton.R;
import com.crakac.ofuton.fragment.UsersListFragment;

public class ListSelectActivity extends FinishableActionbarActivity {
	private Fragment mFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_list);
		if (mFragment == null){
			mFragment = new UsersListFragment();
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.userList, mFragment);
			ft.commit();
		}
	}
}