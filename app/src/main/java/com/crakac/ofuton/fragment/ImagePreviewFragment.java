package com.crakac.ofuton.fragment;

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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.WebImagePreviewActivity;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.NetworkImageListener;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by kosukeshirakashi on 2014/09/24.
 */
public class ImagePreviewFragment extends Fragment {

    protected ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private ImageContainer mImageContainer;
    private ProgressBar mProgressBar;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_preview_image, null);
        mImageView = (ImageView) root.findViewById(R.id.iv_photo);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progress);
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((WebImagePreviewActivity) getActivity()).toggleNavigation();
                return true;
            }
        });
        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                finishActivity();
            }
        });

        showProgress(true);

        String url = getArguments().getString(C.URL);
        retrieveImage(NetUtil.convertToImageFileUrl(url));
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            mImageView.setImageBitmap(null);
        }
        if (mImageContainer != null) {
            mImageContainer.cancelRequest();
            mImageContainer = null;
        }
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
        mImageView.setTag(mImageContainer);
    }

    private void showProgress(boolean b) {
        mProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void saveImage() {
        new AsyncTask<Void, Void, File>() {

            @Override
            protected File doInBackground(Void... params) {
                String url = mImageContainer.getRequestUrl();
                Log.d("DownloadImage", url);
                return NetUtil.download(getActivity(), url);
            }

            @Override
            protected void onPostExecute(File downloadedFile) {
                if (downloadedFile != null) {
                    Log.d("ImageDownloaded", downloadedFile.toString());
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(downloadedFile), "image/*");
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

    public void rotatePreview(float degrees) {
        mAttacher.setRotationBy(degrees);
    }

    public void finishActivity() {
        Activity activity = getActivity();
        activity.finish();
        activity.overridePendingTransition(0, R.anim.fade_out);
    }
}
