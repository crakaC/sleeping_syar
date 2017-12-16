package com.crakac.ofuton.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.ReloadChecker;
import com.crakac.ofuton.util.TwitterList;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.widget.ProgressTextView;

import java.util.List;
import java.util.TreeSet;

import twitter4j.TwitterException;
import twitter4j.UserList;

public class UsersListFragment extends Fragment{

	private static final String TAG = UsersListFragment.class.getSimpleName();
	private UsersListAdapter mAdapter;
	private TreeSet<Long> mCurrentListIdSet, mInitialListIdSet;
	private ParallelTask<Void, List<UserList>> mLoadListTask;
	private ProgressTextView mEmptyView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
        //表示するリストに変更があったか確かめるために，最初の時点でのリストのID一覧を持ってくる
        mInitialListIdSet = new TreeSet<>();
        for(TwitterList list : TwitterUtils.getListsOfCurrentAccount()){
            mInitialListIdSet.add(list.getListId());
        }
        mCurrentListIdSet = new TreeSet<>(mInitialListIdSet);
        ReloadChecker.reset();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		View convertView = inflater.inflate(R.layout.base_listfragment, null);
		mEmptyView = (ProgressTextView) convertView.findViewById(R.id.emptyView);

		ListView lv = (ListView)convertView.findViewById(R.id.listView);
		lv.setEmptyView(mEmptyView);

		mAdapter = new UsersListAdapter(getActivity(), mCurrentListIdSet);
		lv.setAdapter(mAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			ImageView checkMark;
			ParallelTask<Void, Boolean> addTask, removeTask;
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				TwitterList list = (TwitterList)parent.getItemAtPosition(pos);
				checkMark = (ImageView)view.findViewById(R.id.checkMark);
				if(mCurrentListIdSet.contains(list.getListId())){
					removeList(list);
				} else {
					addList(list);
				}
			}

			private void checkListSelectionDiffs() {
				if(mCurrentListIdSet.size() != mInitialListIdSet.size() || !mCurrentListIdSet.containsAll(mInitialListIdSet)){
					ReloadChecker.requestHardReload(true);
				} else {
					ReloadChecker.requestHardReload(false);
				}
			}

			private void addList(final TwitterList list) {
				if(addTask != null && addTask.getStatus() == AsyncTask.Status.RUNNING){
					return;
				}
				addTask = new ParallelTask<Void, Boolean>() {
					@Override
					protected Boolean doInBackground() {
						return TwitterUtils.addList(list);
					}
					@Override
					protected void onPostExecute(Boolean result) {
						if(result){
							checkMark.setVisibility(View.VISIBLE);
							mCurrentListIdSet.add(list.getListId());
							checkListSelectionDiffs();
						} else {
							AppUtil.showToast(getString(R.string.something_wrong));
						}
					}
				};
				addTask.executeParallel();
			}

			private void removeList(final TwitterList list) {
				if(removeTask != null && removeTask.getStatus() == AsyncTask.Status.RUNNING){
					return;
				}
				removeTask = new ParallelTask<Void, Boolean>() {
					@Override
					protected Boolean doInBackground() {
						return TwitterUtils.removeList(list);
					}
					@Override
					protected void onPostExecute(Boolean result) {
						if(result){
							checkMark.setVisibility(View.INVISIBLE);
							mCurrentListIdSet.remove(Long.valueOf(list.getListId()));
							checkListSelectionDiffs();
						} else {
							AppUtil.showToast(getString(R.string.something_wrong));
						}
					}
				};
				removeTask.executeParallel();
			}
		});
		loadList();
		return convertView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mLoadListTask != null && mLoadListTask.getStatus() == AsyncTask.Status.RUNNING){
			mLoadListTask.cancel(true);
			mLoadListTask = null;
		}
	}

	void loadList(){
		if(mLoadListTask != null && mLoadListTask.getStatus() == AsyncTask.Status.RUNNING){
			return;
		}
		mLoadListTask = new ParallelTask<Void, List<UserList>>(){
		    @Override
		    protected void onPreExecute() {
		        mEmptyView.loading();
		    }
			@Override
			protected List<UserList> doInBackground() {
				try {
					return TwitterUtils.getTwitterInstance().getUserLists(TwitterUtils.getCurrentAccountId());
				} catch (IllegalStateException e) {
					e.printStackTrace();
                    AppUtil.showToast(R.string.impossible);
				} catch (TwitterException e) {
					e.printStackTrace();
					AppUtil.showToast(R.string.something_wrong);
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<UserList> lists) {
				if(lists != null){
					long userId = TwitterUtils.getCurrentAccountId();//リスト選ぶんだから現在のユーザでおｋ
					for(UserList list : lists){
						mAdapter.add(new TwitterList(userId, list.getId(), list.getName(), list.getFullName()));
					}
				} else {
				    mEmptyView.standby();
				}
			}
		};
		mLoadListTask.executeParallel();
	}

	private static class UsersListAdapter extends ArrayAdapter<TwitterList> {
		private LayoutInflater mInflater;
		private TreeSet<Long> selectedListIds;
		private ImageView check;

		UsersListAdapter(Context context, TreeSet<Long> listIds) {
			super(context, android.R.layout.simple_list_item_1);
			mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			selectedListIds = listIds;// 参照型なのでこれでおｋ
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.userlist_listitem,
							parent, false);
			}
			TwitterList item = getItem(position);
			check = (ImageView) convertView.findViewById(R.id.checkMark);
			if (selectedListIds.contains(item.getListId())) {
				check.setVisibility(View.VISIBLE);
			} else {
				check.setVisibility(View.INVISIBLE);
			}
			TextView listName = (TextView) convertView.findViewById(R.id.listName);
			listName.setText(item.getName());
			return convertView;
		}
	}
}