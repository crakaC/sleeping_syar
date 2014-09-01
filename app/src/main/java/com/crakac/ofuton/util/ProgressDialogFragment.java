package com.crakac.ofuton.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Window;

public class ProgressDialogFragment extends DialogFragment{
	private static ProgressDialog progressDialog;
	public static ProgressDialogFragment newInstance(String msg){
		ProgressDialogFragment dialog = new ProgressDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString("msg", msg);
		dialog.setArguments(bundle);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage(getArguments().getString("msg"));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		return progressDialog;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		//キャンセル時はアクティビティを終了．
		getActivity().finish();
	}

	@Override
	public Dialog getDialog() {
		return progressDialog;
	}
}
