package com.crakac.ofuton.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.NetworkImageListener;
import com.crakac.ofuton.widget.Rotatable;
import com.crakac.ofuton.widget.Rotator;

import twitter4j.MediaEntity;

public class VideoPreviewFragment extends Fragment implements Rotatable{
    private VideoView mVideoView;
    private ProgressBar mProgressBar;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int index = getArguments().getInt(C.INDEX, 0);
        ((Rotator) getActivity()).setRotatable(index, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_video_preview, null);
        mVideoView = (VideoView) root.findViewById(R.id.videoView);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progress);
        showProgress(true);

        String url = getArguments().getString(C.URL);
        if(url == null){
            MediaEntity e = (MediaEntity) getArguments().getSerializable(C.MEDIA_ENTITY);
            for(MediaEntity.Variant v : e.getVideoVariants()){
                if(v.getContentType().contains("mp4")){
                    url = v.getUrl();
                    break;
                }
            }
        }
        mVideoView.setVideoURI(Uri.parse(url));
        mVideoView.setMediaController(new MediaController(getActivity()));
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                showProgress(false);
                mp.start();
            }
        });

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void showProgress(boolean b) {
        mProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    @Override
    public void rotate(float degrees) {
//        mVideoView.setRotation(mVideoView.getRotation() + degrees);
    }

}
