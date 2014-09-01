package com.crakac.ofuton.util;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public class ParallelTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	
	@SuppressLint("NewApi")
    @SuppressWarnings("unchecked")
    public final AsyncTask<Params, Progress, Result> executeParallel(Params... params){
		if(Build.VERSION.SDK_INT >= 11){
			return super.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
		} else {
			return super.execute(params);
		}
	}

    @SuppressWarnings("unchecked")
	@Override
	protected Result doInBackground(Params... params) {
		// TODO Auto-generated method stub
		return null;
	}
}
