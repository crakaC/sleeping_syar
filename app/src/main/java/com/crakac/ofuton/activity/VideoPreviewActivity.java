package com.crakac.ofuton.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.Util;

import twitter4j.MediaEntity;

public class VideoPreviewActivity extends Activity {

    private static final int PERMISSION_REQUEST_STORAGE = 8686;

    private MediaEntity mEntity;
    private VideoView mVideoView;
    private ProgressBar mProgressBar;
    private ImageView mFrame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        mVideoView = findViewById(R.id.videoView);
        mProgressBar = findViewById(R.id.progress);
        mFrame = findViewById(R.id.videoFrame);
        registerForContextMenu(mFrame);
        showProgress(true);
        mEntity = (MediaEntity) getIntent().getSerializableExtra(C.MEDIA_ENTITY);
        for(MediaEntity.Variant v : mEntity.getVideoVariants()){
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(R.string.download);
    }

    boolean onTouch(View v, MotionEvent me){
        return false;
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
        NetUtil.download(this, url);
    }

    private void showProgress(boolean b){
        mProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
    }
}
