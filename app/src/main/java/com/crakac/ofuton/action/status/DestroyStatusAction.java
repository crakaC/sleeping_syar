package com.crakac.ofuton.action.status;

import android.content.Context;

import com.crakac.ofuton.R;
import com.crakac.ofuton.adapter.TweetStatusAdapter;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class DestroyStatusAction extends ClickAction {
	private TweetStatusAdapter statusAdapter;
	twitter4j.Status selectedStatus;
	public DestroyStatusAction(Context context, TweetStatusAdapter adapter, twitter4j.Status status) {
		super(context, R.string.destroy_status, R.drawable.ic_delete_white_36dp);
		statusAdapter = adapter;
		selectedStatus = status;
	}
	@Override
	public void doAction() {
		ParallelTask<Void, Void, twitter4j.Status> task = new ParallelTask<Void, Void, twitter4j.Status>() {
			@Override
			protected twitter4j.Status doInBackground(Void... params) {
				Twitter mTwitter = TwitterUtils.getTwitterInstance();
				try {
					if(!selectedStatus.isRetweet()){
						return mTwitter.destroyStatus(selectedStatus.getId());
					} else {
						return mTwitter.destroyStatus(selectedStatus.getRetweetedStatus().getId());
					}
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(twitter4j.Status result) {
				if (result == null) {
					AppUtil.showToast("無理でした");
				} else {
					AppUtil.showToast("ツイートを削除しました");
					statusAdapter.remove(result);
				}
			}
		};
		task.executeParallel();
	}
}
