package com.crakac.ofuton.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.AccountActivity;
import com.crakac.ofuton.activity.MainActivity;
import com.crakac.ofuton.activity.UserDetailActivity;
import com.crakac.ofuton.util.Account;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.DialogManager;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.ReloadChecker;
import com.crakac.ofuton.util.TwitterUtils;

import java.util.ArrayList;
import java.util.List;

public class AccountListFragment extends Fragment{

	private static final String TAG = AccountListFragment.class.getSimpleName();
	private AccountAdapter mAdapter;
	private DialogManager mDialogManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView:" + getId());
		mDialogManager = new DialogManager(getActivity().getSupportFragmentManager());
		View view = inflater.inflate(R.layout.base_listfragment, container, false);
		mAdapter = new AccountAdapter(getActivity());
		ListView lv = view.findViewById(R.id.listView);
		View footerView = inflater.inflate(R.layout.account_footer, null);
		footerView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((AccountActivity)AccountListFragment.this.getActivity()).onClickFooter();
			}
		});
		lv.addFooterView(footerView);

		lv.setAdapter(mAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				ListView lv = (ListView)parent;
				Account user = (Account)lv.getItemAtPosition(pos);
				TwitterUtils.setCurrentAccount(user);
				getActivity().finish();
				ReloadChecker.requestHardReload(true);
				startActivity(new Intent(getActivity(), MainActivity.class));
			}
		});
		reloadAcounts();
		return view;
	}

	private class AccountAdapter extends BaseAdapter{
		LayoutInflater mInflater;
		ArrayList<Account> mAccountList;
		AccountAdapter(Context context) {
			mInflater = (LayoutInflater) context
			.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			mAccountList = new ArrayList<>();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		    if(convertView == null){
		        convertView = mInflater.inflate(R.layout.account_listitem, parent, false);
				convertView.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
							v.getForeground().setHotspot(event.getX(), event.getY());
						}
						return false;
					}
				});
			}
			ImageView icon = convertView.findViewById(R.id.accountIcon);
			ImageView check =  convertView.findViewById(R.id.checkMark);
			ImageView remove =  convertView.findViewById(R.id.remove);
			TextView screenName =  convertView.findViewById(R.id.accountName);

			final Account user = (Account) getItem(position);
			AppUtil.setImage(icon, user.getIconUrl());
			icon.setOnClickListener( v -> {
				if(TwitterUtils.getCurrentAccountId() != -1){
					Intent i = new Intent(getActivity(), UserDetailActivity.class);
					i.putExtra(C.SCREEN_NAME, user.getScreenName());
					startActivity(i);
				}
			});
			screenName.setText(user.getScreenName());
			if(user.IsCurrent()){
				check.setVisibility(View.VISIBLE);
				remove.setVisibility(View.GONE);
			} else {
				check.setVisibility(View.GONE);
				remove.setVisibility(View.VISIBLE);
			}

			remove.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					AlertDialog.Builder ab = new AlertDialog.Builder(getContext());
					ab.setTitle(R.string.confirm_delete_account)
							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									removeAccount(user);
								}
							})
							.setNegativeButton(R.string.cancel, null).create().show();
				}
			});
			return convertView;
		}

		private void removeAccount(final Account account){
			ParallelTask<Void, Void> task = new ParallelTask<Void, Void>() {
				@Override
				protected void onPreExecute() {
					mDialogManager.showProgress("削除中");
				}

				@Override
				protected Void doInBackground() {
					TwitterUtils.removeAccount(account);
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					mDialogManager.dismissProgress();
					mAdapter.remove(account);
				}
			};
			task.executeParallel();
		}


		@Override
		public int getCount() {
			return mAccountList.size();
		}

		@Override
		public Object getItem(int pos) {
			return mAccountList.get(pos);
		}

		@Override
		public long getItemId(int pos) {
			return mAccountList.get(pos).getUserId();
		}

		public void remove(Account user){
			mAccountList.remove(user);
			notifyDataSetChanged();
		}

		public void addIfNotContains(Account user){
			if(!mAccountList.contains(user)){
				mAccountList.add(user);
			}
		}
	}

	public void reloadAcounts() {
		List<Account> users = TwitterUtils.getAccounts();
		for(Account user : users){
			mAdapter.addIfNotContains(user);
		}
		mAdapter.notifyDataSetChanged();
	}
}