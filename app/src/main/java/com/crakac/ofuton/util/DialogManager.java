package com.crakac.ofuton.util;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class DialogManager {
	private ProgressDialogFragment progressDialog;
	private FragmentManager mFragmentManager;

	public DialogManager(FragmentManager fm){
		mFragmentManager = fm;
	}
	
	public void showProgress(String msg){
		dismissProgress();
		progressDialog = ProgressDialogFragment.newInstance(msg);
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.add(progressDialog, "progress");
		ft.commitAllowingStateLoss();
	}
	
	public void dismissProgress(){
		if(progressDialog != null && progressDialog.getDialog() != null){
			progressDialog.getDialog().dismiss();
		}
	}
}
