package com.crakac.ofuton;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.crakac.ofuton.util.AppUtil;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;

abstract public class AbstractPreviewActivity extends FragmentActivity {

    protected ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        mImageView = (ImageView) findViewById(R.id.iv_photo);
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                finish();
            }
        });
        mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onLongClickPreview(v);
            }
        });
    }

    protected boolean onLongClickPreview(View v) {
        return false;
    };

    protected void setBitmap(Bitmap bm){
        if(bm == null){
            AppUtil.showToast(R.string.impossible);
            finish();
        } else {
            mImageView.setImageBitmap(bm);
            updatePhotoViewAttacher();
        }
    }
    protected void updatePhotoViewAttacher(){
        mAttacher.update();
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }
}