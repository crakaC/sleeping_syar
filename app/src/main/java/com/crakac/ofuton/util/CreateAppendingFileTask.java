package com.crakac.ofuton.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;

import java.io.File;

/**
 * Created by Kosuke on 2017/12/29.
 */

public class CreateAppendingFileTask extends ParallelTask<Void, File> {
    private Uri uri;
    private ContentResolver contentResolver;
    private OnLoadFinishedListener listener;

    public CreateAppendingFileTask(Activity activity, Uri uri, OnLoadFinishedListener callback) {
        this.uri = uri;
        contentResolver = activity.getContentResolver();
        listener = callback;
    }

    @Override
    protected File doInBackground() {
        return BitmapUtil.createTemporaryResizedImage(contentResolver, uri, 1920);
    }

    @Override
    protected void onPostExecute(File file) {
        if (listener != null)
            listener.onLoadFinished(file);
    }

    @Override
    public ParallelTask<Void, File> executeParallel() {
        return (ParallelTask<Void, File>) executeOnExecutor(BitmapUtil.Executor);
    }

    public interface OnLoadFinishedListener {
        void onLoadFinished(File f);
    }
}