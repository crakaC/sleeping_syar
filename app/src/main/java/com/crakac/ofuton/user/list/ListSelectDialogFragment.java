package com.crakac.ofuton.user.list;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.crakac.ofuton.R;
import com.crakac.ofuton.timeline.AbstractStatusFragment;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterList;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

@SuppressLint("ValidFragment")
public class ListSelectDialogFragment extends DialogFragment {
	private static final String TAG = ListSelectDialogFragment.class.getSimpleName();
	private Twitter mTwitter;
	private TwitterListAdapter mAdapter;
	private Dialog dialog;
	private long userId;
	private ListView listView;
	//private View footerView;

	public ListSelectDialogFragment() {
	}

	public ListSelectDialogFragment(AbstractStatusFragment targetFragment) {
		setTargetFragment((Fragment) targetFragment, 0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.list_dialog, container, false);
		//footerView = inflater.inflate(R.layout.list_item_empty, null);
		mTwitter = TwitterUtils.getTwitterInstance();
		userId = getArguments().getLong("userId");
		listView = (ListView)view.findViewById(R.id.lists);
		//listView.addFooterView(footerView);
		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			TwitterList list;
			ImageView checkMark;
			ProgressBar pBar;
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				ListView lv = (ListView)parent;
				list = (TwitterList)lv.getItemAtPosition(pos);
				checkMark = (ImageView)view.findViewById(R.id.checkMark);
				pBar = (ProgressBar)view.findViewById(R.id.progressBar);
				if(!pBar.isShown()){
					if(checkMark.isShown()){
						removeList(list.getListId(), pBar, checkMark);
					} else {
						addList(list.getListId(), pBar, checkMark);
					}
				}
			}
			private void addList(long listId, final ProgressBar pBar, final ImageView check) {
				pBar.setVisibility(View.VISIBLE);
				ParallelTask<Long, Void, UserList> addTask = new ParallelTask<Long, Void, UserList>() {
					@Override
					protected UserList doInBackground(Long... params) {
						try {
							return mTwitter.createUserListMember(params[0], userId);
						} catch (TwitterException e) {
							e.printStackTrace();
						}
						return null;
					}
					@Override
					protected void onPostExecute(UserList result) {
						pBar.setVisibility(View.GONE);
						if(result != null){
							check.setVisibility(View.VISIBLE);
							mAdapter.addUserToList(result.getId(), userId);
						} else {
							AppUtil.showToast(getString(R.string.something_wrong));
						}
					}
				};
				addTask.executeParallel(listId);
			}

			private void removeList(long listId, final ProgressBar pBar, final ImageView check) {
				//プログレスバーを表示
				check.setVisibility(View.INVISIBLE);
				pBar.setVisibility(View.VISIBLE);
				ParallelTask<Long, Void, UserList> removeTask = new ParallelTask<Long, Void, UserList>() {
					@Override
					protected UserList doInBackground(Long... params) {
						try {
							return mTwitter.destroyUserListMember(params[0], userId);
						} catch (TwitterException e) {
							e.printStackTrace();
						}
						return null;
					}
					@Override
					protected void onPostExecute(UserList result) {
						if(result!=null){
							mAdapter.removeUserFromList(result.getId(), userId);
						} else {
							check.setVisibility(View.VISIBLE);
							AppUtil.showToast(getString(R.string.something_wrong));
						}
						pBar.setVisibility(View.GONE);
					}
				};
				removeTask.executeParallel(listId);
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		dialog = getDialog();

		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();

		DisplayMetrics metrics = getResources().getDisplayMetrics();

		// 縦幅はwrap contentで，横幅は92%で．
		int dialogWidth = (int) (metrics.widthPixels * 0.92);
		int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;

		lp.width = dialogWidth;
		lp.height = dialogHeight;
		dialog.getWindow().setAttributes(lp);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dialog = new Dialog(getActivity());
		// タイトル部分を消す．消さないとダイアログの表示位置が下にずれる
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		// レイアウトはonCreateViewで作られる．ので，dialog.setContentViewはいらない

		// 全画面化
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

		// 背景を透明に
		dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));

		return dialog;
	}

	@Override
	public void onDestroy() {
		mAdapter.release();//ユーザーがリストに追加されているかをチェックするために作成したスレッドを消す．
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
	}

	public void setAdapter(TwitterListAdapter adapter){
		mAdapter = adapter;
	}
}