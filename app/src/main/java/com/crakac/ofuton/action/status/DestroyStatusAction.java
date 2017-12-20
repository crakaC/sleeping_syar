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
	twitter4j.Status selectedStatus;
	public DestroyStatusAction(Context context, twitter4j.Status status) {
		super(context, R.string.destroy_status, R.drawable.ic_delete_white_36dp);
		selectedStatus = status;
	}
	@Override
	public void doAction() {
		ParallelTask<Void, twitter4j.Status> task = new ParallelTask<Void, twitter4j.Status>() {
			@Override
			protected twitter4j.Status doInBackground() {
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
					for (TweetStatusAdapter adapter : TweetStatusAdapter.getAdapters()){
						int pos = adapter.getPosition(selectedStatus);
						if(pos < 0)
							continue;
						adapter.remove(selectedStatus);
						adapter.notifyDataSetChanged();
					}
				}
			}
		};
		task.executeParallel();
	}
}
