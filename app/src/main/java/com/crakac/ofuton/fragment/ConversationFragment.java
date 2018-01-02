package com.crakac.ofuton.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.StatusPool;

import twitter4j.Status;
import twitter4j.TwitterException;

public class ConversationFragment extends AbstractStatusFragment {

    private long mReplyToStatusId = -1l;// ツイートを取得するときに使う．
    private LoadConversationTask mLoadConversationTask;
    private static final String TAG = ConversationFragment.class.getSimpleName();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSwipeRefreshEnable(false);
        if (shouldLoad()) {
            initConversation();
        } else {
            removeFooterView();
        }
    }

    @Override
    protected void onClickFooterView() {
        loadPrevious(mReplyToStatusId);
    }

    @Override
    public void onLastItemVisible() {
        loadPrevious(mReplyToStatusId);
    }

    @Override
    /**
     * onStop時に会話の読み込みを止める
     */
    public void onStop() {
        super.onStop();
        if (isRunning(mLoadConversationTask)) {
            mLoadConversationTask.cancel(true);
            mLoadConversationTask = null;
        }
    }

    private void initConversation() {
        if (!mAdapter.isEmpty()) {
            return;
        }
        Status firstStatus = (Status) getArguments().getSerializable(C.STATUS);
        mAdapter.insert(firstStatus, 0);
        mReplyToStatusId = firstStatus.getInReplyToStatusId();
        if(mReplyToStatusId <= 0) {
            removeFooterView();
        } else {
            loadPrevious(mReplyToStatusId);
        }
    }

    private void onFetchStatus(Status status) {
        if (status != null) {
            mAdapter.add(status);
            mReplyToStatusId = status.getInReplyToStatusId();
            Log.d(TAG, "nextId:" + mReplyToStatusId);

            if (mReplyToStatusId > 0) {
                if (mFooterView.isShown()) {
                    mLoadConversationTask = null;
                    loadPrevious(mReplyToStatusId);
                } else {
                    setFooterViewStandby();
                }
            } else {
                removeFooterView();
                mListView.setOnLastItemVisibleListener(null);
                Log.d(TAG, "shown all conversation");
            }
        } else {
            if (isAdded()) {
                AppUtil.showToast(R.string.something_wrong);
            }
            setFooterViewStandby();
        }
    }

    private void loadPrevious(long nextId) {
        if (!shouldLoad()) return;
        if (isRunning(mLoadConversationTask)) {
            Log.d(TAG, "loadPrevious() return : loadTask is running.");
            return;
        }
        setFooterViewLoading();
        mLoadConversationTask = new LoadConversationTask();
        mLoadConversationTask.execute(nextId);
    }

    private boolean isRunning(LoadConversationTask task) {
        return (task != null && task.getStatus() == AsyncTask.Status.RUNNING);
    }

    private boolean shouldLoad() {
        return (mAdapter.isEmpty() || mAdapter.getItem(mAdapter.getCount() - 1).getInReplyToStatusId() > 0);
    }

    private class LoadConversationTask extends AsyncTask<Long, Void, Status> {

        @Override
        protected twitter4j.Status doInBackground(Long... params) {
            return nextTweet(params[0]);
        }

        @Override
        protected void onPostExecute(twitter4j.Status result) {
            ConversationFragment.this.onFetchStatus(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d(TAG, "onCancelled()");
            setFooterViewStandby();
        }

        private twitter4j.Status nextTweet(long id) {
            try {
                twitter4j.Status status = StatusPool.get(id);
                if (status != null) {
                    try {
                        Thread.sleep(200); // 読み込みが速すぎると画面いっぱいになる前に止まるのでちょっとスリープ
                    } catch (InterruptedException noNeedHandlingError) {
                    }
                } else {
                    status = mTwitter.showStatus(id);
                    Log.d("conversation", "load from web");
                }
                return status;
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}