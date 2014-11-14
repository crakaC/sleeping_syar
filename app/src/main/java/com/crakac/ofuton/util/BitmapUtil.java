package com.crakac.ofuton.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtil {
    private static final String TAG=BitmapUtil.class.getSimpleName();
    /**
     * ビットマップ表示時のメモリ量を減らす程度のリサイズ（厳密なリサイズではなく、読み込み時のスケール（整数）を下げる）
     *
     * @param file
     * @param longEdge
     * @return
     */
    public static Bitmap getResizedBitmap(File file, int longEdge) {
        return getResizedBitmap(file, longEdge, longEdge);
    }

    /**
     * ビットマップ表示時のメモリ量を減らす程度のリサイズ（厳密なリサイズではなく、読み込み時のスケール（整数）を下げる）
     * 元々指定したサイズより小さかった場合はそのまま返す。
     * @param file
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public static Bitmap getResizedBitmap(File file, int targetWidth, int targetHeight) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.toString(), opt);
        if(opt.outWidth > targetHeight || opt.outHeight > targetHeight){
            int scaleH = opt.outHeight / targetHeight;
            int scaleW = opt.outWidth / targetWidth;
            int scale = Math.min(scaleH, scaleW);
            float n = 1.0f;
            while(n <= scale){
                n*=2;
            }
            opt.inSampleSize = (int)n;
            Log.d("ResizeBitmap", "inSampleSize=" + n);
        }
        opt.inJustDecodeBounds = false;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(file.toString(), opt);
    }

    /**
     * longEdgeで指定した長辺の長さになるようリサイズする
     *
     * @param src
     * @param longEdge
     * @return リサイズ後のファイル
     * @throws java.io.IOException
     */
    public static File createTemporaryResizedImage(File src, int longEdge) throws IOException {
        Bitmap bm = getResizedBitmap(src, longEdge);
        float scaleW = (float) longEdge / bm.getWidth();
        float scaleH = (float) longEdge / bm.getHeight();
        float scale = Math.min(scaleH, scaleW);

        //縮小する必要があった場合は縮小する
        if(scale < 1.0){
            int w = (int) (bm.getWidth() * scale + 0.5);
            int h = (int) (bm.getHeight() * scale + 0.5);
            Bitmap old = bm;
            try {
                bm = Bitmap.createScaledBitmap(old, w, h, true);
                old.recycle();
            }catch(OutOfMemoryError oome){
                oome.printStackTrace();
            }
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        bm.recycle();

        File tempFile = File.createTempFile("TMP", ".jpg");

        FileOutputStream os = new FileOutputStream(tempFile);
        os.write(bytes.toByteArray());
        os.flush();
        os.close();

        ExifInterface srcExifInterface = new ExifInterface(src.toString());
        ExifInterface exifInterface = new ExifInterface(tempFile.toString());
        String orientation = srcExifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientation);
        exifInterface.saveAttributes();

        return tempFile;
    }

    public static Bitmap rotateImage(Bitmap bitmap, String filePath)
    {
        Bitmap resultBitmap = bitmap;

        try
        {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Matrix matrix = new Matrix();
            switch (orientation){
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

            // Rotate the bitmap
            resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        catch (Exception exception)
        {
            Log.d(TAG, "Could not rotate the image");
        }
        return resultBitmap;
    }
}
