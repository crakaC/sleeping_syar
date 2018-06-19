package com.crakac.ofuton.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.ImagePreviewActivity;
import com.crakac.ofuton.fragment.dialog.TweetInfoDialogFragment;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.BitmapUtil;
import com.crakac.ofuton.util.GlideApp;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.util.Util;
import com.crakac.ofuton.widget.ColorOverlayOnTouch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.StatusUpdate;
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

    private EditText mInputText;
    private TextView mRemainingText;// 残り文字数を表示
    private ImageView mTweetBtn, mAppendBtn;// つぶやくボタン，画像追加ボタン，リプライ元情報ボタン
    private ImageView mAppendedImageView;
    private Uri mImageUri;// 画像添付用
    private View mRootView;

    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_tweet, null);
        mRootView.setOnClickListener(this);
        mInputText = mRootView.findViewById(R.id.input_text);// 入力欄
        mTweetBtn = mRootView.findViewById(R.id.action_tweet);// ツイートボタン
        mAppendBtn = mRootView.findViewById(R.id.appendPic);// 画像添付ボダン
        mRemainingText = mRootView.findViewById(R.id.remainingText);// 残り文字数
        mAppendedImageView = mRootView.findViewById(R.id.appendedImage);

        // 画像添付ボタンタップの動作
        mAppendBtn.setOnClickListener(this);
        // 添付画像ミニプレビュータップの動作
        mAppendedImageView.setOnClickListener(this);

        if(Util.isPreLollipop()) {
            mAppendedImageView.setOnTouchListener(new ColorOverlayOnTouch());
        }

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
            mImageUri = savedInstanceState.getParcelable(IMAGE_URI);
            if(mImageUri != null) {
                appendPicture(mImageUri);
            }
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
        Intent i = new Intent(getActivity(), ImagePreviewActivity.class);
        i.putExtra(C.ATTACHMENTS, new ArrayList<Uri>(){{add(mImageUri);}});
        startActivity(i);
    }

    private void removeAppendedImage() {
        mAppendedImageView.setVisibility(View.GONE);
        mImageUri = null;
        setRemainLength();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_URI, mImageUri);
    }

    /**
     * ツイートの残り文字数を求め，テキストビューに反映する
     * <p/>
     * 編集中のEditText
     */
    private void setRemainLength() {
        String text = mInputText.getEditableText().toString();
        int remainLength = MAX_TWEET_LENGTH - Util.getActualTextLength(text);
        boolean hasValidContent = !(remainLength < 0 || text.isEmpty() && mImageUri == null);
        enableTweetButton(hasValidContent);
        mRemainingText.setText(String.valueOf(remainLength));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (resultCode != Activity.RESULT_OK || requestCode != SELECT_PICTURE) {
            getActivity().getContentResolver().delete(mImageUri, null, null);
            return;
        }

        Uri uri = data.getData();
        if (uri == null) {
            uri = mImageUri;
        } else {
            mImageUri = uri;
        }
        appendPicture(uri);
    }

    private void enableTweetButton(boolean enabled) {
        mTweetBtn.setEnabled(enabled);
    }

    private void appendPicture(Uri uri) {
        GlideApp.with(this).load(uri).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                enableTweetButton(true);
                setRemainLength();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                enableTweetButton(true);
                setRemainLength();
                return false;
            }
        }).into(mAppendedImageView);
        mAppendedImageView.setVisibility(View.VISIBLE);
    }

    private void updateStatus() {
        mInputText.clearFocus();
        enableTweetButton(false);
        final StatusUpdate update = new StatusUpdate(mInputText.getText().toString());
        ParallelTask<Void, Status> task = new ParallelTask<Void, Status>() {
            @Override
            protected twitter4j.Status doInBackground() {
                try {
                    // 画像が指定されていたら添付
                    if (mImageUri != null) {
                        update.media(BitmapUtil.createTemporaryResizedImage(getContext().getContentResolver(), mImageUri, MAX_APPEND_PICTURE_EDGE_LENGTH));
                    }
                    return TwitterUtils.getTwitterInstance().updateStatus(update);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(twitter4j.Status result) {
                if (result != null) {
                    AppUtil.showToast(R.string.tweeted);
                    clear();
                } else {
                    AppUtil.showToast(R.string.impossible);
                    enableTweetButton(true);
                }
                mInputText.requestFocus();
            }
        };
        task.executeParallel();
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
        if (!Util.checkRuntimePermissions(getActivity(), REQUIRED_PERMISSIONS, 1))
            return;

        //take picture intent
        File imageFile = AppUtil.createImageFile();
        mImageUri = AppUtil.fileToContentUri(imageFile);
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

    public void show() {
        if (mRootView == null) return;
        mRootView.setVisibility(View.VISIBLE);
        mInputText.requestFocus();
        showKeyboard();
    }

    private void showKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(
                mInputText.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED, 0);
    }

    public void hide() {
        if (mRootView == null) return;
        mRootView.setVisibility(View.GONE);
        clear();
    }

    public void clear() {
        mInputText.setText("");
        removeAppendedImage();
        setRemainLength();
    }
}
