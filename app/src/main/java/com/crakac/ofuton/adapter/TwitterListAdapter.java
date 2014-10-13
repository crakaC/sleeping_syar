package com.crakac.ofuton.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.TwitterList;
import com.crakac.ofuton.util.TwitterUtils;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterListAdapter extends ArrayAdapter<TwitterList> {
	private LayoutInflater mInflater;
	private long mUserId = -1;
	private ExecutorService mExecutor;
	private Handler mHandler;

	public TwitterListAdapter(Context context, long userId) {
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		this.mUserId = userId;
		mExecutor = Executors.newFixedThreadPool(3);
		mHandler = new Handler();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//レイアウトを使いまわすとチェックマークとprogressbarの挙動が怪しくなるので毎回inflateする．
		convertView = mInflater.inflate(R.layout.list_dialog_item, parent,
					false);
		TwitterList item = getItem(position);
		ImageView check = (ImageView) convertView.findViewById(R.id.checkMark);
		check.setVisibility(View.INVISIBLE);
		ProgressBar pBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
		pBar.setVisibility(View.VISIBLE);
		mExecutor.execute(new ListCheckTask(check, pBar, item));
		TextView listName = (TextView) convertView.findViewById(R.id.listName);
		listName.setText(item.getName());
		return convertView;
	}

	/**
	 * リストにユーザーが登録されているかチェックする
	 *
	 * @author Kosuke
	 *
	 */
	private class ListCheckTask implements Runnable {
		private ProgressBar pBar;
		private ImageView checkMark;
		private TwitterList tList;

		public ListCheckTask(ImageView iv, ProgressBar pb, TwitterList list) {
			checkMark = iv;
			pBar = pb;
			tList = list;
		}

		@Override
		public void run() {
			Boolean existsUser = ListCache.showMemberShip(tList.getListId(),
					mUserId);
			if (existsUser == null) {
				// userIdを，リストに登録されているユーザーから探す．リストに含まれている人数が多いと時間がかかる．
				Twitter mTwitter = TwitterUtils.getTwitterInstance();
				long cursor = -1;
				PagableResponseList<twitter4j.User> users;
				existsUser = false;
				try {
					while (true) {
						users = mTwitter.getUserListMembers(tList.getListId(),cursor);
						for (twitter4j.User user : users) {
							if (user.getId() == mUserId) {
								existsUser = true;
								break;
							}
						}
						if (existsUser || !users.hasNext()) {
							break;
						}
						cursor = users.getNextCursor();
					}
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				//結果をキャッシュに保存
				ListCache.setMemberShip(new Pair<>(tList.getListId(), mUserId), existsUser);
			}
			if (existsUser != null) {
				setCheckMarkEnableAsync(existsUser, pBar, checkMark);
			}
		}
	}

	/**
	 * ユーザーがリストに含まれているかをキャッシュして通信を減らす
	 * @author Kosuke
	 *
	 */
	private static class ListCache {
		static HashMap<Pair<Long, Long>, Boolean> cache = new HashMap<Pair<Long, Long>, Boolean>();

		public static Boolean showMemberShip(long listId, long userId) {
			Pair<Long, Long> key = new Pair<Long, Long>(listId, userId);
			if (cache.containsKey(key)) {
				return cache.get(key);
			} else {
				return null;
			}
		}

		public static void setMemberShip(Pair<Long, Long> pair,
				boolean existsUser) {
			cache.put(pair, existsUser);
		}
	}

	private void setCheckMarkEnableAsync(final boolean flag, final ProgressBar bar, final ImageView iv) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				bar.setVisibility(View.INVISIBLE);
				if(flag){
					iv.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	public void addUserToList(long listId, long userId) {
		ListCache.setMemberShip(new Pair<>(listId, userId), true);
	}

	public void removeUserFromList(long listId, long userId) {
		ListCache.setMemberShip(new Pair<>(listId, userId), false);
	}

	public void release() {
		mExecutor.shutdown();
	}
}