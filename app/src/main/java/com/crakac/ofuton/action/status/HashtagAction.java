package com.crakac.ofuton.action.status;

import android.content.Context;
import android.content.Intent;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.TweetActivity;

public class HashtagAction extends ClickAction {
	private String tag;
	public HashtagAction(Context context, String tag) {
		super(context, 0, R.drawable.ic_create);
		this.tag = tag;
	}

	@Override
	public String getText() {
		return "#" + tag;
	}

	@Override
	public void doAction() {
		Intent intent = new Intent(mContext, TweetActivity.class);
		intent.putExtra(C.HASH_TAG , "#" + tag);
		mContext.startActivity(intent);
	}
}
