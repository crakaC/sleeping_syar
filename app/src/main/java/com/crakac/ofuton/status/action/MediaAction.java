package com.crakac.ofuton.status.action;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.crakac.ofuton.R;
import com.crakac.ofuton.WebImagePreviewActivity;

public class MediaAction extends ClickAction {
	private String mDispUrl, mMediaUrl;
	public MediaAction(Context context, String displayUrl, String mediaUrl) {
		super(context, 0, R.drawable.ic_menu_media);
		mDispUrl = displayUrl;
		mMediaUrl = mediaUrl;
	}

	@Override
	public String getText() {
		return mDispUrl;
	}

	@Override
	public void doAction() {
		Intent intent = new Intent(mContext, WebImagePreviewActivity.class);
		intent.setData(Uri.parse(mMediaUrl));
		mContext.startActivity(intent);
		((Activity)mContext).overridePendingTransition(R.anim.fade_in, 0);
	}
}
