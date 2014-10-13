package com.crakac.ofuton.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.adapter.DmAdapter;

import twitter4j.DirectMessage;

/**
 *
 * @author Kosuke
 *
 */
public class DmInfoDialogFragment extends DialogFragment {

	private DmAdapter mAdapter;
	private ListView lvStatus;
	private Dialog dialog;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		dialog = new Dialog(getActivity());
		// タイトルを消す
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		// レイアウトを適用
		dialog.setContentView(R.layout.tweet_info_dialog);
		// DM詳細を表示
		lvStatus = (ListView) dialog.findViewById(R.id.tweet_status);
		lvStatus.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				dialog.dismiss();
			}
		});
		mAdapter = new DmAdapter(getActivity());
		lvStatus.setAdapter(mAdapter);
		mAdapter.add((DirectMessage)getArguments().getSerializable(C.DM));
		return dialog;
	}
}
