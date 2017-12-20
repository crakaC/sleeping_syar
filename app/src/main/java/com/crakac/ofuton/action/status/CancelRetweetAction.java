package com.crakac.ofuton.action.status;

import android.content.Context;
import android.util.Log;

import com.crakac.ofuton.R;
import com.crakac.ofuton.adapter.TweetStatusAdapter;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class CancelRetweetAction extends ClickAction {
	private twitter4j.Status selectedStatus;
	private static final String TAG = CancelRetweetAction.class.getSimpleName();
	//private TweetStatusAdapter statusAdapter;
	/**
	 *
	 * @param context リソース呼び出し用
	 */
	public CancelRetweetAction(Context context, twitter4j.Status status) {
		super(context, 0, R.drawable.ic_repeat_white_36dp);
		mContext = context;
		selectedStatus = status;
	}

	@Override
	public String getText() {
		return mContext.getString(R.string.unretweet);
	}

	@Override
	public void doAction() {
		ParallelTask<Void, twitter4j.Status> task = new ParallelTask<Void, twitter4j.Status>() {
			@Override
			protected twitter4j.Status doInBackground() {
				Twitter mTwitter = TwitterUtils.getTwitterInstance();
				try {
					//自分のツイートを選択したときは，普通に消せる
					if( selectedStatus.getUser().getId() == TwitterUtils.getCurrentAccountId() ){
						return mTwitter.destroyStatus(selectedStatus.getId());
					}
					//リツイート元のツイートを選択したときは消すのがヒッジョーに面倒．
					else {
						long targetId = -1;
						//getRetweets(そのツイートをリツイートした人のステータスが100件返ってくる．）その中に自分のやつが含まれてればそれを使ってリツイートを取り消す
						for(twitter4j.Status status : mTwitter.getRetweets(selectedStatus.getId())){
							if(status.getUser().getId() == TwitterUtils.getCurrentAccountId() ){
								targetId = status.getId();
								Log.d(TAG, "hit in getRetweet. targetId:" + targetId);
								return mTwitter.destroyStatus(targetId);
							}
						}
						//getRetweetsで拾えなかった場合は，自分のツイートの中から探す．
						int page = 1;
						while(targetId < 0 && page <= 10){
							for(twitter4j.Status status : mTwitter.getUserTimeline(new Paging(page, 50))){
								Log.d(TAG, "statusId:" + status.getId());
								if( status.isRetweet() && status.getRetweetedStatus().getId() == selectedStatus.getId()){
									targetId = status.getId();
									break;
								}
							}
							page++;
						}
						Log.d(TAG, "getUserTimeline. targetId:" + targetId);
						if(targetId < 0)
							return null;
						return mTwitter.destroyStatus(targetId);
					}
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(twitter4j.Status result) {
				if (result != null) {
					AppUtil.showToast("リツイートを取り消しました");
					/*
					int pos = statusAdapter.getPosition(selectedStatus);
					statusAdapter.remove(selectedStatus);
					statusAdapter.insert(result.getRetweetedStatus(), pos);
					statusAdapter.notifyDataSetChanged();
					//*/
				} else {
					AppUtil.showToast(mContext.getString(R.string.something_wrong));
				}
			}
		};
		task.executeParallel();
	}
}
