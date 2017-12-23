package com.crakac.ofuton.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.action.ClickActionAdapter;
import com.crakac.ofuton.action.status.CancelRetweetAction;
import com.crakac.ofuton.action.status.ClickAction;
import com.crakac.ofuton.action.status.ConversationAction;
import com.crakac.ofuton.action.status.DestroyStatusAction;
import com.crakac.ofuton.action.status.FavAction;
import com.crakac.ofuton.action.status.FavAndRetweeAction;
import com.crakac.ofuton.action.status.HashtagAction;
import com.crakac.ofuton.action.status.LinkAction;
import com.crakac.ofuton.action.status.MediaAction;
import com.crakac.ofuton.action.status.ReplyAction;
import com.crakac.ofuton.action.status.ReplyAllAction;
import com.crakac.ofuton.action.status.RetweetAction;
import com.crakac.ofuton.action.status.TofuBusterAction;
import com.crakac.ofuton.action.status.UserDetailAction;
import com.crakac.ofuton.adapter.TweetStatusAdapter;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.PrefUtil;
import com.crakac.ofuton.util.TwitterUtils;

import java.util.ArrayList;
import java.util.List;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

import static com.crakac.ofuton.util.TwitterUtils.isMyTweet;

/**
 * ツイートをタップした時に出てくるダイアログ
 *
 * @author Kosuke
 */
@SuppressLint("ValidFragment")
public class StatusDialogFragment extends DialogFragment {

    private ClickActionAdapter mActionAdapter;
    private Dialog mDialog;
    private Status mSelectedStatus;
    private boolean isEnablePreview;

    public StatusDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedStatus = (Status) getArguments().getSerializable(C.STATUS);
        isEnablePreview = PrefUtil.getBoolean(R.string.show_image_in_timeline, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_actions, container);
        // 各種アクションをアダプタに追加して表示
        mActionAdapter = new ClickActionAdapter(getActivity());

        // リストビューを作成
        // ツイートが縦に長いと全画面分の領域を使ってしまい，アクションを選択できなくなる
        ListView lvActions = view.findViewById(R.id.action_list);

        // ステータス表示部分を作成．タイムライン中と同じレイアウトなのでTweetStatusAdapter内の処理を使いまわす．
        View statusView = TweetStatusAdapter.createView(
                mSelectedStatus, null);
        statusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        // HeaderView（ツイート表示用）をadd. setAdapterより先にしないと落ちる
        lvActions.addHeaderView(statusView);
        // アダプタをセット
        lvActions.setAdapter(mActionAdapter);
        setActions();

        lvActions.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ListView lv = (ListView) parent;
                ClickAction item = (ClickAction) lv
                        .getItemAtPosition(position);
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                item.doAction();
            }
        });
        return view;
    }

    private boolean isLockedAccountTweet(Status status) {
        return status.getUser().isProtected();
    }

    private boolean isRetweetable(Status status) {
        return (!isLockedAccountTweet(status) || status.isRetweet()) && !status.isRetweeted();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDialog = getDialog();

        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        // 縦幅はwrap contentで，横幅は85%で．
        int dialogWidth = (int) Math.min((metrics.widthPixels * 0.85), AppUtil.dpToPx(480));
        int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;

        lp.width = dialogWidth;
        lp.height = dialogHeight;
        mDialog.getWindow().setAttributes(lp);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialog = new Dialog(getActivity());
        // タイトル部分を消す．消さないとダイアログの表示位置が下にずれる
        mDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // レイアウトはonCreateViewで作られる．ので，dialog.setContentViewはいらない

        // 全画面化
        mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 背景を透明に
        mDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        return mDialog;
    }

    private void setActions() {

        Status retweetedStatus = mSelectedStatus.getRetweetedStatus();
        // 自分のツイートならdestroyアクションを追加
        if (PrefUtil.getBoolean(R.string.show_destroy, true) &&
                ((isMyTweet(mSelectedStatus) && !mSelectedStatus.isRetweet()) || isMyTweet(retweetedStatus))) {
            mActionAdapter.add(new DestroyStatusAction(getActivity(), mSelectedStatus));
        }

        // reply
        if (PrefUtil.getBoolean(R.string.show_reply, true)) {
            mActionAdapter.add(new ReplyAction(getActivity(), mSelectedStatus));
        }

        // reply all
        if (PrefUtil.getBoolean(R.string.show_reply_all, true)) {
            UserMentionEntity[] entities = mSelectedStatus.isRetweet() ? retweetedStatus.getUserMentionEntities() : mSelectedStatus.getUserMentionEntities();

            if ((!isMyTweet(mSelectedStatus) && entities.length > 0)//自分のツイートでなく，誰かしらへリプライを飛ばしている
                    && (!(entities.length == 1 && (entities[0].getId() == TwitterUtils.getCurrentAccountId() //自分だけへのリプライではない
                    || entities[0].getId() == (mSelectedStatus.isRetweet() ? retweetedStatus.getUser().getId() : mSelectedStatus.getUser().getId())
            )))) {
                mActionAdapter.add(new ReplyAllAction(getActivity(), mSelectedStatus));
            }
        }

        // conversation
        if ((mSelectedStatus.isRetweet() && (retweetedStatus.getInReplyToScreenName() != null))
                || mSelectedStatus.getInReplyToStatusId() > 0) {
            mActionAdapter.add(new ConversationAction(getActivity(), mSelectedStatus));
        }

        // favorite
        if(PrefUtil.getBoolean(R.string.show_favorite_key, true)){
            mActionAdapter.add(new FavAction(getActivity(), mSelectedStatus));
        }

        //cancel Retweet
        // 自分がリツイートしたやつはリツイートを取り消せる
        if (PrefUtil.getBoolean(R.string.show_retweet_key, true)) {
            if (mSelectedStatus.isRetweeted() ||
                    (mSelectedStatus.isRetweet() && retweetedStatus.isRetweeted())) {
                mActionAdapter.add(new CancelRetweetAction(getActivity(), mSelectedStatus));
            }
            // retweet //鍵垢のツイートでない(鍵垢のRTは元のツイートをRT出来る)
            else if (isRetweetable(mSelectedStatus)) {
                mActionAdapter.add(new RetweetAction(getActivity(), mSelectedStatus));
            }
        }

        // Fav & Retweet
        if (PrefUtil.getBoolean(R.string.show_fav_and_retweet, true) && isRetweetable(mSelectedStatus) && !mSelectedStatus.isFavorited()) {
            mActionAdapter.add(new FavAndRetweeAction(getContext(), mSelectedStatus));
        }

        //TofuBuster
        if (AppUtil.existsTofuBuster()) {
            mActionAdapter.add(new TofuBusterAction(getActivity(), mSelectedStatus));
        }

        setUserEntities(mSelectedStatus);
        setUrlEntities(mSelectedStatus);

        // インラインプレビューOFFのときは、ダイアログにURLを表示する
        if (!isEnablePreview) {
            setMediaEntities(mSelectedStatus);
        }

        if(PrefUtil.getBoolean(R.string.show_hashtag, true)){
            setHashtagEntities(mSelectedStatus);
        }

        mActionAdapter.notifyDataSetChanged();
    }

    private void setUserEntities(Status status) {
        List<String> users = new ArrayList<>();// statusに関係あるscreenNameをかたっぱしから突っ込む(@抜き)

        if(PrefUtil.getBoolean(R.string.show_accounts_in_tweet, true)) {
            UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
            for (UserMentionEntity user : userMentionEntities) {
                if (!users.contains(user.getScreenName())) {
                    users.add(user.getScreenName());
                }
            }

            // リツイートの場合，オリジナルの方でないと省略される可能性があるのでretweetedStatusからも引っ張ってくる
            if (status.isRetweet()) {
                Status rtStatus = status.getRetweetedStatus();
                UserMentionEntity[] umEntities = rtStatus.getUserMentionEntities();
                for (UserMentionEntity user : umEntities) {
                    if (!users.contains(user.getScreenName()))
                        users.add(user.getScreenName());
                }
            }
            if (users.contains(status.getUser().getScreenName())) {
                // リツイート内でリツイートしたユーザーのスクリーンネームが含まれていた場合呼ばれる
                users.remove(status.getUser().getScreenName());
            }
        } else if(status.isRetweet()){
            users.add(status.getRetweetedStatus().getUser().getScreenName());
        }
        users.add(status.getUser().getScreenName());// ツイートまたはリツイートした人は一番下に置きたい

        for (String user : users) {
            if (user.equals(status.getUser().getScreenName())) {
                mActionAdapter.add(new UserDetailAction(getActivity(), status));
            } else {
                mActionAdapter.add(new UserDetailAction(getActivity(), user));
            }
        }
    }

    private void setUrlEntities(Status status) {
        URLEntity[] urlEntities;
        ArrayList<String> urls = new ArrayList<>();
        if (status.isRetweet()) {
            urlEntities = status.getRetweetedStatus().getURLEntities();
        } else {
            urlEntities = status.getURLEntities();
        }
        for (URLEntity url : urlEntities) {
            if (isEnablePreview && url.getDisplayURL().startsWith("pic.twitter.com/")) {
                continue;
            }
            mActionAdapter.add(new LinkAction(getActivity(), url
                    .getExpandedURL()));
            urls.add(url.getExpandedURL());
            Log.d("URLEntity", url.getExpandedURL());
        }
    }

    private void setMediaEntities(Status status) {
        MediaEntity[] mediaEntities;
        if (status.isRetweet()) {
            mediaEntities = status.getRetweetedStatus().getMediaEntities();
        } else {
            mediaEntities = status.getMediaEntities();
        }

        for (MediaEntity media : mediaEntities) {
            mActionAdapter.add(new MediaAction(getActivity(), media
                    .getExpandedURL(), media
                    .getMediaURL()));
        }

    }

    private void setHashtagEntities(Status status) {
        HashtagEntity[] hashtags;
        if (status.isRetweet()) {
            hashtags = status.getRetweetedStatus().getHashtagEntities();
        } else {
            hashtags = status.getHashtagEntities();
        }
        for (HashtagEntity hashtag : hashtags) {
            mActionAdapter.add(new HashtagAction(getActivity(), hashtag
                    .getText()));
        }
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        if (!isResumed()) return 0;
        return super.show(transaction, tag);
    }
}
