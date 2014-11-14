package com.crakac.ofuton.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.crakac.ofuton.widget.Rotatable;
import com.crakac.ofuton.widget.Rotator;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by kosukeshirakashi on 2014/09/24.
 */
public class ImagePreviewFragment extends Fragment implements Rotatable {

    protected ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private ImageContainer mImageContainer;
    private ProgressBar mProgressBar;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int index = getArguments().getInt(C.INDEX, 0);
        ((Rotator) getActivity()).setRotatable(index, this);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

    public void updatePhotoViewAttacher() {
        mAttacher.update();
    }

    @Override
    public void rotate(float degrees) {
        mAttacher.setRotationBy(degrees);
    }

    public void finishActivity() {
        Activity activity = getActivity();
        if(activity == null) return;
        activity.finish();
        activity.overridePendingTransition(0, R.anim.fade_out);
    }
}
