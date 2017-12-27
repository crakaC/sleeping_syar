package com.crakac.ofuton.fragment;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;

import twitter4j.MediaEntity;

public class VideoPreviewFragment extends Fragment{
    private VideoView mVideoView;
    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_video_preview, null);
        mVideoView = root.findViewById(R.id.videoView);
        mProgressBar = root.findViewById(R.id.progress);
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
}
