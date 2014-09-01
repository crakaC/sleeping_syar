package com.crakac.ofuton.status;

import java.util.ArrayList;
import java.util.List;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import com.crakac.ofuton.status.action.CancelRetweetAction;
import com.crakac.ofuton.status.action.ClickAction;
import com.crakac.ofuton.status.action.ConversationAction;
import com.crakac.ofuton.status.action.DestroyStatusAction;
import com.crakac.ofuton.status.action.FavAction;
import com.crakac.ofuton.status.action.HashtagAction;
import com.crakac.ofuton.status.action.LinkAction;
import com.crakac.ofuton.status.action.MediaAction;
import com.crakac.ofuton.status.action.ReplyAction;
import com.crakac.ofuton.status.action.ReplyAllAction;
import com.crakac.ofuton.status.action.RetweetAction;
import com.crakac.ofuton.status.action.TofuBusterAction;
import com.crakac.ofuton.status.action.UserDetailAction;
import com.crakac.ofuton.timeline.AbstractStatusFragment;
import com.crakac.ofuton.util.TwitterUtils;

/**
 * ツイートをタップした時に出てくるダイアログ
 *
 * @author Kosuke
 *
 */
@SuppressLint("ValidFragment")
public class StatusDialogFragment extends DialogFragment {

	private ClickActionAdapter mActionAdapter;
	private Dialog mDialog;
	private Status mSelectedStatus;

	public StatusDialogFragment() {
	}

	public StatusDialogFragment(AbstractStatusFragment targetFragment) {
		setTargetFragment((Fragment) targetFragment, 0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        mSelectedStatus = (Status) getArguments().getSerializable(C.STATUS);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.status_dialog, container);
		// 各種アクションをアダプタに追加して表示
		mActionAdapter = new ClickActionAdapter(getActivity());

		// リストビューを作成
		// ツイートが縦に長いと全画面分の領域を使ってしまい，アクションを選択できなくなる
		ListView lvActions = (ListView) view
				.findViewById(R.id.status_action_list);

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
				mDialog.dismiss();
				item.doAction();
			}
		});
		return view;
	}

	private boolean isMyTweet(Status status) {
		if (status.getUser().getId() == TwitterUtils.getCurrentAccountId()) {
			return true;
		}
		return false;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mDialog = getDialog();

		WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();

		DisplayMetrics metrics = getResources().getDisplayMetrics();

		// 縦幅はwrap contentで，横幅は92%で．
		int dialogWidth = (int) (metrics.widthPixels * 0.92);
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
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

		// 背景を透明に
		mDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));

		return mDialog;
	}

	private void setActions(){
		AbstractStatusFragment targetFragment = (AbstractStatusFragment) getTargetFragment();
		TweetStatusAdapter statusAdapter = targetFragment.getAdapter();
		// 自分のツイートならdestroyアクションを追加
		if (isMyTweet(mSelectedStatus)
				&& !mSelectedStatus.isRetweet()
				|| (mSelectedStatus.isRetweet() && isMyTweet(mSelectedStatus
						.getRetweetedStatus()))) {
			mActionAdapter.add(new DestroyStatusAction(getActivity(),statusAdapter,mSelectedStatus));
		}

		// reply
		mActionAdapter.add(new ReplyAction(getActivity(),mSelectedStatus));

		// reply all
		UserMentionEntity[] entities = mSelectedStatus.isRetweet() ?
				mSelectedStatus.getRetweetedStatus().getUserMentionEntities() :
				mSelectedStatus.getUserMentionEntities();

		if ( (!isMyTweet(mSelectedStatus) && entities.length != 0)//自分のツイートでなく，誰かしらへリプライを飛ばしている
			&& (!(entities.length == 1 && (
					entities[0].getId() == TwitterUtils.getCurrentAccountId() || //自分だけへのリプライではない
					entities[0].getId() == ( mSelectedStatus.isRetweet() ? mSelectedStatus.getRetweetedStatus().getUser().getId() : mSelectedStatus.getUser().getId() )
					)))
			)
		{
			mActionAdapter.add(new ReplyAllAction(getActivity(),mSelectedStatus));
		}
		// favorite
		mActionAdapter.add(new FavAction(getActivity(), statusAdapter,mSelectedStatus));

		//cancel Retweet
		// 自分がリツイートしたやつはリツイートを取り消せる
		if (mSelectedStatus.isRetweet()
				&& mSelectedStatus.getUser().getId() == TwitterUtils
						.getCurrentAccountId()) {
			mActionAdapter.add(new CancelRetweetAction(getActivity()
					.getApplicationContext(), statusAdapter,mSelectedStatus));
		}
		// retweet 自分のツイートでなく，他人が自分のツイートをリツイートしたものでないときはリツイートできる
		else if (!isMyTweet(mSelectedStatus)//自分のツイートでなく
				&& !(mSelectedStatus.isRetweet() && isMyTweet(mSelectedStatus.getRetweetedStatus()))//自分のツイートをリツイートしたものでもなく
				&& !(mSelectedStatus.getUser().isProtected() && !mSelectedStatus.isRetweet())//鍵垢のツイート(RT除く)でない
				){
			mActionAdapter.add(new RetweetAction(getActivity()
					.getApplicationContext(), statusAdapter,mSelectedStatus));
		}

		// conversation　
		if ((mSelectedStatus.isRetweet() && (mSelectedStatus.getRetweetedStatus()
				.getInReplyToScreenName() != null))
				|| mSelectedStatus.getInReplyToScreenName() != null) {
			mActionAdapter.add(new ConversationAction(getActivity(),mSelectedStatus));
		}

		//TofuBuster
		//TODO 未インストール時は表示しない
		mActionAdapter.add(new TofuBusterAction(getActivity(), mSelectedStatus));

		setUserEntities(mSelectedStatus);
		setUrlEntities(mSelectedStatus);
		setMediaEntities(mSelectedStatus);
		setHashtagEntities(mSelectedStatus);

		mActionAdapter.notifyDataSetChanged();
	}

	private void setUserEntities(Status status){
        List<String> users = new ArrayList<String>();// statusに関係あるscreenNameをかたっぱしから突っ込む(@抜き)
        UserMentionEntity[] userMentionEntities = status.getUserMentionEntities();
        for (UserMentionEntity user : userMentionEntities) {
            users.add(user.getScreenName());
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
        users.add(status.getUser().getScreenName());// ツイートまたはリツイートした人は一番下に置きたい

        for (String user : users) {
            if (user.equals(status.getUser().getScreenName())) {
                mActionAdapter.add(new UserDetailAction(getActivity(), status));
            } else {
                mActionAdapter.add(new UserDetailAction(getActivity(), user));
            }
        }
	}

	private void setUrlEntities(Status status){
        URLEntity[] urlEntities;
        ArrayList<String> urls = new ArrayList<String>();
        if (status.isRetweet()) {
            urlEntities = status.getRetweetedStatus().getURLEntities();
        } else {
            urlEntities = status.getURLEntities();
        }
        for (URLEntity url : urlEntities) {
            mActionAdapter.add(new LinkAction(getActivity(), url
                    .getExpandedURL()));
            urls.add(url.getExpandedURL());
            Log.d("URLEntity", url.getExpandedURL());
        }
	}

	private void setMediaEntities(Status status){
        MediaEntity[] mediaEntities;
        if (status.isRetweet()) {
            mediaEntities = status.getRetweetedStatus().getExtendedMediaEntities();
        } else {
            mediaEntities = status.getExtendedMediaEntities();
        }

        for (MediaEntity media : mediaEntities) {
            mActionAdapter.add(new MediaAction(getActivity(), media
                    .getExpandedURL(), media
                    .getMediaURL()));
            Log.d("MediaEntity", media.getExpandedURL());
        }
	}

	private void setHashtagEntities(Status status){
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
}
