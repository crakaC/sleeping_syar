package com.crakac.ofuton.activity;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.crakac.ofuton.R;

public class ListSelectActivity extends FinishableActionbarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_list);
	}
}