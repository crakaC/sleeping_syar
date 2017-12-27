package com.crakac.ofuton.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.view.Display;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Kosuke on 2017/12/27.
 */

public class AsyncLoadBitmapTask extends ParallelTask<Void, Bitmap> {
    private static Executor sExecutor = Executors.newCachedThreadPool();
    private ContentResolver mContentResolver;
    private Uri mUri;
    private int mLongEdge;
    private WeakReference<ImageView> mTargetViewRef;
    private OnLoadFinishedListener mListener;

    public AsyncLoadBitmapTask(Activity context, Uri uri, ImageView target) {
        mContentResolver = context.getContentResolver();
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        mUri = uri;
        mLongEdge = Math.min(width, height);
        mTargetViewRef = new WeakReference<>(target);
    }

    public AsyncLoadBitmapTask(Activity context, Uri uri, ImageView target, int longEdge) {
        this(context, uri, target);
        mLongEdge = longEdge;
    }

    @Override
    protected Bitmap doInBackground() {
        Bitmap bitmap = BitmapUtil.getResizedBitmap(mContentResolver, mUri, mLongEdge);
        return BitmapUtil.rotateBitmap(mContentResolver, mUri, bitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView v = mTargetViewRef.get();
        if (bitmap != null && v != null) {
            v.setImageBitmap(bitmap);
        }
        if (mListener != null) {
            mListener.onLoadFinished(bitmap);
        }
    }

    @Override
    public ParallelTask<Void, Bitmap> executeParallel() {
        return (ParallelTask<Void, Bitmap>) executeOnExecutor(sExecutor);
    }

    public void setOnLoadFinishedListener(OnLoadFinishedListener listener){
        mListener = listener;
    }

    public interface OnLoadFinishedListener {
        void onLoadFinished(Bitmap bitmap);
    }
}
