package com.crakac.ofuton.action.status;

import android.content.Context;
import android.content.Intent;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.ConversationActivity;

public class ConversationAction extends ClickAction {
	private twitter4j.Status status;
	public ConversationAction(Context context, twitter4j.Status st) {
		super(context, R.string.show_conversation, R.drawable.ic_question_answer);
		status = st.isRetweet() ? st.getRetweetedStatus() : st;
	}
	@Override
	public void doAction() {
		Intent intent = new Intent(mContext, ConversationActivity.class);
		intent.putExtra(C.STATUS, status);
		mContext.startActivity(intent);
	}
}
