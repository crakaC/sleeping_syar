package com.crakac.ofuton;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

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

        mPager = (HackyViewPager) findViewById(R.id.pager);
        mNav = (PreviewNavigation) findViewById(R.id.preview_nav);
        mNav.setNavigationListener(this);
        mNav.setVisibility(View.VISIBLE);

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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mPager.setPageTransformer(true, new DepthPageTransformer());
        }

        int pagerMargin = getResources().getDimensionPixelSize(R.dimen.preview_pager_margin);
        mPager.setPageMargin(pagerMargin);
        if(savedInstanceState == null) {
            int position = getIntent().getIntExtra(C.POSITION, 0);
            mPager.setCurrentItem(position);
        }
    }

    public void toggleNavigation(){
        if(mNav.isShown()){
            AppUtil.slideOut(mNav, 200);
        } else {
            AppUtil.slideIn(mNav, 200);
        }
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

    private ImagePreviewFragment getCurrentFragment() {
        return mAdapter.getItem(mPager.getCurrentItem());
    }

    private static class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        @SuppressLint("NewApi")
        @Override
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}