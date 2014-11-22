package com.crakac.ofuton.activity;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.ThumbnailTask;
import com.crakac.ofuton.widget.ColorOverlayOnTouch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kosukeshirakashi on 2014/11/20.
 */
public class ImagePickActivity extends ActionBarActivity {

    public static final String EXTRA_PICK_LIMIT = "limit";
    private static final String[] IMAGE_COLUMNS = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};

    private GridView mGridView;
    private int mSelectLimits;
    private List<Long> mSelectedIds = new ArrayList<>();
    private GridImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pick);

        mSelectLimits = getIntent().getIntExtra(EXTRA_PICK_LIMIT, 4);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);

        mGridView = (GridView) findViewById(R.id.gridView);
        AsyncQueryHandler mQueryHandler = new AsyncQueryHandler(getContentResolver()) {
            Cursor[] imageCursors = new Cursor[2];

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                imageCursors[token] = cursor;
                if (imageCursors[0] == null || imageCursors[1] == null) return;
                mGridView.setAdapter(new GridImageAdapter(getApplicationContext(), cursor, getContentResolver()));
            }
        };
        if(mAdapter == null) {
            Cursor[] imageCursors = new Cursor[2];
            imageCursors[0] = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_COLUMNS, null, null, MediaStore.Images.Media._ID + " desc");
            imageCursors[1] = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.INTERNAL_CONTENT_URI, IMAGE_COLUMNS, null, null, MediaStore.Images.Media._ID + " desc");
            mAdapter = new GridImageAdapter(getApplicationContext(), new MergeCursor(imageCursors), getContentResolver());
        }
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    boolean isSelected(long id){
        return mSelectedIds.contains(id);
    }

    static class GridImageAdapter extends CursorAdapter {
        private ImagePickActivity mActivity;
        private LayoutInflater mInflater;
        private ContentResolver mContentResolver;
        private List<Long> mSelectedIds;

        private static class ViewHolder {
            ImageView image;
            ImageView check;
            ViewHolder(View v){
                image = (ImageView)v.findViewById(R.id.image);
                check = (ImageView)v.findViewById(R.id.check);
            }
        }

        GridImageAdapter(Context context, Cursor c, ContentResolver contentResolver) {
            super(context, c, true);
            mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            mContentResolver = contentResolver;
            mSelectedIds = new ArrayList<>();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = mInflater.inflate(R.layout.image_pick_item, parent, false);
            ViewHolder holder = new ViewHolder(v);
            v.setTag(holder);
            holder.image.setOnTouchListener(new ColorOverlayOnTouch());
            bindExistView(holder, cursor);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            bindExistView((ViewHolder)view.getTag(), cursor);
        }

        private void bindExistView(ViewHolder holder, Cursor cursor) {
            long id = cursor.getLong(0);
            holder.image.setTag(id);
            new ThumbnailTask(mContentResolver, holder.image, id).executeParallel();
            setSelectState(holder.check, id);
        }

        private void setSelectState(ImageView check, long id){
            if(mActivity.isSelected(id)){
                check.setVisibility(View.VISIBLE);
            } else {
                check.setVisibility(View.GONE);
            }
        }
    }
}
