package com.crakac.ofuton.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.volley.toolbox.NetworkImageView;
import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.WebImagePreviewActivity;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.TwitterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;

/**
 * Created by kosukeshirakashi on 2014/09/09.
 */
public class MultipleImagePreview extends FrameLayout {

    private BitmapImageView mTL, mTR, mBL, mBR;
    private LinearLayout mLeft, mRight;
    private View separatorLeft, separatorRight, separatorCenter;
    private List<BitmapImageView> mImageViews;
    private List<View> mSeparators;

    public MultipleImagePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.appended_image_view, null);
        addView(v);
        mLeft = (LinearLayout) v.findViewById(R.id.left);
        mRight = (LinearLayout) v.findViewById(R.id.right);
        separatorLeft = v.findViewById(R.id.separatorLeft);
        separatorRight = v.findViewById(R.id.separatorRight);
        separatorCenter = v.findViewById(R.id.separatorCenter);
        mSeparators = Arrays.asList(separatorLeft, separatorRight, separatorCenter);
        mTL = (BitmapImageView) v.findViewById(R.id.imageTL);
        mTR = (BitmapImageView) v.findViewById(R.id.imageTR);
        mBL = (BitmapImageView) v.findViewById(R.id.imageBL);
        mBR = (BitmapImageView) v.findViewById(R.id.imageBR);
        mImageViews = Arrays.asList(mTL, mTR, mBL, mBR);
        for (View iv : mImageViews) {
            iv.setOnTouchListener(new ColorOverlayOnTouch());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (width * 9f / 16f + 0.5f);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    public List<BitmapImageView> getRequiredImageViews(int mediaNum) {
        List<BitmapImageView> imageViews = new ArrayList<>();
        switch (mediaNum) {
            case 0:
                break;
            case 1:
                imageViews = Arrays.asList(mTL);
                break;
            case 2:
                imageViews = Arrays.asList(mTL, mTR);
                break;
            case 3:
                imageViews = Arrays.asList(mTL, mTR, mBR);
                break;
            case 4:
                imageViews = Arrays.asList(mTL, mTR, mBL, mBR);
                break;
        }
        return imageViews;
    }

    public void initLayout(int mediaNum) {
        hideAll();
        switch (mediaNum) {
            case 0:
                break;
            case 1:
                mRight.setVisibility(View.GONE);
                break;
            case 2:
                show(mRight, separatorCenter);
                break;
            case 3:
                show(mRight, separatorCenter, separatorRight);
                break;
            case 4:
                show(mRight, separatorCenter, separatorRight, separatorLeft);
                break;
        }
    }

    private void hideAll() {
        hide(mImageViews);
        hide(mSeparators);
    }

    private void hide(List<? extends View> views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    private void show(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    public void setPreview(final Status status){
        if (status.getMediaEntities().length == 0) {
            return;
        }

        final MediaEntity[] medias = TwitterUtils.getMediaEntities(status);

        initLayout(medias.length);
        List<BitmapImageView> imageViews = getRequiredImageViews(medias.length);

        for (int i = 0; i < medias.length; i++) {
            final BitmapImageView imageView = imageViews.get(i);
            final int position = i;
            imageView.setVisibility(View.VISIBLE);
            final MediaEntity media = medias[i];
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getContext();
                    Intent intent = new Intent(context, WebImagePreviewActivity.class);
                    intent.putExtra(C.STATUS, status);
                    intent.putExtra(C.POSITION, position);
                    context.startActivity(intent);
                    ((Activity)context).overridePendingTransition(R.anim.fade_in, 0);
                }
            });
            imageView.setDefaultImageResId(R.color.transparent_black);
            imageView.setErrorImageResId(R.color.transparent_black);
            imageView.setImageUrl(media.getMediaURL(), NetUtil.PREVIEW_LOADER);
        }
    }

    public void cleanUp(){
        for(BitmapImageView view : mImageViews){
            view.cleanUp();
        }
    }
}
