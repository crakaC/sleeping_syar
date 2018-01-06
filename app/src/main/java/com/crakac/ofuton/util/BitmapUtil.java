package com.crakac.ofuton.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BitmapUtil {
    private static final String TAG = BitmapUtil.class.getSimpleName();
    public static Executor Executor = Executors.newCachedThreadPool();

    public static Bitmap getResizedBitmap(ContentResolver cr, Uri contentUri, int longEdge) {
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(cr.openInputStream(contentUri));

            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, null, opt);
            int scaleH = opt.outHeight / longEdge;
            int scaleW = opt.outWidth / longEdge;
            opt.inSampleSize = Math.min(scaleH, scaleW);
            opt.inJustDecodeBounds = false;

            Util.closeQuietly(stream);

            stream = new BufferedInputStream(cr.openInputStream(contentUri));
            return BitmapFactory.decodeStream(stream, null, opt);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(stream);
        }
        return null;
    }

    public static Bitmap getResizedBitmap(File file, int longEdge) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.toString(), opt);
        int scaleH = opt.outHeight / longEdge;
        int scaleW = opt.outWidth / longEdge;
        opt.inSampleSize = Math.min(scaleH, scaleW);
        opt.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.toString(), opt);
    }

    public static File createTemporaryResizedImage(ContentResolver cr, Uri contentUri, int longEdge) {
        Bitmap bm = getResizedBitmap(cr, contentUri, longEdge);
        float scaleW = (float) longEdge / bm.getWidth();
        float scaleH = (float) longEdge / bm.getHeight();
        float scale = Math.min(scaleH, scaleW);

        //縮小する必要があった場合は縮小する
        if (scale < 1.0) {
            int w = (int) (bm.getWidth() * scale + 0.5);
            int h = (int) (bm.getHeight() * scale + 0.5);
            Bitmap old = bm;
            try {
                bm = Bitmap.createScaledBitmap(old, w, h, true);
                old.recycle();
            } catch (OutOfMemoryError oome) {
                oome.printStackTrace();
            }
        }

        Bitmap.CompressFormat format = getFormatFromUri(cr, contentUri);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(format, 90, bytes);
        //bm.recycle();

        File tempFile = null;
        OutputStream os = null;
        try {
            tempFile = File.createTempFile("TMP",  "." + format.toString());
            os = new BufferedOutputStream(new FileOutputStream(tempFile));
            os.write(bytes.toByteArray());
            os.flush();
            os.close();
            if(format == Bitmap.CompressFormat.JPEG) {
                ExifInterface srcExifInterface = new ExifInterface(cr.openInputStream(contentUri));
                ExifInterface exifInterface = new ExifInterface(tempFile.toString());
                String orientation = srcExifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
                exifInterface.saveAttributes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(os);
        }

        return tempFile;
    }

    public static Bitmap rotateBitmap(ContentResolver cr, Uri uri, Bitmap bm) {
        try {
            ExifInterface exif = new ExifInterface(cr.openInputStream(uri));
            return rotateBitmap(bm, exif);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, String fileName) {
        try {
            ExifInterface exif = new ExifInterface(fileName);
            return rotateBitmap(bitmap, exif);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, ExifInterface exifInterface) {
        Bitmap resultBitmap = bitmap;
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
        }

        try {
            // Rotate the bitmap
            Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (!bitmap.equals(bm)) {
                bitmap.recycle();
            }
            return bm;
        } catch (Exception exception) {
            Log.d(TAG, "Could not rotate the image");
        }
        return resultBitmap;
    }

    private static Bitmap.CompressFormat getFormatFromUri(ContentResolver cr, Uri uri) {
        String extension = getExtensionFromUri(cr, uri);
        if(extension.contains("png")){
            return Bitmap.CompressFormat.PNG;
        }
        return Bitmap.CompressFormat.JPEG;
    }

    private static String getExtensionFromUri(ContentResolver cr, Uri uri) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if(!extension.isEmpty())
            return extension;

        Cursor c = cr.query(uri, new String[]{MediaStore.Images.Media.DISPLAY_NAME}, null, null, null);
        if (c != null) {
            c.moveToFirst();
            extension = MimeTypeMap.getFileExtensionFromUrl(c.getString(0));
            c.close();
        }
        return extension;
    }
}