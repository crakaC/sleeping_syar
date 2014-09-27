package com.crakac.ofuton.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.NetworkImageListener;
import com.crakac.ofuton.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by kosukeshirakashi on 2014/09/24.
 */
public class ImagePreviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<String>{

    protected ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private ImageContainer mImageContainer;
    private ProgressBar mProgressBar;

    public static ImagePreviewFragment createInstance(Uri imageUri) {
        Bundle b = new Bundle(1);
        b.putParcelable(C.URI, imageUri);
        ImagePreviewFragment f = new ImagePreviewFragment();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_preview_image, null);
        mImageView = (ImageView) root.findViewById(R.id.iv_photo);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progress);
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                finishActivity();
            }
        });

        showProgress(true);

        Uri imageUri = getArguments().getParcelable(C.URI);
        Bundle b = new Bundle(1);
        b.putParcelable(C.URI, imageUri);
        getLoaderManager().initLoader(0, b, this).forceLoad();

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageView.setImageBitmap(null);
        if (mImageContainer != null) {
            mImageContainer.cancelRequest();
            NetUtil.releaseBitmap(mImageContainer);
            mImageContainer = null;
        }
    }

    @Override
    public Loader<String> onCreateLoader(int arg0, Bundle bundle) {
        return new UrlExpander(getActivity(), (Uri) bundle.getParcelable(C.URI));
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String url) {
        retrieveImage(url);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
    }

    private void retrieveImage(String url) {
        mImageContainer = NetUtil.fetchNetworkImageAsync(url, new NetworkImageListener(mImageView) {
            @Override
            protected void onBitmap(Bitmap bitmap) {
                showProgress(false);
                updatePhotoViewAttacher();
            }

            @Override
            protected void onError(VolleyError error) {
                showProgress(false);
                AppUtil.showToast(R.string.impossible);
            }
        });
    }

    private void showProgress(boolean b) {
        mProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void saveImage() {
        new AsyncTask<Void, Void, Boolean>() {
            File distFile;

            @SuppressLint("SimpleDateFormat")
            @Override
            protected Boolean doInBackground(Void... params) {
                File cacheFile = null;
                String url = mImageContainer.getRequestUrl();
                FileOutputStream os = null;
                FileInputStream is = null;
                try {
                    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
                    String now = new SimpleDateFormat("yyyyMMddhhmmss.").format(new Date());
                    distFile = new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), now
                            + extension);
                    distFile.getParentFile().mkdirs();
                    distFile.createNewFile();
                    byte[] data = NetUtil.getCache(url);
                    if (data == null) {
                        cacheFile = NetUtil.download(getActivity(), url);
                        if (cacheFile == null) return false;
                        is = new FileInputStream(cacheFile);
                        data = new byte[(int) cacheFile.length()];
                        is.read(data);
                    }
                    os = new FileOutputStream(distFile);
                    os.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    Util.closeQuietly(os);
                    Util.closeQuietly(is);
                    if (cacheFile != null)
                        cacheFile.delete();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(distFile.getAbsolutePath()));
                    i.setDataAndType(Uri.fromFile(distFile), mimetype);
                    PendingIntent pi = PendingIntent.getActivity(getActivity().getApplicationContext(), 0, i, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity().getApplicationContext());
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher)).setSmallIcon(R.drawable.ic_menu_media).setTicker(getString(R.string.save_complete))
                            .setAutoCancel(true).setContentTitle(getString(R.string.save_complete))
                            .setContentText(getString(R.string.app_name)).setContentIntent(pi);
                    NotificationManager nm = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel(0);
                    nm.notify(0, builder.build());
                } else {
                    AppUtil.showToast(R.string.impossible);
                }
            }
        }.execute();
    }

    public void updatePhotoViewAttacher() {
        mAttacher.update();
    }

    public void rotatePreview(float degrees){
        mAttacher.setRotationBy(degrees);
    }

    public void finishActivity() {
        Activity activity = getActivity();
        activity.finish();
        activity.overridePendingTransition(0, android.R.anim.fade_out);
    }

    private static class UrlExpander extends AsyncTaskLoader<String> {
        Uri mUri;

        public UrlExpander(Context context, Uri uri) {
            super(context);
            mUri = uri;
        }

        @Override
        public String loadInBackground() {
            String expandUrl = "";
            try {
                expandUrl = NetUtil.expandUrlIfNecessary(mUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return expandUrl;
        }
    }

}
