package com.crakac.ofuton.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.AsyncTaskLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AsyncBitmapLoader extends AsyncTaskLoader<Bitmap> {
    private File mPath;

    public AsyncBitmapLoader(Context context, File path) {
        super(context);
        mPath = path;
    }

    @Override
    public Bitmap loadInBackground() {
        FileInputStream is = null;
        Bitmap bitmap = null;
        try {
            is = new FileInputStream(mPath);
            bitmap = BitmapFactory.decodeStream(is);
            bitmap = BitmapUtil.rotateImage(bitmap, mPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                is.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
