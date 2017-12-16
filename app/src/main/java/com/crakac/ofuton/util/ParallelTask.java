package com.crakac.ofuton.util;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.RejectedExecutionException;

abstract public class ParallelTask<Progress, Result> extends AsyncTask< Class<Void>, Progress, Result> {

    @Override
    protected Result doInBackground(Class<Void>[] classes) {
        return doInBackground();
    }

    abstract protected Result doInBackground();

    public final ParallelTask<Progress, Result> executeParallel(){
        return (ParallelTask<Progress, Result>) executeOnExecutor(THREAD_POOL_EXECUTOR);
	}
}
