package com.crakac.ofuton.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.PhotoPreviewActivity;
import com.crakac.ofuton.fragment.dialog.TweetInfoDialogFragment;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.BitmapUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.util.Util;
import com.crakac.ofuton.widget.ColorOverlayOnTouch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterAPIConfiguration;
import twitter4j.TwitterException;

/**
 * Created by kosukeshirakashi on 2014/10/24.
 */
public class TweetFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = TweetFragment.class.getSimpleName();

    private static final int SELECT_PICTURE = 1;

    private static final int MAX_TWEET_LENGTH = 140;
    private static final int MAX_APPEND_PICTURE_EDGE_LENGTH = 1920;
    private static final String IMAGE_URI = "IMAGE_URI";
    public static final String APPENDED_FILE = "APPENDED_FILE";

    private EditText mInputText;
    private File mAppendingFile; // 画像のアップロードに使用
    private TextView mRemainingText;// 残り文字数を表示
    private View mTweetBtn, mAppendBtn;// つぶやくボタン，画像追加ボタン，リプライ元情報ボタン
    private ImageView mAppendedImageView;
    private Uri mImageUri;// カメラ画像添付用
    private TwitterAPIConfiguration mApiConfiguration;
    private View mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mImageUri = savedInstanceState.getParcelable(IMAGE_URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_tweet, null);
        mRootView.setVisibility(View.INVISIBLE);
        mRootView.setOnClickListener(this);
        mApiConfiguration = TwitterUtils.getApiConfiguration();
        mInputText = (EditText) mRootView.findViewById(R.id.input_text);// 入力欄
        mTweetBtn = mRootView.findViewById(R.id.action_tweet);// ツイートボタン
        mAppendBtn = mRootView.findViewById(R.id.appendPic);// 画像添付ボダン
        mRemainingText = (TextView) mRootView.findViewById(R.id.remainingText);// 残り文字数
        mAppendedImageView = (ImageView) mRootView.findViewById(R.id.appendedImage);

        // 画像添付ボタンタップの動作
        mAppendBtn.setOnClickListener(this);
        // 添付画像ミニプレビュータップの動作
        mAppendedImageView.setOnClickListener(this);
        mAppendedImageView.setOnTouchListener(new ColorOverlayOnTouch());

        // tweetボタンの動作
        mTweetBtn.setOnClickListener(this);

        // 文章に変更があったら残り文字数を変化させる
        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setRemainLength();
            }
        });

        // 画像添付ボタンタップの動作
        mAppendBtn.setOnClickListener(this);
        // 添付画像ミニプレビュータップの動作
        mAppendedImageView.setOnClickListener(this);
        mAppendedImageView.setOnTouchListener(new ColorOverlayOnTouch());

        // tweetボタンの動作
        mTweetBtn.setOnClickListener(this);

        // 文章に変更があったら残り文字数を変化させる
        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setRemainLength();
            }
        });

        if (savedInstanceState != null) {
            mAppendingFile = (File) savedInstanceState.getSerializable(APPENDED_FILE);
        }
        if (mAppendingFile != null) {
            setAppendingImageDrawable(mAppendingFile);
        }

        setRemainLength();
        return mRootView;
    }

    private final static int PREVIEW_APPENDED_IMAGE = 1;
    private final static int REMOVE_APPENDED_IMAGE = 2;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.appendedImage:
                menu.setHeaderTitle("添付画像");
                menu.add(0, PREVIEW_APPENDED_IMAGE, 0, getString(R.string.preview));
                menu.add(0, REMOVE_APPENDED_IMAGE, 0, getString(R.string.delete));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case PREVIEW_APPENDED_IMAGE:
                previewAppendedImage();
                return true;
            case REMOVE_APPENDED_IMAGE:
                removeAppendedImage();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void previewAppendedImage() {
        Intent i = new Intent(getActivity(), PhotoPreviewActivity.class);
        i.putExtra(C.FILE, mAppendingFile);
        startActivity(i);
    }

    private void removeAppendedImage() {
        mAppendedImageView.setVisibility(View.GONE);
        clearTemporaryImageFile();
        setRemainLength();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_URI, mImageUri);
        outState.putSerializable(APPENDED_FILE, mAppendingFile);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    public static final String MATCH_URL_HTTPS = "(https)(:\\/\\/[-_.!~*\\'()a-zA-Z0-9;\\/?:\\@&=+\\$,%#]+)";
    public static final String MATCH_URL_HTTP = "(http)(:\\/\\/[-_.!~*\\'()a-zA-Z0-9;\\/?:\\@&=+\\$,%#]+)";

    /**
     * ツイートの残り文字数を求め，テキストビューに反映する
     * <p/>
     * 編集中のEditText
     */
    private void setRemainLength() {
        String text = mInputText.getEditableText().toString();
        text = text.replaceAll(MATCH_URL_HTTPS, Util.blanks(mApiConfiguration.getShortURLLengthHttps()));
        text = text.replaceAll(MATCH_URL_HTTP, Util.blanks(mApiConfiguration.getShortURLLength()));
        int remainLength = MAX_TWEET_LENGTH - text.length();
        if (mAppendingFile != null) {
            remainLength -= mApiConfiguration.getCharactersReservedPerMedia();
        }
        if (remainLength < 0 || (remainLength == MAX_TWEET_LENGTH && mAppendingFile == null)) {
            enableTweetButton(false);
        } else {
            enableTweetButton(true);
        }
        mRemainingText.setText(String.valueOf(remainLength));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode == SELECT_PICTURE) {
            boolean isCamera = false;
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                    String action = data.getAction();
                    if(action != null){
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                } else {
                    isCamera = true;
                }
                if (uri == null) {
                    uri = mImageUri;
                }
                appendPicture(uri);

                if (!isCamera) {
                    // ContentResolverでファイルを登録してあるので削除する。しないとゴミが出る。
                    getActivity().getContentResolver().delete(mImageUri, null, null);
                }
            } else {
                getActivity().getContentResolver().delete(mImageUri, null, null);
            }
        }
    }

    private void enableTweetButton(boolean enabled) {
        mTweetBtn.setEnabled(enabled);
    }

    private void appendPicture(Uri uri) {
        clearTemporaryImageFile();
        ContentResolver cr = getActivity().getContentResolver();
        String[] columns = {MediaStore.Images.Media.DATA};
        Cursor c = cr.query(uri, columns, null, null, null);
        c.moveToFirst();
        File imageFile = new File(c.getString(0));
        try {
            mAppendingFile = BitmapUtil.createTemporaryResizedImage(imageFile, MAX_APPEND_PICTURE_EDGE_LENGTH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mAppendingFile == null) {
            AppUtil.showToast("ファイルの読み込みに失敗しました");
        } else {
            setAppendingImageDrawable(mAppendingFile);
            enableTweetButton(true);
            setRemainLength();
        }
    }

    private void setAppendingImageDrawable(File appendingFile) {
        final int BITMAP_EDGE_LENGTH = 96;
        Bitmap bm = BitmapUtil.getResizedBitmap(appendingFile, BITMAP_EDGE_LENGTH);
        mAppendedImageView.setImageBitmap(BitmapUtil.rotateImage(bm, appendingFile.toString()));
        mAppendedImageView.setVisibility(View.VISIBLE);
    }

    private void updateStatus() {
        mInputText.clearFocus();
        StatusUpdate update = new StatusUpdate(mInputText.getText().toString());
        ParallelTask<StatusUpdate, Void, Status> task = new ParallelTask<StatusUpdate, Void, Status>() {
            @Override
            protected twitter4j.Status doInBackground(StatusUpdate... params) {
                try {
                    // 画像が指定されていたら添付
                    if (mAppendingFile != null) {
                        params[0].media(mAppendingFile);
                    }
                    return TwitterUtils.getTwitterInstance().updateStatus(params[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(twitter4j.Status result) {
                if (result != null) {
                    AppUtil.showToast("つぶやきました");
                    mInputText.setText("");
                    if (mAppendingFile != null) {
                        removeAppendedImage();
                    }
                } else {
                    AppUtil.showToast(R.string.impossible);
                }
            }
        };
        task.executeParallel(update);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.appendPic:
                pickOrTakePicture();
                break;
            case R.id.tweetInfoBtn:
                TweetInfoDialogFragment dialog = new TweetInfoDialogFragment();
                Bundle b = new Bundle();
                Status targetStatus = getTargetStatus();
                b.putSerializable(C.STATUS, targetStatus);
                dialog.setArguments(b);
                dialog.show(getFragmentManager(), "StatusInfo");
                break;

            case R.id.action_tweet:
                updateStatus();
                break;

            case R.id.appendedImage:
                registerForContextMenu(mAppendedImageView);
                getActivity().openContextMenu(v);
                unregisterForContextMenu(mAppendedImageView);
                break;
        }
    }

    private void pickOrTakePicture() {

        //take picture intent
        String filename = System.currentTimeMillis() + ".jpg";
        // コンテントプロバイダを使用し,ギャラリーに画像を保存. 保存したUriを取得.
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        mImageUri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        final List<Intent> cameraIntents = new ArrayList<>();
        final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager pm = getActivity().getPackageManager();
        final List<ResolveInfo> cameraList = pm.queryIntentActivities(cameraIntent, 0);
        for (ResolveInfo info : cameraList) {
            final Intent intent = new Intent(cameraIntent);
            intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
            intent.setPackage(info.activityInfo.packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            cameraIntents.add(intent);
        }

        //select picture intent
        final Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");

        String pickTitle = getString(R.string.choose_or_take_picture);
        Intent chooserIntent = Intent.createChooser(galleryIntent, pickTitle);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

        startActivityForResult(chooserIntent, SELECT_PICTURE);
    }

    /**
     * reply先のStatusを取得する
     *
     * @return
     */
    private Status getTargetStatus() {
        return (Status) getArguments().getSerializable(C.STATUS);
    }

    /**
     * 添付画像用の一時ファイルをクリア．
     */
    private void clearTemporaryImageFile() {
        if (mAppendingFile != null && mAppendingFile.exists()) {
            mAppendingFile.delete();
        }
        mAppendingFile = null;
    }

    public void show(){
        if(mRootView == null) return;
        mRootView.setVisibility(View.VISIBLE);
    }

    public void hide(){
        if (mRootView == null) return;
        mRootView.setVisibility(View.GONE);
    }
}
