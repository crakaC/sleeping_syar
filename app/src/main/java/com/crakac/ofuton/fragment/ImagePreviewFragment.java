package com.crakac.ofuton.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.AsyncLoadBitmapTask;
import com.crakac.ofuton.util.GlideApp;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.widget.Rotatable;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import twitter4j.MediaEntity;

/**
 * Created by kosukeshirakashi on 2014/09/24.
 */
public class ImagePreviewFragment extends Fragment implements Rotatable, AsyncLoadBitmapTask.OnLoadFinishedListener {

    protected ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private ProgressBar mProgressBar;
    AsyncLoadBitmapTask mTask;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_preview_image, null);
        mImageView = root.findViewById(R.id.iv_photo);
        mProgressBar = root.findViewById(R.id.progress);
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnViewTapListener((v, x, y) -> {
            finishActivity();
        });

        showProgress(true);

        Uri uri = getArguments().getParcelable(C.URI);
        if (uri != null) {
            mTask = new AsyncLoadBitmapTask(getActivity(), uri, mImageView);
            mTask.setOnLoadFinishedListener(this);
            mTask.executeParallel();
            return root;
        }

        String url = getArguments().getString(C.URL);
        if (url == null) {
            MediaEntity e = (MediaEntity) getArguments().getSerializable(C.MEDIA_ENTITY);
            url = e.getMediaURLHttps();
        }

        retrieveImage(NetUtil.convertToImageFileUrl(url));
        return root;
    }

    @Override
    public void onLoadFinished(Bitmap bitmap) {
        updatePhotoViewAttacher();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mImageView != null) {
            mImageView.setImageBitmap(null);
        }
        Glide.with(this).clear(mImageView);
        if (mTask != null) {
            mTask.setOnLoadFinishedListener(null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void retrieveImage(String url) {
        GlideApp.with(this).load(url).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                showProgress(false);
                AppUtil.showToast(R.string.impossible);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                showProgress(false);
                updatePhotoViewAttacher();
                return false;
            }
        }).into(mImageView);
    }

    private void showProgress(boolean b) {
        mProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void updatePhotoViewAttacher() {
        mAttacher.update();
    }

    @Override
    public void rotate(float degrees) {
        mAttacher.setRotationBy(degrees);
    }

    public void finishActivity() {
        Activity activity = getActivity();
        if (activity == null) return;
        activity.finish();
        activity.overridePendingTransition(0, R.anim.fade_out);
    }


}
