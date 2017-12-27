package com.crakac.ofuton.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.fragment.ImagePreviewFragment;
import com.crakac.ofuton.fragment.VideoPreviewFragment;
import com.crakac.ofuton.fragment.adapter.SimpleFragmentPagerAdapter;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.Util;
import com.crakac.ofuton.widget.HackyViewPager;
import com.crakac.ofuton.widget.PreviewNavigation;
import com.crakac.ofuton.widget.Rotatable;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;

public class ImagePreviewActivity extends AppCompatActivity implements PreviewNavigation.NavigationListener {
    private HackyViewPager mPager;
    private SimpleFragmentPagerAdapter<Fragment> mAdapter;
    private PreviewNavigation mNav;
    private List<String> mUrls;
    private static final int PERMISSION_REQUEST_STORAGE = 8686;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.actvity_image_preview);

        mPager = findViewById(R.id.pager);
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mNav.setImageIndex(position);
            }
        });
        mNav = findViewById(R.id.preview_nav);
        mNav.setNavigationListener(this);
        mNav.setVisibility(View.VISIBLE);

        mAdapter = new SimpleFragmentPagerAdapter<>(this, mPager);

        List<MediaEntity> mediaEntities = (List<MediaEntity>) getIntent().getSerializableExtra(C.MEDIA_ENTITY);
        List<Uri> attachedMedias = getIntent().getParcelableArrayListExtra(C.ATTACHMENTS);
        Uri imageUri = getIntent().getData();
        if (imageUri != null) {
            mAdapter.add(ImagePreviewFragment.class, createArgs(imageUri.toString(), 0), 0);
            mUrls = new ArrayList<>(1);
            mUrls.add(imageUri.toString());
        } else if (mediaEntities != null) {
            for (int i = 0; i < mediaEntities.size(); i++) {
                MediaEntity e = mediaEntities.get(i);
                boolean hasValidVideo = false;
                for (MediaEntity.Variant v : e.getVideoVariants()) {
                    if (v.getContentType().contains("mp4")) {
                        mAdapter.add(VideoPreviewFragment.class, createArgs(e, i), i);
                        hasValidVideo = true;
                    }
                }
                if (!hasValidVideo) {
                    mAdapter.add(ImagePreviewFragment.class, createArgs(e, i), i);
                }
            }
            mUrls = extractUrl(mediaEntities);
            mNav.setImageNums(mUrls.size());
        } else if (attachedMedias != null) {
            for (int i = 0; i < attachedMedias.size(); i++) {
                mAdapter.add(ImagePreviewFragment.class, createArgs(attachedMedias.get(i), i), i);
            }
            mNav.setDownloadEnabled(false);
            mNav.setImageNums(attachedMedias.size());
        }
        mAdapter.notifyDataSetChanged();

        mPager.setPageTransformer(true, new DepthPageTransformer());
        if (savedInstanceState == null) {
            int position = getIntent().getIntExtra(C.POSITION, 0);
            mPager.setCurrentItem(position);
        }
    }

    private List<String> extractUrl(List<MediaEntity> entities) {
        List<String> ret = new ArrayList<>();
        for (MediaEntity e : entities) {
            boolean hasValidUrl = false;
            for (MediaEntity.Variant v : e.getVideoVariants()) {
                if (v.getContentType().contains("mp4")) {
                    ret.add(v.getUrl());
                    hasValidUrl = true;
                    break;
                }
            }
            if (!hasValidUrl) {
                ret.add(e.getMediaURLHttps());
            }
        }
        return ret;
    }

    private Bundle createArgs(String url, int index) {
        Bundle b = new Bundle(2);
        b.putString(C.URL, url);
        b.putInt(C.INDEX, index);
        return b;
    }

    private Bundle createArgs(MediaEntity entity, int index) {
        Bundle b = new Bundle(2);
        b.putSerializable(C.MEDIA_ENTITY, entity);
        b.putInt(C.INDEX, index);
        return b;
    }

    private Bundle createArgs(Uri uri, int index) {
        Bundle b = new Bundle(2);
        b.putParcelable(C.URI, uri);
        b.putInt(C.INDEX, index);
        return b;
    }

    @Override
    public void onDownloadClick() {
        String url = mUrls.get(mPager.getCurrentItem());
        if (!Util.checkRuntimePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE))
            return;
        saveImage(NetUtil.convertToImageFileUrl(url));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onDownloadClick();
                }
        }
    }

    @Override
    public void onRotateLeftClick() {
        ((Rotatable) mAdapter.get(mPager.getCurrentItem())).rotate(-90f);
    }

    @Override
    public void onRotateRightClick() {
        ((Rotatable) mAdapter.get(mPager.getCurrentItem())).rotate(90f);
    }

    private static class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

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

    public void saveImage(String url) {
        NetUtil.download(this, url);
    }
}