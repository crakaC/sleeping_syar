package com.crakac.ofuton.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.github.chrisbanes.photoview.OnViewTapListener;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

abstract public class AbstractPreviewActivity extends AppCompatActivity {

    protected ImageView mImageView;
    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);
        mImageView = findViewById(R.id.iv_photo);
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
        overridePendingTransition(0, R.anim.fade_out);
    }
}