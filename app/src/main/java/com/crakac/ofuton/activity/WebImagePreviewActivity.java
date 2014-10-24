package com.crakac.ofuton.activity;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.fragment.ImagePreviewFragment;
import com.crakac.ofuton.fragment.adapter.SimpleFragmentPagerAdapter;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.widget.HackyViewPager;
import com.crakac.ofuton.widget.PreviewNavigation;
import com.crakac.ofuton.widget.Rotatable;
import com.crakac.ofuton.widget.Rotator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WebImagePreviewActivity extends FragmentActivity implements PreviewNavigation.NavigationListener, Rotator {
    private HackyViewPager mPager;
    private SimpleFragmentPagerAdapter<ImagePreviewFragment> mAdapter;
    private PreviewNavigation mNav;
    private List<String> mUrls;
    private HashMap<Integer, Rotatable> mRotatables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.actvity_image_preview);

        mPager = (HackyViewPager) findViewById(R.id.pager);
        mNav = (PreviewNavigation) findViewById(R.id.preview_nav);
        mNav.setNavigationListener(this);
        mNav.setVisibility(View.VISIBLE);

        mAdapter = new SimpleFragmentPagerAdapter<>(this, mPager);
        mRotatables = new HashMap<>();

        List<String> imageUrls = getIntent().getStringArrayListExtra(C.URL);
        Uri imageUri = getIntent().getData();
        if (imageUri != null) {
            mAdapter.add(ImagePreviewFragment.class, createArgs(imageUri.toString(), 0), 0);
            mUrls = new ArrayList<>(1);
            mUrls.add(imageUri.toString());
        } else if (imageUrls != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                mAdapter.add(ImagePreviewFragment.class, createArgs(imageUrls.get(i), i), i);
            }
            mUrls = imageUrls;
        }
        mAdapter.notifyDataSetChanged();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mPager.setPageTransformer(true, new DepthPageTransformer());
        } else {
            int pagerMargin = getResources().getDimensionPixelSize(R.dimen.preview_pager_margin);
            mPager.setPageMargin(pagerMargin);
        }
        if (savedInstanceState == null) {
            int position = getIntent().getIntExtra(C.POSITION, 0);
            mPager.setCurrentItem(position);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Resources res = getResources();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else {
                int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
                int navHeight = res.getDimensionPixelSize(resourceId);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mNav.getLayoutParams();
                params.bottomMargin += navHeight;
                mNav.setLayoutParams(params);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRotatables.clear();
    }

    public void toggleNavigation() {
        if (mNav.isShown()) {
            AppUtil.slideOut(mNav, 200);
        } else {
            AppUtil.slideIn(mNav, 200);
        }
    }

    private Bundle createArgs(String url, int index) {
        Bundle b = new Bundle(2);
        b.putString(C.URL, url);
        b.putInt(C.INDEX, index);
        return b;
    }

    @Override
    public void onDownloadClick() {
        String url = mUrls.get(mPager.getCurrentItem());
        saveImage(NetUtil.convertToImageFileUrl(url));
    }

    @Override
    public void onRotateLeftClick() {
        mRotatables.get(mPager.getCurrentItem()).rotate(-90f);
    }

    @Override
    public void onRotateRightClick() {
        mRotatables.get(mPager.getCurrentItem()).rotate(90f);
    }

    private static class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
        new AsyncTask<String, Void, File>() {

            @Override
            protected File doInBackground(String... params) {
                Log.d("DownloadImage", params[0]);
                return NetUtil.download(WebImagePreviewActivity.this, params[0]);
            }

            @Override
            protected void onPostExecute(File downloadedFile) {
                if (downloadedFile != null) {
                    Log.d("ImageDownloaded", downloadedFile.toString());
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(downloadedFile), "image/*");
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher)).setSmallIcon(R.drawable.ic_insert_photo_white_18dp).setTicker(getString(R.string.save_complete))
                            .setAutoCancel(true).setContentTitle(getString(R.string.save_complete))
                            .setContentText(getString(R.string.app_name)).setContentIntent(pi);
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel(0);
                    nm.notify(0, builder.build());
                } else {
                    AppUtil.showToast(R.string.impossible);
                }
            }
        }.execute(url);
    }

    @Override
    public void setRotatable(int index, Rotatable rotatable) {
        mRotatables.put(index, rotatable);
    }
}