package com.crakac.ofuton;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.NetworkImageListener;
import com.crakac.ofuton.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WebImagePreviewActivity extends AbstractPreviewActivity implements LoaderCallbacks<String>{
    private static final String TAG = WebImagePreviewActivity.class.getSimpleName();
    private ImageContainer mImageContainer;

    private static final int ACTION_SAVE = 0;
    private static final int ACTION_CANCEL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri imageUri = getIntent().getData();
        if(imageUri == null){
            AppUtil.showToast(R.string.something_wrong);
            Log.d(TAG, "imageUri is null");
            finish();
            return;
        }
        showProgress(true);
        Bundle b = new Bundle(1);
        b.putParcelable(C.URI, imageUri);
        getSupportLoaderManager().initLoader(0, b, this).forceLoad();
    }

    @Override
    public Loader<String> onCreateLoader(int arg0, Bundle bundle) {
        return new UrlExpander(this, (Uri)bundle.getParcelable(C.URI));
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String url) {
        retrieveImage(url);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mImageContainer != null)
            mImageContainer.cancelRequest();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(0, ACTION_SAVE, 0, getString(R.string.save));
        menu.add(0, ACTION_CANCEL, 0, getString(R.string.cancel));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case ACTION_SAVE:
            saveFile();
            break;
        }
        return true;
    }

    @Override
    protected boolean onLongClickPreview(View v) {
        registerForContextMenu(v);
        openContextMenu(v);
        unregisterForContextMenu(v);
        return super.onLongClickPreview(v);
    }

    private void showProgress(boolean b) {
        findViewById(R.id.progressBar).setVisibility( b ? View.VISIBLE : View.GONE);
    }

    private void retrieveImage(String url){
        mImageContainer = NetUtil.fetchNetworkImageAsync(url, new NetworkImageListener(mImageView){
            @Override
            protected void onBitmap(Bitmap bitmap) {
                showProgress(false);
                updatePhotoViewAttacher();
            }
            @Override
            protected void onError(VolleyError error) {
                showProgress(false);
                AppUtil.showToast(R.string.impossible);
                finish();
            }
        });
    }

    private void saveFile() {
        new AsyncTask<Void, Void, Boolean>() {
            File distFile;
            @SuppressLint("SimpleDateFormat")
            @Override
            protected Boolean doInBackground(Void... params) {
                File cacheFile = null;
                String url = mImageContainer.getRequestUrl();
                FileOutputStream os = null;
                FileInputStream is = null;
                try {
                    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
                    String now = new SimpleDateFormat("yyyyMMddhhmmss.").format(new Date());
                    distFile = new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), now
                                    + extension);
                    distFile.getParentFile().mkdirs();
                    distFile.createNewFile();
                    byte[] data = NetUtil.getCache(url);
                    if(data == null){
                        cacheFile = NetUtil.download(WebImagePreviewActivity.this, url);
                        if(cacheFile == null) return false;
                        is = new FileInputStream(cacheFile);
                        data = new byte[(int) cacheFile.length()];
                        is.read(data);
                    }
                    os = new FileOutputStream(distFile);
                    os.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    Util.closeQuietly(os);
                    Util.closeQuietly(is);
                    if(cacheFile != null)
                        cacheFile.delete();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(distFile.getAbsolutePath()));
                    i.setDataAndType(Uri.fromFile(distFile), mimetype);
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher)).setSmallIcon(R.drawable.ic_menu_media).setTicker(getString(R.string.save_complete))
                            .setAutoCancel(true).setContentTitle(getString(R.string.save_complete))
                            .setContentText(getString(R.string.app_name)).setContentIntent(pi);
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.cancel(0);
                    nm.notify(0, builder.build());
                } else {
                    AppUtil.showToast(R.string.impossible);
                }
            }
        }.execute();
    }

    private static class UrlExpander extends AsyncTaskLoader<String>{
        Uri mUri;
        public UrlExpander(Context context, Uri uri) {
            super(context);
            mUri = uri;
        }

        @Override
        public String loadInBackground() {
            String expandUrl = "";
            try {
                expandUrl = NetUtil.expandUrlIfNecessary(mUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return expandUrl;
        }
    }
}
