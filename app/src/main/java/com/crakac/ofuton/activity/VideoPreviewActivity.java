package com.crakac.ofuton.activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.Util;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import twitter4j.ExtendedMediaEntity;

public class VideoPreviewActivity extends Activity {

    private static final int PERMISSION_REQUEST_STORAGE = 8686;

    private ExtendedMediaEntity mEntity;
    @Bind(R.id.videoView) VideoView mVideoView;
    @Bind(R.id.progress) ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        ButterKnife.bind(this);
        showProgress(true);
        mEntity = (ExtendedMediaEntity) getIntent().getSerializableExtra(C.MEDIA_ENTITY);
        for(ExtendedMediaEntity.Variant v : mEntity.getVideoVariants()){
            if(v.getContentType().contains("mp4")){
                mVideoView.setVideoURI(Uri.parse(v.getUrl()));
                mVideoView.setMediaController(new MediaController(this));
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        showProgress(false);
                        mp.start();
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
        mVideoView.setVisibility(View.GONE);
    }

    private boolean viewTouched = false;
    private float touchStartTime = 0f;
    private final long LONG_TOUCH_TIME_MS = 1000;
    private final Handler mHandler = new Handler();
    @OnTouch(R.id.videoView)
    public boolean onTouch(final View v, MotionEvent ev){
        if(ev.getAction() == MotionEvent.ACTION_DOWN) {
            viewTouched = true;
        } else if (ev.getAction() == MotionEvent.ACTION_UP){
            viewTouched = false;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(viewTouched) {
                    registerForContextMenu(v);
                    openContextMenu(v);
                    unregisterForContextMenu(v);
                    viewTouched = false;
                }
            }
        }, LONG_TOUCH_TIME_MS);
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(R.string.download);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
                return true;
            }
        }
        download(Util.getValidVideoUrl(mEntity));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        String url = Util.getValidVideoUrl(mEntity);
        switch(requestCode){
            case PERMISSION_REQUEST_STORAGE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    download(url);
                }
        }
    }


    public void download(String url) {
        new AsyncTask<String, Void, File>() {

            int notificationId = 1;
            @Override
            protected File doInBackground(String... params) {
                Log.d("DownloadImage", params[0]);
                return NetUtil.download(VideoPreviewActivity.this, params[0], notificationId);
            }

            @Override
            protected void onPostExecute(File downloadedFile) {
                if (downloadedFile != null) {
                    Log.d("ImageDownloaded", downloadedFile.toString());
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(downloadedFile), "video/*");
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                    icon.setHasMipMap(true);
                    builder.setLargeIcon(icon)
                            .setSmallIcon(R.drawable.ic_file_download_white_18dp)
                            .setTicker(getString(R.string.complete_download))
                            .setAutoCancel(true).setContentTitle(getString(R.string.complete_download))
                            .setContentText(getString(R.string.app_name)).setContentIntent(pi);
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(notificationId, builder.build());
                } else {
                    AppUtil.showToast(R.string.impossible);
                }
            }
        }.execute(url);
    }

    private void showProgress(boolean b){
        mProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
    }
}
