package com.crakac.ofuton;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;

import com.crakac.ofuton.util.AsyncBitmapLoader;

import java.io.File;

public class PhotoPreviewActivity extends AbstractPreviewActivity implements LoaderCallbacks<Bitmap> {

    private static final int LOAD_FROM_FILE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File picPath = (File) getIntent().getSerializableExtra(C.FILE);
        Bundle b = new Bundle(1);
        b.putSerializable(C.FILE, picPath);
        getSupportLoaderManager().initLoader(LOAD_FROM_FILE, b, this).forceLoad();
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        if (id == LOAD_FROM_FILE) {
            File f = (File) args.getSerializable(C.FILE);
            if (f != null) {
                return new AsyncBitmapLoader(this, f);
            }
        }
        return null;

    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
        if (bitmap == null) {
            Log.w("PhotoPreviewActivity", "bitmap is null");
            return;
        }
        setBitmap(bitmap);
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> arg0) {
    }
}