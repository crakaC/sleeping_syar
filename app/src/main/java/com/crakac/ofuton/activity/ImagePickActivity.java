package com.crakac.ofuton.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.ThumbnailTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kosukeshirakashi on 2014/11/20.
 */
public class ImagePickActivity extends ActionBarActivity {

    public static final String EXTRA_PICK_LIMIT = "limit";
    public static final String EXTRA_IMAGE_URIS = "extra_image_uris";
    private static final String[] IMAGE_COLUMNS = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
    private static final int SELECT_LIMIT_DEFAULT = 4;

    private GridView mGridView;
    private int mSelectLimits;
    private List<Long> mSelectedIds = new ArrayList<>();
    private GridImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pick);

        mSelectLimits = getIntent().getIntExtra(EXTRA_PICK_LIMIT, SELECT_LIMIT_DEFAULT);

        mGridView = (GridView) findViewById(R.id.gridView);
        if (mAdapter == null) {
            //TODO CursorLoaderを使った実装に変更する
            Cursor imageCursor = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_COLUMNS, null, null, MediaStore.Images.Media._ID + " desc");
            mAdapter = new GridImageAdapter(this, imageCursor, getContentResolver());
        }
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("ItemId", id + "");
                Log.d("ItemId by tag", parent.getAdapter().getItem(position).toString());
                toggleItem(view, id);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_pick, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pick_complete:
                Intent data = new Intent();
                data.putExtra(EXTRA_IMAGE_URIS, convertIdToUri(mSelectedIds));
                setResult(RESULT_OK, data);
                finish();
        }
        return true;
    }

    private Uri[] convertIdToUri(List<Long> ids) {
        Uri[] uris = new Uri[ids.size()];
        for (int i = 0; i < ids.size(); i++){
            uris[i] = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ids.get(i));
        }
        return uris;
    }

    private void toggleItem(View v, long itemId) {
        if (isSelected(itemId)) {
            mSelectedIds.remove(itemId);
            ((GridImageAdapter.ViewHolder) v.getTag()).setSelected(false);
        } else if (mSelectedIds.size() < mSelectLimits) {
            mSelectedIds.add(itemId);
            ((GridImageAdapter.ViewHolder) v.getTag()).setSelected(true);
        }
    }

    boolean isSelected(long id) {
        return mSelectedIds.contains(id);
    }

    static class GridImageAdapter extends CursorAdapter {
        private ImagePickActivity mActivity;
        private LayoutInflater mInflater;
        private ContentResolver mContentResolver;

        static class ViewHolder {
            ImageView image;
            View check;
            ThumbnailTask task;

            ViewHolder(View v) {
                image = (ImageView) v.findViewById(R.id.image);
                check = v.findViewById(R.id.check);
            }

            public void setSelected(boolean selected) {
                check.setVisibility((selected) ? View.VISIBLE : View.GONE);
            }
        }

        GridImageAdapter(Context context, Cursor c, ContentResolver contentResolver) {
            super(context, c, true);
            mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            mContentResolver = contentResolver;
            mActivity = (ImagePickActivity) context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = mInflater.inflate(R.layout.image_pick_item, parent, false);
            ViewHolder holder = new ViewHolder(v);
            v.setTag(holder);
            bindExistView(holder, cursor);
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            bindExistView((ViewHolder) view.getTag(), cursor);
        }

        private void bindExistView(ViewHolder holder, Cursor cursor) {
            long id = cursor.getLong(0);
            String imageFilePath = cursor.getString(1);
            holder.image.setTag(id);
            Log.d("ContentId", id + "");
            if (holder.task != null && holder.task.getStatus().equals(AsyncTask.Status.RUNNING)) {
                holder.task.cancel(true);
            }
            holder.task = new ThumbnailTask(mContentResolver, holder.image, imageFilePath, id);
            holder.task.executeParallel();
            setSelectState(holder.check, id);
        }

        public void setSelectState(View check, long id) {
            if (mActivity.isSelected(id)) {
                check.setVisibility(View.VISIBLE);
            } else {
                check.setVisibility(View.GONE);
            }
        }
    }
}
