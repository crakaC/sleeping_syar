package com.crakac.ofuton.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.view.Display;

public class AsyncBitmapPreviewLoader extends AsyncTaskLoader<Bitmap> {
    private Uri mUri;
    private int mLongEdge;

    public AsyncBitmapPreviewLoader(Activity context, Uri uri) {
        super(context);
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        mUri = uri;
        mLongEdge = Math.min(width, height);
    }

    @Override
    public Bitmap loadInBackground() {
        return BitmapUtil.getResizedBitmap(getContext().getContentResolver(), mUri, mLongEdge);
    }
}
