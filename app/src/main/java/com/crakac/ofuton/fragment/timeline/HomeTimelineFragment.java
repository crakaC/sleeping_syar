package com.crakac.ofuton.fragment.timeline;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.MainActivity;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.PrefUtil;
import com.crakac.ofuton.util.TwitterUtils;

import java.util.List;

import twitter4j.ConnectionLifeCycleListener;
import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.UserStreamAdapter;

public class HomeTimelineFragment extends AbstractTimelineFragment implements ConnectionLifeCycleListener {
    public static final String TITLE = "Home";

    // UserStream
    private TwitterStream mTwitterStream;
    private boolean mIsStreaming = false;

    private boolean mIsOverflowing = false;

    static class ConnectStreamingRunnable implements Runnable {
        private TwitterStream stream;

        ConnectStreamingRunnable(TwitterStream stream) {
            this.stream = stream;
        }

        @Override
        public void run() {
            stream.user();
        }
    }

    static class DisconnectStreamingRunnable implements Runnable {
        private TwitterStream stream;

        DisconnectStreamingRunnable(TwitterStream stream) {
            this.stream = stream;
        }

        @Override
        public void run() {
            stream.shutdown();
        }
    }

    @Override
    public void onConnect() {
        AppUtil.showToast(R.string.connected);
    }

    @Override
    public void onDisconnect() {
        AppUtil.showToast(R.string.disconnected);
    }

    @Override
    public void onCleanUp() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTwitterStream = TwitterUtils.getTwitterStreamInstance();
        mTwitterStream.addListener(new StreamListener());
        mTwitterStream.addConnectionLifeCycleListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mListView.setOnTouchListener((view, event) -> {
            mIsOverflowing = false;
            return false;
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (PrefUtil.getBoolean(R.string.streaming_mode)) {
            setSwipeRefreshEnable(false);
            if (!mIsStreaming) {
                new Thread(new ConnectStreamingRunnable(mTwitterStream)).start();
                mIsStreaming = true;
            }
        } else {
            setSwipeRefreshEnable(true);
            if (mIsStreaming) {
                new Thread(new DisconnectStreamingRunnable(mTwitterStream)).start();
                mIsStreaming = false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        restorePosition();
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsOverflowing = false;
        savePosition();
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
        return TITLE;
    }

    /* streaming api */
    public class StreamListener extends UserStreamAdapter {
        private Handler mHandler;

        public StreamListener() {
            mHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onStatus(final Status status) {
            if (mAdapter.getPosition(status) >= 0) return;
            mSinceId = status.getId();
            mHandler.post(() -> {
                insertQuietly(status);
                if (isFirstItemVisible() && isCurrentTab() && isResumed()) {
                    mListView.smoothScrollToPosition(0);
                    mIsOverflowing = true;
                } else if (mIsOverflowing) {
                    mListView.smoothScrollToPosition(0);
                }
                if (PrefUtil.getBoolean(R.string.notification)) {
                    if (PrefUtil.getBoolean(R.string.reply_notification)) {
                        Status st = (status.isRetweet()) ? status.getRetweetedStatus() : status;
                        for (UserMentionEntity ue : st.getUserMentionEntities()) {
                            if (ue.getId() == TwitterUtils.getCurrentAccountId()) {
                                AppUtil.showStatus(st);
                                break;
                            }
                        }
                    }
                    if (PrefUtil.getBoolean(R.string.rt_notification)) {
                        if (status.isRetweet()
                                && status.getRetweetedStatus().getUser().getId() == TwitterUtils
                                .getCurrentAccountId()) {
                            AppUtil.showStatus(status);
                        }
                    }
                    updateDisplayedTime();
                }
            });
        }

        @Override
        public void onFollow(final User source, User followedUser) {
            if (PrefUtil.getBoolean(R.string.notification)
                    && PrefUtil.getBoolean(R.string.new_follower_notification)) {
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
            if (PrefUtil.getBoolean(R.string.notification)
                    && PrefUtil.getBoolean(R.string.fav_notification)) {
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
            if (PrefUtil.getBoolean(R.string.notification)
                    && PrefUtil.getBoolean(R.string.fav_notification)) {
                if (target.getId() == TwitterUtils.getCurrentAccountId()
                        && source.getId() != TwitterUtils.getCurrentAccountId()) {
                    mHandler.post(() ->
                            AppUtil.showToast(source.getName() + " (@" + source.getScreenName() + ")が\n"
                                    + unfavoritedStatus.getText() + "\nをお気に入りにから削除しました"));
                }
            }
        }

        @Override
        public void onDirectMessage(final DirectMessage directMessage) {
            if (PrefUtil.getBoolean(R.string.notification)
                    && PrefUtil.getBoolean(R.string.dm_notification)) {
                mHandler.post(() ->
                        AppUtil.showToast("@" + directMessage.getSenderScreenName() + "からダイレクトメッセージが届きました")
                );
            }
        }
    }

    private boolean isCurrentTab() {
        if (!isAdded()) return false;
        MainActivity activity = (MainActivity) getActivity();
        return activity.isCurrentTab(MainActivity.TAB_ID_HOME);
    }

    @Override
    public void refresh() {
        if (!mIsStreaming) {
            super.refresh();
        }
    }
}