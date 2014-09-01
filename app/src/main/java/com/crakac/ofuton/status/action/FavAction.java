package com.crakac.ofuton.status.action;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;

import com.crakac.ofuton.R;
import com.crakac.ofuton.status.TweetStatusAdapter;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;

public class FavAction extends ClickAction {
	
	private TweetStatusAdapter statusAdapter;
	private twitter4j.Status selectedStatus;

	public FavAction(Context context, TweetStatusAdapter adapter, twitter4j.Status status) {
		super(context, R.string.favorite, R.drawable.ic_menu_favorite);
		mContext = context;
		statusAdapter = adapter;
		selectedStatus = status;
		if (selectedStatus.isFavorited()) {
			stringId = R.string.unfavorite;
			iconId =  R.drawable.ic_menu_unfav;
		} else {
			stringId = R.string.favorite;
			iconId = R.drawable.ic_menu_favorite;
		}
	}

	@Override
	public void doAction() {
		ParallelTask<Void, Void, twitter4j.Status>task = new ParallelTask<Void, Void, twitter4j.Status>() {
			@Override
			protected twitter4j.Status doInBackground(Void... params) {
				Twitter mTwitter = TwitterUtils.getTwitterInstance();
				try {
					if (selectedStatus.isFavorited()) {
						return mTwitter.destroyFavorite(selectedStatus.getId());
					} else {
						return mTwitter.createFavorite(selectedStatus.getId());
					}
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(twitter4j.Status result) {
				if (result != null) {
					if (selectedStatus.isFavorited()) {
						AppUtil.showToast("お気に入りから削除しました");
					} else {
						AppUtil.showToast("お気に入りに追加しました");
					}
					int pos = statusAdapter.getPosition(selectedStatus);
					statusAdapter.remove(selectedStatus);
					statusAdapter.insert(result, pos);
					statusAdapter.notifyDataSetChanged();
				} else {
					AppUtil.showToast(mContext
							.getString(R.string.something_wrong));
				}
			}
		};
		task.executeParallel();
	}
}
