package com.crakac.ofuton.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.fragment.dialog.DmInfoDialogFragment;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class ComposeDmActivity extends FinishableActionbarActivity {
    private EditText mInputText;
    private Twitter mTwitter;
    private TextView remaining;// 残り文字数を表示
    private View sendBtn, infoBtn;// 送信ボタン，DM情報ボタン
    private String screenName;// リプライ先スクリーンネーム
    private DirectMessage replyDM;
    private ActionBar mActionbar;

    private final int MAX_TEXT_LENGTH = TwitterUtils.getApiConfiguration().getDmTextCharacterLimit();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // レイアウトとタイトルの設定
        setContentView(R.layout.activity_compose_dm);

        Intent intent = getIntent();
        replyDM = (DirectMessage) intent.getSerializableExtra(C.DM);
        twitter4j.User user = (twitter4j.User) intent.getSerializableExtra(C.USER);
        screenName = user.getScreenName();
        String name = user.getName();

        mTwitter = TwitterUtils.getTwitterInstance();// Twitter周りのやつ
        mInputText = findViewById(R.id.input_text);// 入力欄
        sendBtn = findViewById(R.id.sendBtn);// ツイートボタン
        infoBtn = findViewById(R.id.dmInfoBtn);// リプライ先表示ボタン
        remaining = findViewById(R.id.remainingText);// 残り文字数

        mActionbar = getSupportActionBar();
        mActionbar.setSubtitle("DM to " + name + " @" + screenName);

        //DMへの返信だった場合は返信元を表示できるようにする
        if (replyDM != null) {
            infoBtn.setVisibility(View.VISIBLE);
            infoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DmInfoDialogFragment dialog = new DmInfoDialogFragment();
                    Bundle b = new Bundle();
                    b.putSerializable(C.DM, replyDM);
                    dialog.setArguments(b);
                    dialog.show(getSupportFragmentManager(), "DM info");
                }
            });
        }
        // sendボタンの動作
        sendBtn.setEnabled(false);// 初期状態では不可にしておく
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDM();
                finish();
            }
        });

        // 文章に変更があったら残り文字数を変化させる
        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setRemainLength(s);
            }
        });
        // アクティビティ開始時の残り文字数をセットする．
        setRemainLength(mInputText.getEditableText());
    }

    private void setRemainLength(Editable s) {
        int remainLength = MAX_TEXT_LENGTH - s.length();
        if (remainLength < 0 || remainLength == MAX_TEXT_LENGTH) {
            sendBtn.setEnabled(false);
        } else {
            sendBtn.setEnabled(true);
        }
        remaining.setText(String.valueOf(remainLength));
    }

    private void sendDM() {
        final String text = mInputText.getText().toString();
        new SendDmTask(mTwitter, screenName, text).executeParallel();
    }

    static class SendDmTask extends ParallelTask<Void, DirectMessage>{
        private Twitter mTwitter;
        private String screenName;
        private String text;
        SendDmTask(Twitter twitter, String screenName, String text){
            this.mTwitter = twitter;
            this.screenName = screenName;
            this.text = text;
        }

        @Override
        protected DirectMessage doInBackground() {
            try {
                return mTwitter.sendDirectMessage(screenName, text);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(DirectMessage result) {
            if (result == null) {
                AppUtil.showToast("無理でした");
            } else {
                AppUtil.showToast("ダイレクトメッセージを送信しました");
            }
        }
    };
}