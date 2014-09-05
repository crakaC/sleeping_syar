package com.crakac.ofuton.user;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crakac.ofuton.AbstractPtrFragment;
import com.crakac.ofuton.C;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.User;

public abstract class AbstractUserFragment extends AbstractPtrFragment {

	private UserAdapter mAdapter;
	private ParallelTask<Void, Void, PagableResponseList<User>> mLoadTask;
	private long mCursor;
	private static final String TAG = AbstractUserFragment.class.getSimpleName();
	protected Twitter mTwitter;
	protected User mUser;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTwitter = TwitterUtils.getTwitterInstance();
		mUser = (User) getArguments().getSerializable(C.USER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		if (mAdapter == null) {
			mAdapter = new UserAdapter(getActivity());
		}
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView lv = (ListView) parent;
				User user = (User) lv.getItemAtPosition(position);
				Intent intent = new Intent(getActivity(),
						UserDetailActivity.class);
				intent.putExtra(C.USER, user);
				startActivity(intent);
			}
		});
		initialize();
		return view;
	}

	@Override
	protected void onClickEmptyView() {
		initialize();
	}

	private void initialize() {
        mSwipeWidget.setEnabled(false);
        if (mAdapter.getCount() > 0) {
			Log.d(TAG, "mAdapter already has items.");
			return;
		}
		mCursor = -1;
		loadUser();
	}

    @Override
    public void onBottomOfLastItemShown() {
        loadUser();
    }

    private void loadUser() {
		Log.d(TAG, "loadUser()");
		if (mLoadTask != null
				&& mLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}
		mLoadTask = new FetchUsersTask();
		mLoadTask.executeParallel();
	}

	/**
	 * ユーザーを読み込むやつ
	 *
	 * @param cursor
	 * @return
	 */
	protected abstract PagableResponseList<User> fetchNextUser(
			long cursor);

	/**
	 * 取得に失敗した時によぶやつ
	 */
	protected void failToLoad() {
	}

	class FetchUsersTask extends
			ParallelTask<Void, Void, PagableResponseList<User>> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mAdapter.isEmpty()) {
				setEmptyViewLoading();
			} else {
				setFooterViewLoading();
			}
		}

		@Override
		protected PagableResponseList<User> doInBackground(
				Void... params) {
			return fetchNextUser(mCursor);
		}

		@Override
		protected void onPostExecute(PagableResponseList<User> result) {
			if (result != null) {
				for (User user : result) {
					mAdapter.add(user);
				}

				if (result.hasNext()) {
					mCursor = result.getNextCursor();
				} else {
					removeFooterView();
				}
			} else {
				failToLoad();
			}
			setEmptyViewStandby();
			setFooterViewStandby();
		}
	}
}