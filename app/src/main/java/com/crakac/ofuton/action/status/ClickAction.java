package com.crakac.ofuton.action.status;

import android.content.Context;

abstract public class ClickAction{
	protected Context mContext;
	protected int stringId, iconId;
	public ClickAction(Context context, int stringId, int iconId){
		mContext = context;
		this.stringId = stringId;
		this.iconId = iconId;
	}

	public String getText() {
		return mContext.getString(stringId);
	}

	public int getIconId() {
		return iconId;
	}

	public abstract void doAction();
}