package com.crakac.ofuton.dm.action;

import android.content.Context;
import android.content.Intent;

import com.crakac.ofuton.R;
import com.crakac.ofuton.user.UserDetailActivity;

import twitter4j.DirectMessage;

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
