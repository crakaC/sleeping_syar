package com.crakac.ofuton;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.widget.HackyViewPager;
import com.crakac.ofuton.widget.ImagePreviewFragment;
import com.crakac.ofuton.widget.PreviewNavigation;

import twitter4j.MediaEntity;
import twitter4j.Status;

public class WebImagePreviewActivity extends AbstractPreviewActivity implements PreviewNavigation.NavigationListener {
    private HackyViewPager mPager;
    private SimpleFragmentPagerAdapter<ImagePreviewFragment> mAdapter;
    private PreviewNavigation mNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.actvity_image_preview);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        mPager = (HackyViewPager) findViewById(R.id.pager);
        mNav = (PreviewNavigation) findViewById(R.id.preview_nav);
        mNav.setNavigationListener(this);

        mAdapter = new SimpleFragmentPagerAdapter<>(getSupportFragmentManager());

        Status status = (Status) getIntent().getSerializableExtra(C.STATUS);
        Uri imageUri = getIntent().getData();
        if (imageUri != null) {
            mAdapter.add(ImagePreviewFragment.createInstance(imageUri));
        } else if (status != null) {
            for (MediaEntity entity : status.getExtendedMediaEntities()) {
                Uri uri = Uri.parse(entity.getMediaURL());
                mAdapter.add(ImagePreviewFragment.createInstance(uri));
            }
        }
        mPager.setAdapter(mAdapter);

        int pagerMargin = getResources().getDimensionPixelSize(R.dimen.preview_pager_margin);
        mPager.setPageMargin(pagerMargin);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pos", mPager.getCurrentItem());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPager.setCurrentItem(savedInstanceState.getInt("pos"));
    }

    @Override
    public void onDownloadClick() {
        getCurrentFragment().saveImage();
    }

    @Override
    public void onRotateLeftClick() {
        getCurrentFragment().rotatePreview(-90f);
    }

    @Override
    public void onRotateRightClick() {
        getCurrentFragment().rotatePreview(90f);
    }

    public void toggleNavigation() {
        if (mNav.isShown()) {
            AppUtil.slideOut(mNav, 200);
            Log.d("nav", "slide out");
        } else {
            AppUtil.slideIn(mNav, 200);
            Log.d("nav", "slide in");
        }
    }

    private ImagePreviewFragment getCurrentFragment() {
        return mAdapter.getItem(mPager.getCurrentItem());
    }
}