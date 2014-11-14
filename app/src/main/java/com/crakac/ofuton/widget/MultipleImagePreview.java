package com.crakac.ofuton.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.WebImagePreviewActivity;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.TwitterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import twitter4j.MediaEntity;

/**
 * Created by kosukeshirakashi on 2014/09/09.
 */
public class MultipleImagePreview extends FrameLayout {
    private static int URL_EXPANDING_THREADS = 2;
    private static ExecutorService sExecutor = Executors.newFixedThreadPool(URL_EXPANDING_THREADS);
    private BitmapImageView topLeft, topCenter, topRight, bottomLeft, bottomCenter, bottomRight;
    private LinearLayout mLeft, mCenter, mRight;
    private View separatorLeft, separatorCenter, separatorRight, virticalSeparatorLeft, virticalSeparatorRight;
    private List<BitmapImageView> mImageViews;
    private List<View> mSeparators;
    private List<LinearLayout> mBlocks;

    public MultipleImagePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.appended_image_view, null);
        addView(v);
        mLeft = (LinearLayout) v.findViewById(R.id.left);
        mRight = (LinearLayout) v.findViewById(R.id.right);
        mCenter = (LinearLayout) v.findViewById(R.id.center);
        mBlocks = Arrays.asList(mLeft, mCenter, mRight);
        separatorLeft = v.findViewById(R.id.separatorLeft);
        separatorCenter = v.findViewById(R.id.separatorCenter);
        separatorRight = v.findViewById(R.id.separatorRight);
        virticalSeparatorLeft = v.findViewById(R.id.virticalSeparatorLeft);
        virticalSeparatorRight = v.findViewById(R.id.virticalSeparatorRight);
        mSeparators = Arrays.asList(separatorLeft, separatorCenter, separatorRight, virticalSeparatorLeft, virticalSeparatorRight);
        topLeft = (BitmapImageView) v.findViewById(R.id.imageTL);
        topCenter = (BitmapImageView) v.findViewById(R.id.imageTC);
        topRight = (BitmapImageView) v.findViewById(R.id.imageTR);
        bottomLeft = (BitmapImageView) v.findViewById(R.id.imageBL);
        bottomCenter = (BitmapImageView) v.findViewById(R.id.imageBC);
        bottomRight = (BitmapImageView) v.findViewById(R.id.imageBR);
        mImageViews = Arrays.asList(topLeft, topCenter, topRight, bottomLeft, bottomCenter, bottomRight);
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
        switch (mediaNum) {
            case 1:
                return Arrays.asList(topLeft);
            case 2:
                return Arrays.asList(topLeft, topRight);
            case 3:
                return Arrays.asList(topLeft, topRight, bottomRight);
            case 4:
                return Arrays.asList(topLeft, topRight, bottomLeft, bottomRight);
            case 5:
                return Arrays.asList(topLeft, topCenter, topRight, bottomCenter, bottomRight);
            case 6:
                return Arrays.asList(topLeft, topCenter, topRight, bottomLeft, bottomCenter, bottomRight);
            default:
                return new ArrayList<>();
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
                show(mLeft, mRight, virticalSeparatorLeft, separatorRight);
                break;
            case 4:
                show(mLeft, mRight, virticalSeparatorLeft, separatorRight, separatorLeft);
                break;
            case 5:
                show(mBlocks);
                show(virticalSeparatorLeft, virticalSeparatorRight);
                show(separatorCenter, separatorRight);
                break;
            case 6:
                show(mBlocks);
                show(mSeparators);
                break;
        }
    }

    private void hideAll() {
        hide(mImageViews);
        hide(mSeparators);
        hide(mBlocks);
    }

    private void hide(List<? extends View> views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    private void hide(View... views){
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    private void show(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    private void show(List<? extends View> views){
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    public void setMediaEntities(final List<MediaEntity> mediaEntities) {
        initLayout(mediaEntities.size());
        List<BitmapImageView> imageViews = getRequiredImageViews(mediaEntities.size());
        final ArrayList<String> mediaUrls = TwitterUtils.extractMediaUrls(mediaEntities);
        for (int i = 0; i < mediaUrls.size(); i++) {
            final BitmapImageView imageView = imageViews.get(i);
            final int position = i;
            imageView.setVisibility(View.VISIBLE);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = getContext();
                    Intent intent = new Intent(context, WebImagePreviewActivity.class);
                    intent.putStringArrayListExtra(C.URL, mediaUrls);
                    intent.putExtra(C.POSITION, position);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(com.crakac.ofuton.R.anim.fade_in, 0);
                }
            });
            imageView.setDefaultImageResId(R.color.transparent_black);
            imageView.setErrorImageResId(R.color.transparent_black);
            String mediaUrl = mediaUrls.get(i);
            imageView.setImageUrl(NetUtil.convertToImageFileUrl(mediaUrl), NetUtil.PREVIEW_LOADER);
        }
    }

    public void cleanUp() {
        for (BitmapImageView view : mImageViews) {
            view.cleanUp();
        }
    }
}
