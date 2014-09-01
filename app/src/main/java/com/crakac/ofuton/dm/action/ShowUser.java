package com.crakac.ofuton.dm.action;

import twitter4j.DirectMessage;

import com.crakac.ofuton.R;
import com.crakac.ofuton.user.UserDetailActivity;

import android.content.Context;
import android.content.Intent;

public class ShowUser extends DmAction{
	private DirectMessage dm;
	public ShowUser(Context context, DirectMessage dm) {
		super(context, 0, R.drawable.ic_menu_user);
		this.dm = dm;
	}
	
	@Override
	public String getText() {
		return "@" + dm.getSenderScreenName();
	}
	@Override
	public void doAction() {
		Intent intent = new Intent(mContext, UserDetailActivity.class);
		intent.putExtra("screenName", dm.getSenderScreenName());
		mContext.startActivity(intent);
	}	
}
