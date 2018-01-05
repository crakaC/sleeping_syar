package com.crakac.ofuton.fragment.timeline;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crakac.ofuton.C;
import com.crakac.ofuton.fragment.AbstractStatusFragment;
import com.crakac.ofuton.util.ParallelTask;

import java.util.List;
import java.util.ListIterator;

public abstract class AbstractTimelineFragment extends AbstractStatusFragment {

    protected long mSinceId, mMaxId;// ツイートを取得するときに使う．
    int mCount = 50;
    private ParallelTask<Void, List<twitter4j.Status>> mFetchStatusTask;
    private static final String TAG = AbstractTimelineFragment.class.getSimpleName();
    protected long mUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);// BaseStatusActionFragment内でTwitterInstanceを生成する
        mUserId = getArguments().getLong(C.USER_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    public void onRefresh() {
        loadNewTweets();
    }

    @Override
    public void onBottomOfLastItemShown() {
        loadPreviousTweets();
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

    private void initId(){
        mSinceId = 1L;
        mMaxId = Long.MAX_VALUE;
    }

    protected void initTimeline() {
        if (mFetchStatusTask != null && mFetchStatusTask.getStatus() == AsyncTask.Status.RUNNING) {
            setEmptyViewLoading();
            return;
        }
        if (!mAdapter.isEmpty()) {
            return;
        }
        initId();
        mFetchStatusTask = new FetchStatusTask();
        mFetchStatusTask.executeParallel();
    }

    private void loadNewTweets() {
        // 読み込み中なら何もしない
        if (isRunning(mFetchStatusTask)) {
            setEmptyViewLoading();
            return;
        }
        // initTaskが走っておらず，中身がないときはinitTaskを呼ぶ
        if (mAdapter.isEmpty()) {
            setSwipeRefreshEnable(true);
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

    class FetchStatusTask extends ParallelTask<Void, List<twitter4j.Status>> {
        @Override
        protected void onPreExecute() {
            setEmptyViewLoading();
            setFooterViewLoading();// StreamingAPI使用時はFooterViewが見えてしまうので．
        }

        @Override
        protected List<twitter4j.Status> doInBackground() {
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

                if (!result.isEmpty()) {
                    mMaxId = Math.min(mMaxId, result.listIterator(result.size()).previous().getId());
                    mSinceId = Math.max(mSinceId, result.iterator().next().getId());
                }

                if(result.size() < 10) {
                    // 10以下の場合はおそらくもう読み込めないのでFooterを消す。
                    removeFooterView();
                    mListView.setOnLastItemVisibleListener(null);
                }
            } else {
                failToGetStatuses();
            }
            updateDisplayedTime();
            setEmptyViewStandby();
            setFooterViewStandby();// StreamingAPI使用時はFooterViewが見えるので
        }

        @Override
        protected void onCancelled() {
            setEmptyViewStandby();
            setFooterViewStandby();
        }
    }

    ;

    class FetchNewStatusTask extends ParallelTask<Void, List<twitter4j.Status>> {
        @Override
        protected List<twitter4j.Status> doInBackground() {
            return newStatuses(mSinceId, 200);
        }

        @Override
        protected void onPostExecute(List<twitter4j.Status> result) {
            if (result != null) {
                savePosition();
                for (ListIterator<twitter4j.Status> ite = result.listIterator(result.size()); ite.hasPrevious(); ) {
                    twitter4j.Status status = ite.previous();
                    if (mAdapter.getPosition(status) < 0) {
                        mAdapter.insert(status, 0);
                    }
                }
                if (result.size() > 0) {
                    mSinceId = result.get(0).getId();
                    mAdapter.notifyDataSetChanged();
                }
                restorePosition();
            } else {
                failToGetStatuses();
            }
            updateDisplayedTime();
            setSwipeWidgetRefreshing(false);
        }

        @Override
        protected void onCancelled() {
            setSwipeWidgetRefreshing(false);
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

    protected void stopTask(){
        terminateTask(mFetchStatusTask);
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
     */
    protected void failToGetStatuses() {
    }

    /**
     * FragmentPagerAdapterに渡してタイトルを表示するためのやつ
     *
     * @return
     */
    public abstract String getTimelineName();

    public void updateDisplayedTime() {
        SparseArray<View> visibleItems = getVisibleItems();
        for (int i = 0; i < visibleItems.size(); i++) {
            int position = visibleItems.keyAt(i);
            View v = visibleItems.get(position);
            mAdapter.updateDisplayTime(position, v);
        }
    }

    private SparseArray<View> getVisibleItems() {
        SparseArray<View> views = new SparseArray<>();
        try {
            int head = mListView.getFirstVisiblePosition();
            int tail = mListView.getLastVisiblePosition();
            for (int i = head; i <= tail; i++) {
                views.append(i, mListView.getChildAt(i - head));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return views;
    }

    public void refresh(){
        setSwipeWidgetRefreshing(true);
        loadNewTweets();
    }
}