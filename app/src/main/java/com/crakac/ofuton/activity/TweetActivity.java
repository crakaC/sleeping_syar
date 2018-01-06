package com.crakac.ofuton.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.service.StatusUpdateService;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.CreateAppendingFileTask;
import com.crakac.ofuton.util.GlideApp;
import com.crakac.ofuton.util.PrefUtil;
import com.crakac.ofuton.util.SimpleTextChangeListener;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.util.Util;
import com.crakac.ofuton.widget.AppendedImageView;
import com.crakac.ofuton.widget.ColorOverlayOnTouch;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.User;
import twitter4j.UserMentionEntity;

/**
 * ツイート作成アクティビティ
 *
 * @author Kosuke
 */
public class TweetActivity extends FinishableActionbarActivity implements View.OnClickListener {

    private static final String TAG = TweetActivity.class.getSimpleName();

    private static final int REQUEST_SELECT_PICTURE = 1;
    private static final int REQUEST_CAMERA = 2;

    private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST = 11;
    private static final int CAMERA_PERMISSION_REQUEST = 12;

    private static final int MAX_TWEET_LENGTH = 140;
    private static final String IMAGE_URI = "IMAGE_URI";
    private static final int MAX_APPEND_FILES = 4;

    private EditText mInputText;
    private ImageView mAppendPicBtn, mCameraBtn, mInfoBtn;// つぶやくボタン，画像追加ボタン，リプライ元情報ボタン
    private Button mTweetBtn;
    private Uri mCameraUri;// カメラ画像添付用
    private long mReplyId;// reply先ID
    private String mReplyName;// リプライ先スクリーンネーム
    private String mHashTag;// ハッシュタグ
    private User mMentionUser;

    private LinearLayout mAppendedImageRoot;
    private ArrayList<Uri> mAppendedImages = new ArrayList<>(MAX_APPEND_FILES);
    private ArrayList<File> mAppendedFiles = new ArrayList<>(MAX_APPEND_FILES);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // レイアウトを読み込み
        setContentView(R.layout.activity_tweet);

        mInputText = findViewById(R.id.input_text);// 入力欄
        mTweetBtn = findViewById(R.id.action_tweet);// ツイートボタン
        mAppendPicBtn = findViewById(R.id.appendPic);// 画像添付ボダン
        mCameraBtn = findViewById(R.id.picFromCamera);// 撮影して添付するボタン
        mInfoBtn = findViewById(R.id.tweetInfoBtn);// リプライ先表示ボタン
        mAppendedImageRoot = findViewById(R.id.image_attachments_root);

        mInputText.setTextSize(PrefUtil.getLargeFontSize());

        for (View clickable : new View[]{mTweetBtn, mAppendPicBtn, mCameraBtn, mInfoBtn}) {
            clickable.setOnClickListener(this);
        }

        // 文章に変更があったら残り文字数を変化させる
        mInputText.addTextChangedListener(new SimpleTextChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                updateState();
            }
        });

        setReplyData();

        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            handleSendIntent(intent);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            handleViewIntent(intent);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
            handleSendMultipleIntent(intent);
        }

        // アクティビティ開始時の残り文字数をセットする．リプライ時やハッシュタグ時のときも140字にならないために．
        updateState();
    }

    private void handleSendIntent(Intent intent) {
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (text != null) {
            mInputText.setText(text);
            mInputText.setSelection(text.length());
        }
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            appendPicture(imageUri);
        }
    }

    private void handleSendMultipleIntent(Intent intent) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris == null) {
            return;
        }
        int num = Math.min(uris.size(), MAX_APPEND_FILES);
        for (int i = 0; i < num; i++) {
            appendPicture(uris.get(i));
        }
    }

    private void handleViewIntent(Intent intent) {
        Uri data = intent.getData();
        if (data == null) {
            AppUtil.showToast(R.string.something_wrong);
        } else {
            String sharedText = Util.parseSharedText(data);
            mInputText.setText(sharedText);
            mInputText.setSelection(sharedText.length());
        }
    }

    private void setReplyData() {
        final Intent intent = getIntent();// reply時に必要なデータとかハッシュタグとかが入ってる
        final ActionBar actionbar = getSupportActionBar();

        mReplyId = intent.getLongExtra(C.REPLY_ID, -1);// reply先ID
        mReplyName = intent.getStringExtra(C.SCREEN_NAME);// リプライ先スクリーンネーム
        mMentionUser = (User) intent.getSerializableExtra(C.USER);
        mHashTag = intent.getStringExtra(C.HASH_TAG);// ハッシュタグ

        // リプライ時は@screen_nameを事前に打ち込んでおき，リプライ先表示ボタンを有効にする
        if (mReplyName != null) {
            mInputText.setText(String.format(getString(R.string.reply_format), mReplyName));
            // カーソルの位置を@~の後に
            mInputText.setSelection(mInputText.getText().toString().length());
            Status targetStatus = getTargetStatus();
            if (targetStatus != null) {
                User user = targetStatus.getUser();
                actionbar.setSubtitle("Reply to " + user.getName());
                // マルチリプライ
                if (intent.getBooleanExtra(C.REPLY_ALL, false)) {
                    for (UserMentionEntity entity : targetStatus.getUserMentionEntities()) {
                        if (entity.getId() != TwitterUtils.getCurrentAccountId()
                                && !entity.getScreenName().equals(mReplyName)) {
                            mInputText.append("@" + entity.getScreenName() + " ");
                        }
                    }
                }
                // リプライ先のツイートを見れるように
                mInfoBtn.setVisibility(View.VISIBLE);
            }
        }

        // ハッシュタグがある場合は自前に打ち込んでおく
        if (mHashTag != null) {
            mInputText.setText(mInputText.getText().toString() + " " + mHashTag);
        }

    }

    private void removeAppendedImage(AppendedImageView v) {
        for(int i = 0; i < mAppendedImageRoot.getChildCount(); i++){
            if(mAppendedImageRoot.getChildAt(i).equals(v)){
                mAppendedImages.remove(i);
                mAppendedFiles.remove(i);
                break;
            }
        }
        mAppendedImageRoot.removeView(v);
        updateState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_URI, mCameraUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCameraUri = savedInstanceState.getParcelable(IMAGE_URI);
    }

    private void updateState() {
        String text = mInputText.getEditableText().toString();
        int remainLength = MAX_TWEET_LENGTH - Util.getActualTextLength(text);
        getSupportActionBar().setSubtitle(String.valueOf(remainLength));

        boolean invalidContent = remainLength < 0 || (text.isEmpty() && mAppendedFiles.isEmpty()) || (mAppendedFiles.size() != mAppendedImages.size());
        mTweetBtn.setEnabled(!invalidContent);

        boolean canAppend = mAppendedImages.size() < MAX_APPEND_FILES;
        AppUtil.setImageViewEnabled(canAppend, mAppendPicBtn, R.drawable.ic_insert_photo);
        AppUtil.setImageViewEnabled(canAppend, mCameraBtn, R.drawable.ic_camera);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CAMERA:
                appendPicture(mCameraUri);
                updateState();
                break;
            case REQUEST_SELECT_PICTURE:
                if (data != null) {
                    setUpThumbnails(ImagePicker.getImages(data));
                }
                updateState();
                break;
        }
    }

    private void appendPicture(Uri uri) {
        setUpThumbnail(uri);
        mAppendedImages.add(uri);
        updateState();
    }

    private void setUpThumbnails(List<Image> images) {
        for (Image image : images) {
            appendPicture(Uri.fromFile(new File(image.getPath())));
        }
    }

    private AppendedImageView inflateThumbnail() {
        final AppendedImageView iv = new AppendedImageView(this);
        mAppendedImageRoot.addView(iv);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            iv.getImageView().setOnTouchListener(new ColorOverlayOnTouch());
        }
        return iv;
    }

    private void setUpThumbnail(final Uri image) {
        final AppendedImageView view = inflateThumbnail();
        // set thumbnail
        view.setOnAppendedImageListener(new AppendedImageView.OnAppendedImageClickListener() {
            @Override
            public void onClickCancel() {
                removeAppendedImage(view);
            }

            @Override
            public void onClickThumbnail() {
                Intent i = new Intent(TweetActivity.this, ImagePreviewActivity.class);
                i.putExtra(C.POSITION, mAppendedImages.indexOf(image));
                i.putExtra(C.ATTACHMENTS, mAppendedImages);
                startActivity(i);
            }
        });
        new CreateAppendingFileTask(this, image, (file) ->{
            mAppendedFiles.add(file);
            updateState();
            GlideApp.with(getApplicationContext()).load(file).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    view.clearProgress();
                    return false;
                }
            }).into(view.getImageView());
        }).executeParallel();
    }

    private void updateStatus() {
        Intent i = new Intent(TweetActivity.this, StatusUpdateService.class);
        i.putExtra(C.TEXT, mInputText.getText().toString());
        if (mReplyId > 0) {
            i.putExtra(C.IN_REPLY_TO, mReplyId);
        }
        i.putExtra(C.ATTACHMENTS, mAppendedFiles);
        startService(i);
        finish();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        int clickedId = v.getId();
        switch (clickedId) {
            case R.id.appendPic:
                // 画像添付ボタン押下時の動作
                // 画像を選びに行く．画像を選んだらonActivityResultが呼ばれる
                if (!Util.checkRuntimePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, EXTERNAL_STORAGE_PERMISSION_REQUEST))
                    return;
                ImagePicker.create(this)
                        .folderMode(true)
                        .folderTitle(getString(R.string.select_image))
                        .limit(MAX_APPEND_FILES - mAppendedImages.size())
                        .showCamera(false)
                        .theme(R.style.ImagePicker)
                        .start(REQUEST_SELECT_PICTURE);
                break;
            case R.id.picFromCamera:
                if (!Util.checkRuntimePermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST))
                    return;
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) == null) {
                    AppUtil.showToast(R.string.no_camera);
                    return;
                }
                File photoFile = AppUtil.createImageFile();
                mCameraUri = FileProvider.getUriForFile(this, getString(R.string.file_provider_authority), photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
                startActivityForResult(intent, REQUEST_CAMERA);
                break;

            case R.id.tweetInfoBtn:
                Status targetStatus = getTargetStatus();
                intent = new Intent(this, ConversationActivity.class);
                intent.putExtra(C.STATUS, targetStatus);
                startActivity(intent);
                break;

            case R.id.action_tweet:
                updateStatus();
                break;
        }
    }

    /**
     * reply先のStatusを取得する
     *
     * @return
     */
    private Status getTargetStatus() {
        return (Status) getIntent().getSerializableExtra(C.STATUS);
    }
}
