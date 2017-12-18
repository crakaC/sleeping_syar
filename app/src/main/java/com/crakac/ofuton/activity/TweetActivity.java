package com.crakac.ofuton.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.BitmapUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.SimpleTextChangeListener;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.util.Util;
import com.crakac.ofuton.widget.ColorOverlayOnTouch;

import java.io.File;
import java.io.IOException;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
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
    private static final int MAX_APPEND_PICTURE_EDGE_LENGTH = 1024;
    private static final String IMAGE_URI = "IMAGE_URI";

    private EditText mInputText;
    private File mAppendingFile; // 画像のアップロードに使用
    private View mTweetBtn, mAppendPicBtn, mCameraBtn, mInfoBtn;// つぶやくボタン，画像追加ボタン，リプライ元情報ボタン
    private ImageView mAppendedImageView;
    private Uri mImageUri;// カメラ画像添付用
    private long mReplyId;// reply先ID
    private String mReplyName;// リプライ先スクリーンネーム
    private String mHashTag;// ハッシュタグ
    private User mMentionUser;
    private boolean mIsUpdatingStatus = false;//ツイート中かどうか。onDestroyで添付ファイルを削除する際の判定に使う。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // レイアウトを読み込み
        setContentView(R.layout.activity_tweet);

        mInputText = (EditText) findViewById(R.id.input_text);// 入力欄
        mTweetBtn = findViewById(R.id.action_tweet);// ツイートボタン
        mAppendPicBtn = findViewById(R.id.appendPic);// 画像添付ボダン
        mCameraBtn = findViewById(R.id.picFromCamera);// 撮影して添付するボタン
        mInfoBtn = findViewById(R.id.tweetInfoBtn);// リプライ先表示ボタン
        mAppendedImageView = (ImageView) findViewById(R.id.appendedImage);

        for (View clickable : new View[]{mTweetBtn, mAppendPicBtn, mCameraBtn, mInfoBtn, mAppendedImageView}) {
            clickable.setOnClickListener(this);
        }

        mAppendedImageView.setOnTouchListener(new ColorOverlayOnTouch());

        // 文章に変更があったら残り文字数を変化させる
        mInputText.addTextChangedListener(new SimpleTextChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                setRemainLength();
            }
        });

        setReplyData();

        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            handleSendIntent(intent);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            handleViewIntent(intent);
        }

        // アクティビティ開始時の残り文字数をセットする．リプライ時やハッシュタグ時のときも140字にならないために．
        setRemainLength();
    }

    private void handleSendIntent(Intent intent) {
        Bundle b = intent.getExtras();
        if (b != null) {
            String text = b.getString(Intent.EXTRA_TEXT);
            if (text != null) {
                mInputText.setText(text);
                mInputText.setSelection(text.length());
            }
            Uri imageUri = (Uri) b.get(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                appendPicture(imageUri);
            }
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
            mInputText.setText("@" + mReplyName + " ");
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

    private final static int PREVIEW_APPENDED_IMAGE = 1;
    private final static int REMOVE_APPENDED_IMAGE = 2;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
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
                return false;
        }
    }

    private void previewAppendedImage() {
        Intent i = new Intent(this, PhotoPreviewActivity.class);
        i.putExtra(C.FILE, mAppendingFile);
        startActivity(i);
    }

    private void removeAppendedImage() {
        mAppendedImageView.setVisibility(View.GONE);
        clearTemporaryImageFile();
        setRemainLength();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_URI, mImageUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageUri = savedInstanceState.getParcelable(IMAGE_URI);
        Log.d(TAG, "onRestore");
    }

    @Override
    protected void onDestroy() {
        //ツイートボタン押下時にfinishする仕様だとツイート終了前にonDestroyが走るのでフラグで判定する
        if (!mIsUpdatingStatus) {
            clearTemporaryImageFile();
        }
        super.onDestroy();
    }

    /**
     * ツイートの残り文字数を求め，テキストビューに反映する
     * <p/>
     * 編集中のEditText
     */
    private void setRemainLength() {
        String text = mInputText.getEditableText().toString();
        int remainLength = MAX_TWEET_LENGTH - Util.getActualTextLength(text);
        if (mAppendingFile != null) {
            remainLength -= TwitterUtils.getApiConfiguration().getCharactersReservedPerMedia();
        }
        if (remainLength < 0 || (remainLength == MAX_TWEET_LENGTH && mAppendingFile == null)) {
            enableTweetButton(false);
        } else {
            enableTweetButton(true);
        }
        getSupportActionBar().setSubtitle(String.valueOf(remainLength));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (resultCode == RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
            }
            if (uri == null) {
                uri = mImageUri;
            }
            appendPicture(uri);
        } else if (requestCode == REQUEST_CAMERA) {
            // ContentResolverでファイルを登録してあるので削除する。しないとゴミが出る。
            getContentResolver().delete(mImageUri, null, null);
        }
    }

    private void enableTweetButton(boolean enabled) {
        mTweetBtn.setEnabled(enabled);
    }

    private void appendPicture(Uri uri) {
        clearTemporaryImageFile();
        File imageFile = AppUtil.convertUriToFile(this, uri);
        try {
            mAppendingFile = BitmapUtil.createTemporaryResizedImage(imageFile, MAX_APPEND_PICTURE_EDGE_LENGTH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mAppendingFile == null) {
            AppUtil.showToast("ファイルの読み込みに失敗しました");
        } else {
            setAppendingImagePreview(mAppendingFile);
            enableTweetButton(true);
            setRemainLength();
        }
    }

    private void setAppendingImagePreview(File appendingFile) {
        final int BITMAP_EDGE_LENGTH = 128;
        Bitmap bm = BitmapUtil.getResizedBitmap(appendingFile, BITMAP_EDGE_LENGTH);
        mAppendedImageView.setImageBitmap(BitmapUtil.rotateImage(bm, appendingFile.toString()));
        mAppendedImageView.setVisibility(View.VISIBLE);
    }

    private void updateStatus() {
        mIsUpdatingStatus = true;
        final StatusUpdate update = new StatusUpdate(mInputText.getText().toString());
        if (mReplyId > 0) {
            update.setInReplyToStatusId(mReplyId);
        }
        ParallelTask<Void, Status> task = new ParallelTask<Void, Status>() {
            @Override
            protected twitter4j.Status doInBackground() {
                try {
                    // 画像が指定されていたら添付
                    if (mAppendingFile != null) {
                        update.media(mAppendingFile);
                    }
                    TwitterUtils.checkUpdateName(update.getStatus());
                    return TwitterUtils.getTwitterInstance().updateStatus(update);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(twitter4j.Status result) {
                AppUtil.showToast(result != null ? "つぶやきました" : "ツイート失敗");
                if (mAppendingFile != null) {
                    mAppendingFile.delete();
                }
                mIsUpdatingStatus = false;
            }
        };
        task.executeParallel();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.appendPic:
                // 画像添付ボタン押下時の動作
                // 画像を選びに行く．画像を選んだらonActivityResultが呼ばれる
                if(!Util.checkRuntimePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, EXTERNAL_STORAGE_PERMISSION_REQUEST))
                    return;
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_SELECT_PICTURE);
                break;
            case R.id.picFromCamera:
                if(!Util.checkRuntimePermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST))
                    return;

                String filename = System.currentTimeMillis() + ".jpg";
                // コンテントプロバイダを使用し,ギャラリーに画像を保存. 保存したUriを取得.
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
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
                finish();
                break;

            case R.id.appendedImage:
                registerForContextMenu(mAppendedImageView);
                openContextMenu(v);
                unregisterForContextMenu(mAppendedImageView);
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

    /**
     * 画像添付用に縮小画像を生成するので、添付し直し時、アクティビティ終了時にファイルを削除する。
     */
    private void clearTemporaryImageFile() {
        if (mAppendingFile != null && mAppendingFile.exists()) {
            mAppendingFile.delete();
            mAppendingFile = null;
        }
    }
}
