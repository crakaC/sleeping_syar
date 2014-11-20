package com.crakac.ofuton.activity;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.ThumbnailTask;

import java.io.File;

/**
 * Created by kosukeshirakashi on 2014/11/20.
 */
public class ImagePickActivity extends ActionBarActivity {

    private static final String[] IMAGE_COLUMNS = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};

    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pick);
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
        Cursor[] imageCursors = new Cursor[2];
        imageCursors[0] = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_COLUMNS, null, null, MediaStore.Images.Media._ID + " desc");
        imageCursors[1] = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.INTERNAL_CONTENT_URI, IMAGE_COLUMNS, null, null, MediaStore.Images.Media._ID + " desc");
        mGridView.setAdapter(new GridImageAdapter(getApplicationContext(), new MergeCursor(imageCursors), getContentResolver()));
    }

    static class GridImageAdapter extends CursorAdapter {
        private LayoutInflater mInflater;
        private ContentResolver mContentResolver;

        GridImageAdapter(Context context, Cursor c, ContentResolver contentResolver) {
            super(context, c, false);
            mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            mContentResolver = contentResolver;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ImageView iv = (ImageView) mInflater.inflate(R.layout.image_pick_item, parent, false);
            bindExistView(iv, cursor);
            return iv;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            bindExistView((ImageView) view, cursor);
        }

        private void bindExistView(ImageView v, Cursor cursor) {
            long id = cursor.getLong(0);
            //if(id == v.getTag()) return;
            v.setTag(id);
            new ThumbnailTask(mContentResolver, v, id, cursor.getString(1)).executeParallel();
        }
    }
}
