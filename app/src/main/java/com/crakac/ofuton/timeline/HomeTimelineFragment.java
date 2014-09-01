package com.crakac.ofuton.timeline;

import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.UserStreamAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterUtils;
public class HomeTimelineFragment extends AbstractTimelineFragment {
    private final String title = "Home";

    // UserStream
    private TwitterStream mTwitterStream;
    private boolean mIsStreaming;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTwitterStream = TwitterUtils.getTwitterStreamInstance();
        mTwitterStream.addListener(new StreamListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (AppUtil.getBooleanPreference(R.string.streaming_mode)) {
            mSwipeWidget.setOnRefreshListener(null);
            mSwipeWidget.setEnabled(false);
            if (!mIsStreaming) {
                mTwitterStream.user();
                mIsStreaming = true;
            }
        } else {
            mSwipeWidget.setOnRefreshListener(this);
            mSwipeWidget.setEnabled(true);
            if (mIsStreaming) {
                mTwitterStream.shutdown();
                mIsStreaming = false;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIsStreaming) {
            mTwitterStream.shutdown();
            mIsStreaming = false;
        }
    }

    @Override
    protected List<Status> newStatuses(long id, int count) {
        try {
            return mTwitter.getHomeTimeline(new Paging().sinceId(id).count(count));
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected List<Status> previousStatuses(long id, int count) {
        try {
            return mTwitter.getHomeTimeline(new Paging().maxId(id - 1l).count(count));
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void failToGetStatuses() {
    }

    @Override
    public String getTimelineName() {
        return title;
    }

    /* streaming api */
    public class StreamListener extends UserStreamAdapter {
        private Handler mHandler;

        public StreamListener() {
            mHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onStatus(final Status status) {
            mSinceId = status.getId();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.insert(status, 0);
                    if (AppUtil.getBooleanPreference(R.string.notification)) {
                        if (AppUtil.getBooleanPreference(R.string.reply_notification)) {
                            Status st = (status.isRetweet()) ? status.getRetweetedStatus() : status;
                            for (UserMentionEntity ue : st.getUserMentionEntities()) {
                                if (ue.getId() == TwitterUtils.getCurrentAccountId()) {
                                    AppUtil.showStatus(st);
                                    break;
                                }
                            }
                        }
                        if (AppUtil.getBooleanPreference(R.string.rt_notification)) {
                            if (status.isRetweet()
                                    && status.getRetweetedStatus().getUser().getId() == TwitterUtils
                                            .getCurrentAccountId()) {
                                AppUtil.showStatus(status);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onFollow(final User source, User followedUser) {
            if (AppUtil.getBooleanPreference(R.string.notification)
                    && AppUtil.getBooleanPreference(R.string.new_follower_notification)) {
                if (followedUser.getId() == TwitterUtils.getCurrentAccountId()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            AppUtil.showToast(source.getName() + "(@" + source.getScreenName() + ")\nにフォローされました");
                        }
                    });
                }
            }
        }

        @Override
        public void onFavorite(final User source, User target, final Status favoritedStatus) {
            if (AppUtil.getBooleanPreference(R.string.notification)
                    && AppUtil.getBooleanPreference(R.string.fav_notification)) {
                if (target.getId() == TwitterUtils.getCurrentAccountId()
                        && source.getId() != TwitterUtils.getCurrentAccountId()) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            AppUtil.showToast(source.getName() + " (@" + source.getScreenName() + ")が\n"
                                    + favoritedStatus.getText() + "\nをお気に入りに追加しました");
                        }
                    });
                }
            }
        }

        @Override
        public void onUnfavorite(final User source, User target, final Status unfavoritedStatus) {
            if (AppUtil.getBooleanPreference(R.string.notification)
                    && AppUtil.getBooleanPreference(R.string.fav_notification)) {
                if (target.getId() == TwitterUtils.getCurrentAccountId()
                        && source.getId() != TwitterUtils.getCurrentAccountId()) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            AppUtil.showToast(source.getName() + " (@" + source.getScreenName() + ")が\n"
                                    + unfavoritedStatus.getText() + "\nをお気に入りにから削除しました");
                        }
                    });
                }
            }
        }

        @Override
        public void onDirectMessage(final DirectMessage directMessage) {
            if (AppUtil.getBooleanPreference(R.string.notification)
                    && AppUtil.getBooleanPreference(R.string.dm_notification)) {
                mHandler.post(new Runnable() {
                    public void run() {
                        AppUtil.showToast("@" + directMessage.getSenderScreenName() + "からダイレクトメッセージが届きました");
                    }
                });
            }
        }
    }
}