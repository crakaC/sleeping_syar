package com.crakac.ofuton.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.ImagePreviewActivity;
import com.crakac.ofuton.activity.VideoPreviewActivity;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.util.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import twitter4j.MediaEntity;

/**
 * Created by kosukeshirakashi on 2014/09/09.
 */
public class MultipleImagePreview extends LinearLayout {
    private ImageView imageL1, imageC1, imageR1, imageL2, imageC2, imageR2, imageL3, imageC3, imageR3;
    private ImageView videoIcon;
    private LinearLayout mLeft, mCenter, mRight;
    private View separatorL1, separatorL2, separatorC1, separatorC2, separatorR1, separatorR2, virticalSeparatorLeft, virticalSeparatorRight;
    private List<ImageView> mImageViews;
    private List<View> mSeparators;
    private List<LinearLayout> mBlocks;

    public MultipleImagePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(LinearLayout.HORIZONTAL);
        View v = View.inflate(context, R.layout.inline_preview_layout, this);
        videoIcon = v.findViewById(R.id.videoIcon);
        mLeft = v.findViewById(R.id.left);
        mRight = v.findViewById(R.id.right);
        mCenter = v.findViewById(R.id.center);
        mBlocks = Arrays.asList(mLeft, mCenter, mRight);
        separatorL1 = v.findViewById(R.id.separatorL1);
        separatorC1 = v.findViewById(R.id.separatorC1);
        separatorR1 = v.findViewById(R.id.separatorR1);
        separatorL2 = v.findViewById(R.id.separatorL2);
        separatorC2 = v.findViewById(R.id.separatorC2);
        separatorR2 = v.findViewById(R.id.separatorR2);
        virticalSeparatorLeft = v.findViewById(R.id.virticalSeparatorLeft);
        virticalSeparatorRight = v.findViewById(R.id.virticalSeparatorRight);
        mSeparators = Arrays.asList(separatorL1, separatorC1, separatorR1, separatorL2, separatorC2, separatorR2, virticalSeparatorLeft, virticalSeparatorRight);
        imageL1 = v.findViewById(R.id.imageL1);
        imageC1 = v.findViewById(R.id.imageC1);
        imageR1 = v.findViewById(R.id.imageR1);
        imageL2 = v.findViewById(R.id.imageL2);
        imageC2 = v.findViewById(R.id.imageC2);
        imageR2 = v.findViewById(R.id.imageR2);
        imageL3 = v.findViewById(R.id.imageL3);
        imageC3 = v.findViewById(R.id.imageC3);
        imageR3 = v.findViewById(R.id.imageR3);
        mImageViews = Arrays.asList(imageL1, imageC1, imageR1, imageL2, imageC2, imageR2, imageL3, imageC3, imageR3);
        if (Util.isPreLollipop()) {
            for (View iv : mImageViews) {
                iv.setOnTouchListener(new ColorOverlayOnTouch());
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width * 9f / 16f + 0.5f);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    public List<ImageView> getRequiredImageViews(int mediaNum) {
        switch (mediaNum) {
            case 0:
                return new ArrayList<>();
            case 1:
                return Collections.singletonList(imageL1);
            case 2:
                return Arrays.asList(imageL1, imageR1);
            case 3:
                return Arrays.asList(imageL1, imageR1, imageR2);
            case 4:
                return Arrays.asList(imageL1, imageR1, imageL2, imageR2);
            case 5:
                return Arrays.asList(imageL1, imageC1, imageR1, imageC2, imageR2);
            case 6:
                return Arrays.asList(imageL1, imageC1, imageR1, imageL2, imageC2, imageR2);
            case 7:
                return Arrays.asList(imageL1, imageC1, imageL2, imageC2, imageR1, imageR2, imageR3);
            case 8:
                return Arrays.asList(imageL1, imageL2, imageC1, imageR1, imageC2, imageR2, imageC3, imageR3);
            default:
                return mImageViews;
        }
    }

    public void initLayout(int mediaNum) {
        hideAll();
        switch (mediaNum) {
            case 0:
                break;
            case 1:
                show(mLeft);
                break;
            case 2:
                show(mLeft, mRight, virticalSeparatorLeft);
                break;
            case 3:
                show(mLeft, mRight, virticalSeparatorLeft, separatorR1);
                break;
            case 4:
                show(mLeft, mRight, virticalSeparatorLeft, separatorR1, separatorL1);
                break;
            case 5:
                show(mBlocks);
                show(virticalSeparatorLeft, virticalSeparatorRight);
                show(separatorC1, separatorR1);
                break;
            case 6:
                show(mBlocks);
                show(virticalSeparatorLeft, virticalSeparatorRight);
                show(separatorC1, separatorR1, separatorL1);
                break;
            case 7:
                show(mBlocks);
                show(virticalSeparatorLeft, virticalSeparatorRight);
                show(separatorC1, separatorR1, separatorL1, separatorR2);
                break;
            case 8:
                show(mBlocks);
                show(virticalSeparatorLeft, virticalSeparatorRight);
                show(separatorC1, separatorR1, separatorL1, separatorR2, separatorC2);
                break;
            default:
                show(mBlocks);
                show(mSeparators);
                break;
        }
    }

    private void hideAll() {
        hide(mImageViews);
        hide(mSeparators);
        hide(mBlocks);
        hide(videoIcon);
    }

    private void hide(List<? extends View> views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    private void hide(View... views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    private void show(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void show(List<? extends View> views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    public void setMediaEntities(final List<MediaEntity> mediaEntities) {
        initLayout(mediaEntities.size());
        List<ImageView> imageViews = getRequiredImageViews(mediaEntities.size());
        List<String> mediaUrls = TwitterUtils.extractMediaUrls(mediaEntities);
        for (int i = 0; i < imageViews.size(); i++) {
            final ImageView imageView = imageViews.get(i);
            final int position = i;
            imageView.setVisibility(View.VISIBLE);
            final MediaEntity me = mediaEntities.get(position);
            if (hasVideoEntity(me)) {
                videoIcon.setVisibility(View.VISIBLE);
                imageView.setOnClickListener(v -> {
                    Context context = getContext();
                    Intent intent = new Intent(context, VideoPreviewActivity.class);
                    intent.putExtra(C.MEDIA_ENTITY, me);
                    context.startActivity(intent);
                });
            } else {
                imageView.setOnClickListener(v -> {
                    Context context = getContext();
                    Intent intent = new Intent(context, ImagePreviewActivity.class);
                    intent.putExtra(C.MEDIA_ENTITY, (Serializable) mediaEntities);
                    intent.putExtra(C.POSITION, position);
                    context.startActivity(intent);
                });
            }
            String mediaUrl = mediaUrls.get(i);
            AppUtil.setImage(imageView, NetUtil.convertToImageFileUrl(mediaUrl));
        }
    }

    private boolean hasVideoEntity(MediaEntity e) {
        return (e.getType().contains("gif") || e.getType().contains("video"));
    }
}
