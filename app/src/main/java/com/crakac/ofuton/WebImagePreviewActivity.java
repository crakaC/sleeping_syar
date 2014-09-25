package com.crakac.ofuton;

import android.annotation.SuppressLint;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.NetworkImageListener;
import com.crakac.ofuton.util.Util;
import com.crakac.ofuton.widget.ImagePreviewFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import twitter4j.MediaEntity;
import twitter4j.Status;

public class WebImagePreviewActivity extends AbstractPreviewActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.actvity_image_preview);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());

        Status status = (Status) getIntent().getSerializableExtra(C.STATUS);
        Uri imageUri = getIntent().getData();
        if (imageUri != null) {
            adapter.add(ImagePreviewFragment.createInstance(imageUri));
        } else if (status != null){
            for (MediaEntity entity : status.getExtendedMediaEntities()) {
                Uri uri = Uri.parse(entity.getMediaURL());
                adapter.add(ImagePreviewFragment.createInstance(uri));
            }
        }
        pager.setAdapter(adapter);

        int pagerMargin = getResources().getDimensionPixelSize(R.dimen.preview_pager_margin);
        pager.setPageMargin(pagerMargin);
    }
}
