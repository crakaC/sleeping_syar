package com.crakac.ofuton.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.crakac.ofuton.R;

import java.io.IOException;

/**
 * Created by kosukeshirakashi on 2014/11/20.
 */

public class ThumbnailTask extends ParallelTask<Void, Bitmap, Bitmap> {

    private static ImageLruCache mBitmapCache = new ImageLruCache();
    private ContentResolver mContentResolver;
    private ImageView mImageView;
    private String mImageFileName;
    private long mId;

    public ThumbnailTask(ContentResolver contentResolver, ImageView imageView, String fileName, long id) {
        mContentResolver = contentResolver;
        mImageView = imageView;
        mImageFileName = fileName;
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
    protected Bitmap doInBackground(Void... params) {
        int orientation = getOrientation(mImageFileName);
        return getThumbnail(MediaStore.Images.Thumbnails.MICRO_KIND, orientation);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null && mImageView.getTag() != null && mImageView.getTag().equals(mId)) {
            mBitmapCache.put(String.valueOf(mId), bitmap);
            mImageView.setImageBitmap(bitmap);
        } else {
            Log.w("ThumbnailTask", "Bitmap is null");
        }
    }

    private Bitmap getThumbnail(int kind, int orientation) {
        Bitmap bm = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, mId, kind, null);
        if (orientation == 0) {
            return bm;
        }
        Log.d("ImageOrientation", orientation + "");
        Matrix m = new Matrix();
        m.setRotate(orientation);
        Bitmap rotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, false);
        bm.recycle();
        return rotated;
    }

    private int getOrientation(String filePath) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                degree = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                degree = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                degree = 270;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
}
