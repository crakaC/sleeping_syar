package com.crakac.ofuton.action.status;

import android.content.Context;

import com.crakac.ofuton.R;
import com.crakac.ofuton.adapter.TweetStatusAdapter;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class CancelRetweetAction extends ClickAction {
    private twitter4j.Status selectedStatus;
    private static final String TAG = CancelRetweetAction.class.getSimpleName();
    //private TweetStatusAdapter statusAdapter;

    /**
     * @param context リソース呼び出し用
     */
    public CancelRetweetAction(Context context, twitter4j.Status status) {
        super(context, 0, R.drawable.ic_repeat);
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
            Twitter mTwitter = TwitterUtils.getTwitterInstance();
            private twitter4j.Status response;
            @Override
            protected twitter4j.Status doInBackground() {

                try {
                    response = mTwitter.unRetweetStatus(selectedStatus.getId());
                    return mTwitter.showStatus(selectedStatus.getId());
                } catch (TwitterException e) {
                    if (e.getErrorCode() != 404) {
                        e.printStackTrace();
                    }

                }
                return null;
            }

            @Override
            protected void onPostExecute(twitter4j.Status result) {
                if (response == null) {
                    AppUtil.showToast(mContext.getString(R.string.something_wrong));
                    return;
                }
                AppUtil.showToast("リツイートを取り消しました");
                if (TwitterUtils.isMyTweet(selectedStatus)) {
                    TweetStatusAdapter.removeItem(selectedStatus);
                } else if(selectedStatus.isRetweet() && result.isRetweet()){
                    TweetStatusAdapter.updateItem(selectedStatus, result);
                    TweetStatusAdapter.updateItem(selectedStatus.getRetweetedStatus(), result.getRetweetedStatus());
                }
            }
        };
        task.executeParallel();
    }
}
