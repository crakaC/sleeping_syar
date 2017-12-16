package com.crakac.ofuton.fragment.dm;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.activity.DmActivity;
import com.crakac.ofuton.adapter.DmAdapter;
import com.crakac.ofuton.fragment.AbstractPtrFragment;
import com.crakac.ofuton.fragment.dialog.DmDialogFragment;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;

import java.util.List;
import java.util.ListIterator;

import twitter4j.DirectMessage;
import twitter4j.Twitter;

abstract public class AbstractDmFragment extends AbstractPtrFragment {
	private long mSinceId = Long.MIN_VALUE, mMaxId = Long.MAX_VALUE;
	int mCount = 20;
	private DmAdapter mAdapter;
	protected Twitter mTwitter;
	private ParallelTask<Void, List<DirectMessage>> fetchNewTask,
			fetchPreviousTask;
	private static final String TAG = DmActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(mTwitter == null){
			mTwitter = TwitterUtils.getTwitterInstance();
		}
		if(mAdapter == null){
			mAdapter = new DmAdapter(getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		//中身のListView
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ListView lv = (ListView) parent;
				DirectMessage item = (DirectMessage) lv
						.getItemAtPosition(position);
				DmDialogFragment dialog = new DmDialogFragment();
				Bundle b = new Bundle();
				b.putSerializable(C.DM, item);
				dialog.setArguments(b);
				dialog.show(getFragmentManager(), "status dialog");
			}
		});
		initDMs();
		return view;
	}

	@Override
	protected void onClickEmptyView() {
		initDMs();
	}

	@Override
	protected void onClickFooterView() {
		loadPreviousMessages();
	}

	@Override
	public void onRefresh() {
		loadNewMessages();
	}

    @Override
    public void onBottomOfLastItemShown() {
        loadPreviousMessages();
    }

    private void initDMs() {
		if (fetchPreviousTask != null
				&& fetchPreviousTask.getStatus() == AsyncTask.Status.RUNNING) {
			Log.d(TAG , ":initTask is already running.");
			return;
		}
		if (mAdapter.getCount() > 0) {
			Log.d(TAG , ":mAdapter has items.");
			return;
		}
		fetchPreviousTask = new FetchDirectMessageTask();
		fetchPreviousTask.executeParallel();
	}

	private void loadNewMessages() {
		if (mAdapter.isEmpty()){
			initDMs();
			return;
		}
		if (isRunning(fetchNewTask)) {
			return;
		}
		fetchNewTask = new FetchNewMessageTask();
		fetchNewTask.executeParallel();
	}

	private void loadPreviousMessages() {
		if (fetchPreviousTask != null
				&& fetchPreviousTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}
		fetchPreviousTask = new FetchDirectMessageTask();
		fetchPreviousTask.executeParallel();
	}


	private void failToGetMessages(){
		AppUtil.showToast("DMの取得に失敗しました");
	}

	@Override
	public void onPause() {
		super.onPause();
		terminateTask(fetchPreviousTask, fetchNewTask);
	}
	private void terminateTask(AsyncTask<?, ?, ?>... tasks) {
		for(AsyncTask<?, ?, ?>task : tasks){
			if( isRunning(task) ){
				task.cancel(true);
			}
		}
	}

	private boolean isRunning(AsyncTask<?,?,?> task){
		return task != null && task.getStatus() == AsyncTask.Status.RUNNING;
	}

	private class FetchDirectMessageTask extends ParallelTask<Void, List<DirectMessage>>{
		@Override
		protected void onPreExecute() {
			setEmptyViewLoading();
			setFooterViewLoading();
		}

		@Override
		protected List<DirectMessage> doInBackground() {
			return fetchMessages(mMaxId, mCount);
		}

		@Override
		protected void onPostExecute(List<DirectMessage> result) {
			if (result != null) {
				for (DirectMessage status : result) {
					mAdapter.add(status);
				}
				if (result.size() > 0) {
					mMaxId = Math.min(result.listIterator(result.size()).previous()
							.getId(), mMaxId);
					mSinceId = Math.max(result.iterator().next().getId(), mSinceId);
				} else {
					removeFooterView();
				}
			} else {
				Log.d(TAG,"fail to get DM");
				failToGetMessages();
			}
			setEmptyViewStandby();
			setFooterViewStandby();
		}
		@Override
		protected void onCancelled() {
			super.onCancelled();
			Log.d(TAG,"cancel initTask");
			setEmptyViewStandby();
		}
	}

	private class FetchNewMessageTask extends ParallelTask<Void, List<DirectMessage>> {

		@Override
		protected List<DirectMessage> doInBackground() {
			return newMessages(mSinceId, mCount);
		}

		@Override
		protected void onPostExecute(List<DirectMessage> result) {
			if (result != null) {
				int lastPos = mListView.getFirstVisiblePosition();//新しいstatus追加前の一番上のポジションを保持
				for (ListIterator<DirectMessage> ite = result
						.listIterator(result.size()); ite.hasPrevious();) {
					mAdapter.insert(ite.previous(), 0);
				}
				if (result.size() > 0) {
					mSinceId = result.iterator().next().getId();
					mListView.setSelection(lastPos + result.size());//追加した分ずらす
				}
			} else {
				Log.d(TAG,"fail to get Tilmeline");
				failToGetMessages();
			}
            setSwipeWidgetRefreshing(false);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			Log.d(TAG,"cancel loadNewTask");
            setSwipeWidgetRefreshing(false);
		}
	}

	abstract protected List<DirectMessage> newMessages(long sinceId, int counts);
	abstract protected List<DirectMessage> fetchMessages(long maxId, int counts);

}
