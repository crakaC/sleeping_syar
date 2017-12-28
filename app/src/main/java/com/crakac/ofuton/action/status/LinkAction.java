package com.crakac.ofuton.action.status;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.crakac.ofuton.R;

public class LinkAction extends ClickAction {
	private String url;
	public LinkAction(Context context, String url) {
		super(context, 0, R.drawable.ic_open_in_browser);
		this.url = url;
	}

	@Override
	public String getText() {
		return url;
	}

	@Override
	public void doAction() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		mContext.startActivity(intent);
	}
}