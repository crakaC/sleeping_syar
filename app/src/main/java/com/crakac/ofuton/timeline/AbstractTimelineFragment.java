package com.crakac.ofuton.timeline;

import java.util.List;
import java.util.ListIterator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.util.ParallelTask;

public abstract class AbstractTimelineFragment extends AbstractStatusFragment {

    protected long mSinceId = Long.MIN_VALUE, mMaxId = Long.MAX_VALUE;// ツイートを取得するときに使う．
    int mCount = 50;
    private ParallelTask<Void, Void, List<twitter4j.Status>> mFetchStatusTask;
    private static final String TAG = AbstractTimelineFragment.class.getSimpleName();
    protected long mUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);// BaseStatusActionFragment内でTwitterInstanceを生成する
        setRetainInstance(true);// インスタンスを保持　画面が回転しても取得したTweetが消えなくなる
        mUserId = getArguments().getLong(C.USER_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, getTimelineName() + " onCreateView");
        return view;
    }

    @Override
    public void onRefresh() {
        loadNewTweets();
    }

    @Override
    protected void onClickFooterView() {
        loadPreviousTweets();
    }

    @Override
    protected void onClickEmptyView() {
        initTimeline();
    }

    @Override
    public void onResume() {
        initTimeline();
        super.onResume();
    }

    private void initTimeline() {
        if (mFetchStatusTask != null && mFetchStatusTask.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d(TAG + getTimelineName(), ":initTask is already running.");
            setEmptyViewLoading();
            return;
        }
        if (!mAdapter.isEmpty()) {
            Log.d(TAG + getTimelineName(), ":mAdapter has items.");
            return;
        }
        mFetchStatusTask = new FetchStatusTask();
        mFetchStatusTask.executeParallel();
    }

    public void loadNewTweets() {
        // 読み込み中なら何もしない
        if (isRunning(mFetchStatusTask)) {
            Log.d(TAG + getTimelineName(), "cannot loadNewTweets(): task is running.");
            setEmptyViewLoading();
            return;
        }
        // initTaskが走っておらず，中身がないときはinitTaskを呼ぶ
        if (mAdapter.isEmpty()) {
            mSwipeWidget.setRefreshing(true);
            Log.d(TAG + getTimelineName(), ":initTask hasn't run.");
            initTimeline();
            return;
        }
        mFetchStatusTask = new FetchNewStatusTask();
        mFetchStatusTask.executeParallel();
    }

    private void loadPreviousTweets() {
        if (isRunning(mFetchStatusTask)) {
            return;
        }
        mFetchStatusTask = new FetchStatusTask();
        mFetchStatusTask.executeParallel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        terminateTask(mFetchStatusTask);
    }

    class FetchStatusTask extends ParallelTask<Void, Void, List<twitter4j.Status>> {
        @Override
        protected void onPreExecute() {
            setEmptyViewLoading();
            setFooterViewLoading();// StreamingAPI使用時はFooterViewが見えてしまうので．
        }

        @Override
        protected List<twitter4j.Status> doInBackground(Void... params) {
            return previousStatuses(mMaxId, mCount);
        }

        @Override
        protected void onPostExecute(List<twitter4j.Status> result) {
            if (result != null) {
                for (twitter4j.Status status : result) {
                    // StreamingAPI使用時は重複して取得する可能性があるのでこうする．
                    if (mAdapter.getPosition(status) < 0) {
                        mAdapter.add(status);
                    }
                }
                if (result.size() > 0) {
                    mMaxId = Math.min(mMaxId, result.listIterator(result.size()).previous().getId());
                    mSinceId = Math.max(mSinceId, result.iterator().next().getId());
                } else {
                    removeFooterView();
                    //mPtrListView.setOnLastItemVisibleListener(null);
                }
            } else {
                failToGetStatuses();
                Log.d(TAG + getTimelineName(), "fail to get Tilmeline");
            }
            updateDisplayedTime();
            setEmptyViewStandby();
            setFooterViewStandby();// StreamingAPI使用時はFooterViewが見えるので
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG + getTimelineName(), "cancel initTask");
            setEmptyViewStandby();
            setFooterViewStandby();
        }
    };

    class FetchNewStatusTask extends ParallelTask<Void, Void, List<twitter4j.Status>> {
        @Override
        protected List<twitter4j.Status> doInBackground(Void... params) {
            return newStatuses(mSinceId, 200);
        }

        @Override
        protected void onPostExecute(List<twitter4j.Status> result) {
            if (result != null) {
                int lastPos = mListView.getFirstVisiblePosition();// 新しいstatus追加前の一番上のポジションを保持
                for (ListIterator<twitter4j.Status> ite = result.listIterator(result.size()); ite.hasPrevious();) {
                    twitter4j.Status status = ite.previous();
                    if (mAdapter.getPosition(status) < 0) {
                        mAdapter.insert(status, 0);
                    }
                }
                if (result.size() > 0) {
                    mSinceId = result.iterator().next().getId();
                    mListView.setSelection(lastPos + result.size());// 追加した分ずらす
                }
            } else {
                failToGetStatuses();
                Log.d(TAG + getTimelineName(), "fail to get Tilmeline");
            }
            updateDisplayedTime();
            mSwipeWidget.setRefreshing(false);
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG + getTimelineName(), "cancel loadNewTask");
            mSwipeWidget.setRefreshing(false);
        }
    }

    private boolean isRunning(AsyncTask<?, ?, ?> task) {
        return task != null && task.getStatus() == AsyncTask.Status.RUNNING;
    }

    private void terminateTask(AsyncTask<?, ?, ?>... tasks) {
        for (AsyncTask<?, ?, ?> task : tasks) {
            if (isRunning(task)) {
                task.cancel(true);
            }
        }
    }

    /**
     * 更新するときに呼ぶやつ
     *
     * @param sinceId
     * @param count
     * @return
     */
    protected abstract List<twitter4j.Status> newStatuses(long sinceId, int count);

    /**
     * 古いツイートを取得するときに呼ぶ奴
     *
     * @param maxId
     * @param count
     * @return
     */
    protected abstract List<twitter4j.Status> previousStatuses(long maxId, int count);

    /**
     * 取得に失敗した時によぶやつ
     *
     */
    protected abstract void failToGetStatuses();

    /**
     * FragmentPagerAdapterに渡してタイトルを表示するためのやつ
     *
     * @return
     */
    public abstract String getTimelineName();

    public void updateDisplayedTime() {
        SparseArray<View> visibleItems = getVisibleItems();
        for(int i = 0; i < visibleItems.size(); i++){
            int position = visibleItems.keyAt(i);
            View v = visibleItems.get(position);
            mAdapter.updateDisplayTime(position, v);
        }
    }

    public void getViews(){
        SparseArray<View> visibleItems = getVisibleItems();
        for(int i = 0; i < visibleItems.size(); i++){
            int position = visibleItems.keyAt(i);
            View v = visibleItems.get(position);
            mAdapter.getView(position, v, null);
        }
    }

    private SparseArray<View> getVisibleItems(){
        SparseArray<View> views = new SparseArray<View>();
        try {
            int head = mListView.getFirstVisiblePosition();
            int tail = mListView.getLastVisiblePosition();
            int bias;
            if(head > 0){
                head--;
                bias = 0;
            } else {
                bias = 1;
            }
            tail = Math.min(tail, mListView.getCount());
            for (int i = head; i < tail; i++) {
                views.append(i, mListView.getChildAt(i - head + bias));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return views;
    }
}