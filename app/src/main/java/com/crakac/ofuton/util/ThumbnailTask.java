package com.crakac.ofuton.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.crakac.ofuton.R;

/**
 * Created by kosukeshirakashi on 2014/11/20.
 */

public class ThumbnailTask extends ParallelTask<Void, Bitmap, Bitmap> {

    private static ImageLruCache mBitmapCache = new ImageLruCache();
    private ContentResolver mContentResolver;
    private ImageView mImageView;
    private long mId;

    public ThumbnailTask(ContentResolver contentResolver, ImageView imageView, long id) {
        mContentResolver = contentResolver;
        mImageView = imageView;
        mId = id;
    }

    @Override
    protected void onPreExecute() {
        Bitmap bm = mBitmapCache.get(String.valueOf(mId));
        if (bm == null) {
            mImageView.setImageResource(R.color.transparent_black);
        } else {
            cancel(true);
            onPostExecute(bm);
        }
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        if (mImageView.getTag() != null && mImageView.getTag().equals(mId)) {
            mImageView.setImageBitmap(values[0]);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        int orientation = 0;
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mId);
        Cursor c = MediaStore.Images.Media.query(mContentResolver, contentUri, new String[]{MediaStore.Images.ImageColumns.ORIENTATION});
        if(c != null){
            c.moveToFirst();
            orientation = c.getInt(0);
        }
        Bitmap bm = getThumbnail(MediaStore.Images.Thumbnails.MICRO_KIND, orientation);
        publishProgress(bm);
        return getThumbnail(MediaStore.Images.Thumbnails.MINI_KIND, orientation);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mImageView.getTag() != null && mImageView.getTag().equals(mId)) {
            mBitmapCache.put(String.valueOf(mId), bitmap);
            mImageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap getThumbnail(int kind, int orientation) {
        Bitmap bm = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, mId, kind, null);
        if (orientation == 0) {
            return bm;
        }
        Matrix m = new Matrix();
        m.setRotate(orientation);
        Bitmap rotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, false);
        bm.recycle();
        return rotated;

    }
}
