package com.crakac.ofuton.action.status;

import android.content.Context;

import com.crakac.ofuton.R;
import com.crakac.ofuton.adapter.TweetStatusAdapter;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class RetweetAction extends ClickAction {
	private twitter4j.Status selectedStatus;
	//private TweetStatusAdapter mAdapter;
	/**
	 *
	 * @param context
	 *            リソース呼び出し用
	 */
	public RetweetAction(Context context, twitter4j.Status status) {
		super(context, 0, R.drawable.ic_repeat_white_36dp);
		mContext = context;
		selectedStatus = status;
	//	mAdapter = adapter;
	}

	@Override
	public String getText() {
		return mContext.getString(R.string.retweet);
	}

	@Override
	public void doAction() {
		ParallelTask<Void, twitter4j.Status>task = new ParallelTask<Void, twitter4j.Status>() {
			@Override
			protected twitter4j.Status doInBackground() {
				Twitter mTwitter = TwitterUtils.getTwitterInstance();
				try {
					return mTwitter.retweetStatus(selectedStatus.getId());
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(twitter4j.Status result) {
				if (result != null) {
					AppUtil.showToast("リツイートしました");
					/*
					int pos = mAdapter.getPosition(selectedStatus);
					mAdapter.remove(selectedStatus);
					mAdapter.insert(result.getRetweetedStatus(), pos);
					mAdapter.notifyDataSetChanged(); //
					//*/
				} else {
					AppUtil.showToast(mContext
							.getString(R.string.something_wrong));
				}
			}
		};
		task.executeParallel();
	}
}
