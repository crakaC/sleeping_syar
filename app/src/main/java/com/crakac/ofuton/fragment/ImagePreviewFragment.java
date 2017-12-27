package com.crakac.ofuton.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.AsyncBitmapPreviewLoader;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.NetworkImageListener;
import com.crakac.ofuton.widget.Rotatable;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

import twitter4j.MediaEntity;

/**
 * Created by kosukeshirakashi on 2014/09/24.
 */
public class ImagePreviewFragment extends Fragment implements Rotatable, LoaderManager.LoaderCallbacks<Bitmap> {

    protected ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private ImageContainer mImageContainer;
    private ProgressBar mProgressBar;
    private Uri mUri;

    private static int sLoaderCounter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_preview_image, null);
        mImageView = root.findViewById(R.id.iv_photo);
        mProgressBar = root.findViewById(R.id.progress);
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                finishActivity();
            }
        });

        showProgress(true);

        Uri uri = getArguments().getParcelable(C.URI);
        if (uri != null) {
            mUri = uri;
            getActivity().getSupportLoaderManager().initLoader(sLoaderCounter++, null, this).forceLoad();
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
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new AsyncBitmapPreviewLoader(getActivity(), mUri);
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        mImageView.setImageBitmap(data);
        updatePhotoViewAttacher();
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mImageView != null) {
            mImageView.setImageBitmap(null);
        }
        if (mImageContainer != null) {
            mImageContainer.cancelRequest();
            mImageContainer = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void retrieveImage(String url) {
        mImageContainer = NetUtil.fetchPreviewImageAsync(url, new NetworkImageListener(mImageView) {
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
