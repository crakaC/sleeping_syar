package com.crakac.ofuton.action.status;

import android.content.Context;
import android.content.Intent;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.TweetActivity;

public class ReplyAction extends ClickAction {
	private twitter4j.Status status;
	public ReplyAction(Context context, twitter4j.Status status) {
		super(context, R.string.reply, R.drawable.ic_reply);
		this.status = status;
	}
	@Override
	public void doAction() {
		Intent intent = new Intent(mContext, TweetActivity.class);
		if (status.isRetweet()) {
			status = status.getRetweetedStatus();
		}
		intent.putExtra(C.REPLY_ID, status.getId());
		intent.putExtra(C.SCREEN_NAME, status.getUser().getScreenName());
		intent.putExtra(C.STATUS, status);
		mContext.startActivity(intent);
	}
}
