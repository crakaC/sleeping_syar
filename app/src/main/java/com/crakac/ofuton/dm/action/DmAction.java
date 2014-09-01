package com.crakac.ofuton.dm.action;

import android.content.Context;

import com.mobeta.android.dslv.DragSortListView;

public class DmAction {
	protected Context mContext;
	protected int stringId, iconId;
    DragSortListView lv;
	public DmAction(Context context, int stringId, int iconId ){
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
	public void doAction() {
	}
}
