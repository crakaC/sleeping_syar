package com.crakac.ofuton.action.status;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;

public class TofuBusterAction extends ClickAction {
	private String text;
	public TofuBusterAction(Context context, twitter4j.Status status) {
		super(context, R.string.tofu, R.drawable.ic_share);
		if (status.isRetweet()){
			this.text = status.getRetweetedStatus().getText();
		} else {
			this.text = status.getText();
		}
	}

	@Override
	public void doAction() {
		String ACTION_SHOW_TEXT = "com.product.kanzmrsw.tofubuster.ACTION_SHOW_TEXT";
		Intent intent = new Intent(ACTION_SHOW_TEXT);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.app_name));
		intent.putExtra("isCopyEnabled", true);
		try {
			mContext.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			AppUtil.showToast("TofuBusterがインストールされていません");
		}
	}

}
