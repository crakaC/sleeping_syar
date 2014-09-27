package com.crakac.ofuton.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.toolbox.ImageLoader;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.NetUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kosukeshirakashi on 2014/09/09.
 */
public class MultipleImagePreview extends FrameLayout {

    private ImageView mTL, mTR, mBL, mBR;
    private LinearLayout mLeft, mRight;
    private View separatorLeft, separatorRight, separatorCenter;
    private List<ImageView> mImageViews;
    private List<View> mSeparators;
    private int mImageCounts;

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
        mTL = (ImageView) v.findViewById(R.id.imageTL);
        mTR = (ImageView) v.findViewById(R.id.imageTR);
        mBL = (ImageView) v.findViewById(R.id.imageBL);
        mBR = (ImageView) v.findViewById(R.id.imageBR);
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

    public void release() {
        for (ImageView view : mImageViews) {
            ImageLoader.ImageContainer container = (ImageLoader.ImageContainer) view.getTag();
            if (container != null){
                container.cancelRequest();
                NetUtil.releaseBitmap(container);
            }
            view.setImageBitmap(null);
            view.setTag(null);
        }
    }

    public void setImageCounts(int counts) {
        mImageCounts = counts;
    }

    public List<ImageView> getImageViews() {
        List<ImageView> imageViews = new ArrayList<>();
        switch (mImageCounts) {
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

    public void initLayout() {
        hideAll();
        switch (mImageCounts) {
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

    private void show(List<? extends View> views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

}
