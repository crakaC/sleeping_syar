package com.crakac.ofuton.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.UserDetailActivity;
import com.crakac.ofuton.util.Account;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.PrefUtil;
import com.crakac.ofuton.util.StatusClickListener;
import com.crakac.ofuton.util.StatusPool;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.widget.ColorOverlayOnTouch;
import com.crakac.ofuton.widget.MultipleImagePreview;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

public class TweetStatusAdapter extends ArrayAdapter<Status> {
    private static ArrayList<TweetStatusAdapter> sAdapters = new ArrayList<>(3);//fav, mention, home
    private static LayoutInflater sInflater;
    private static Account sUserAccount;
    private static boolean shouldShowPreview = false;
    private boolean mShouldPool = true;
    private StatusClickListener mListener;
    private static final String TAG = TweetStatusAdapter.class.getSimpleName();

    private static class ViewHolder {
        View base;
        TextView name;
        TextView screenName;
        TextView text;
        TextView postedAt;
        View retweeterInfo;
        TextView via;
        TextView retweetedBy;
        NetworkImageView icon;
        NetworkImageView smallIcon;
        ImageView favicon;
        MultipleImagePreview imagePreview;
        ImageView lockedIcon;
    }

    @Override
    public void add(Status status) {
        super.add(status);
        if (mShouldPool) {
            StatusPool.put(status.getId(), status);
        }
    }

    @Override
    public void insert(Status status, int index) {
        super.insert(status, index);
        if (mShouldPool) {
            StatusPool.put(status.getId(), status);
        }
    }

    public TweetStatusAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
        sInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        sUserAccount = TwitterUtils.getCurrentAccount();
        sAdapters.add(this);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final Status item = getItem(position);
        return createView(item, convertView);
    }

    public void updateDisplayTime(int position, View view) {
        if (view.getTag() == null) {
            return;// フッタービューは弾く．
        }
        setPostedAtTime((ViewHolder) view.getTag(), getItem(position));
    }

    public static View createView(final Status item, View convertView) {
        ViewHolder holder;

        // レイアウトの構築
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = sInflater.inflate(R.layout.list_item_tweet, null);
            setViewHolder(holder, convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setBasicInfo(holder, item);
        if (item.isRetweet()) {
            setRetweetView(holder, item);
        } else {
            setNormalTweetView(holder, item);
        }
        optimizeFontSize(holder);
        setColors(holder, item);
        // アイコンクリック時の挙動を設定．ユーザー詳細に飛ばす．
        holder.icon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, UserDetailActivity.class);
                intent.putExtra(C.USER, (item.isRetweet()) ? item.getRetweetedStatus().getUser() : item.getUser());
                context.startActivity(intent);
            }
        });

        setPostedAtTime(holder, item);

        return convertView;
    }

    private static void optimizeFontSize(ViewHolder holder) {
        // フォントサイズの調整
        int fontSize = PrefUtil.getFontSize();
        float subFontSize = PrefUtil.getSubFontSize();
        holder.name.setTextSize(fontSize);
        holder.screenName.setTextSize(subFontSize);
        holder.text.setTextSize(fontSize);
        holder.retweetedBy.setTextSize(subFontSize);
        holder.postedAt.setTextSize(subFontSize);
        holder.via.setTextSize(subFontSize);
    }

    private static void setBasicInfo(ViewHolder holder, Status status) {
        if (status.isRetweet())
            status = status.getRetweetedStatus();
        // ユーザー名＋スクリーンネーム
        holder.name.setText(status.getUser().getName());
        holder.screenName.setText("@" + status.getUser().getScreenName());

        String text = status.getText();
        if (shouldShowPreview) {
            text = AppUtil.trimUrl(status);
        }
        holder.text.setText(AppUtil.getColoredText(text, status));
        // アイコン
        setIcon(holder.icon, status);
        // 鍵アイコン
        setLockIcon(holder.lockedIcon, status);
        // ☆
        setFavIcon(holder.favicon, status);

        // inline preview
        if (text.length() == 0) {
            holder.text.setVisibility(View.GONE);
        } else {
            holder.text.setVisibility(View.VISIBLE);
        }
        setImagePreview(holder.imagePreview, status);
    }

    private static void setImagePreview(final MultipleImagePreview imagePreview, final Status status) {
        imagePreview.setVisibility(View.GONE);
        List<MediaEntity> mediaEntities = TwitterUtils.getMediaEntities(status);
        if (!shouldShowPreview || mediaEntities.isEmpty()) {
            return;
        }
        imagePreview.setVisibility(View.VISIBLE);
        imagePreview.cleanUp();
        imagePreview.setMediaEntities(mediaEntities);
    }

    /**
     * 背景色とスクリーンネームの色を調整する
     *
     * @param holder
     * @param status
     */
    private static void setColors(ViewHolder holder, Status status) {
        if (status.isRetweet()) {
            // スクリーンネームの色
            holder.name.setTextColor(AppUtil.getColor(R.color.droid_green));
            holder.screenName.setTextColor(AppUtil.getColor(R.color.droid_green));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                holder.base.setBackgroundResource(R.color.retweet_background);
            else {
                holder.base.setBackgroundColor(AppUtil.getColor(R.color.gray_green));
            }

        } else if (isMention(status)) {
            // mention
            holder.name.setTextColor(AppUtil.getColor(R.color.droid_red));
            holder.screenName.setTextColor(AppUtil.getColor(R.color.droid_red));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                holder.base.setBackgroundResource(R.color.mention_background);
            } else {
                holder.base.setBackgroundColor(AppUtil.getColor(R.color.gray_red));
            }
        } else {
            // others' tweet
            holder.name.setTextColor(AppUtil.getColor(R.color.twitter_blue));
            holder.screenName.setTextColor(AppUtil.getColor(R.color.twitter_blue));
            if (status.getUser().getId() == sUserAccount.getUserId()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    holder.base.setBackgroundResource(R.color.mytweet_background);
                } else {
                    holder.base.setBackgroundColor(AppUtil.getColor(R.color.gray_blue));
                }
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    holder.base.setBackgroundResource(R.color.clickable_background);
                } else {
                    holder.base.setBackgroundResource(R.color.gray);
                }
            }
        }
    }

    private static void setViewHolder(final ViewHolder holder, View convertView) {
        holder.base = convertView;
        holder.name = convertView.findViewById(R.id.name);
        holder.screenName = convertView.findViewById(R.id.screenName);
        holder.text = convertView.findViewById(R.id.text);
        holder.postedAt = convertView.findViewById(R.id.postedAt);
        holder.via = convertView.findViewById(R.id.via);
        holder.icon = convertView.findViewById(R.id.icon);

        holder.icon.setOnTouchListener(new ColorOverlayOnTouch());
        holder.retweeterInfo = convertView.findViewById(R.id.retweeterInfo);
        holder.smallIcon = convertView.findViewById(R.id.smallIcon);
        holder.retweetedBy = convertView.findViewById(R.id.retweeted_by);
        holder.imagePreview = convertView.findViewById(R.id.inline_preview);
        holder.favicon = convertView.findViewById(R.id.favedStar);
        holder.lockedIcon = convertView.findViewById(R.id.lockedIcon);
    }

    private static void setRetweetView(ViewHolder holder, Status origStatus) {
        Status status = origStatus.getRetweetedStatus();

        // 不要な部分を非表示に
        holder.via.setVisibility(View.GONE);
        // 必要な部分を表示
        holder.retweeterInfo.setVisibility(View.VISIBLE);

        setIcon(holder.smallIcon, origStatus);

        holder.retweetedBy.setText(origStatus.getUser().getName() + " @" + origStatus.getUser().getScreenName() + " ("
                + status.getRetweetCount() + ")");

    }

    private static void setNormalTweetView(ViewHolder holder, Status status) {
        // 不要な部分を非表示に
        holder.retweeterInfo.setVisibility(View.GONE);
        if (PrefUtil.getBoolean(R.string.show_source, true)) {
            holder.via.setVisibility(View.VISIBLE);
        } else {
            holder.via.setVisibility(View.GONE);
            return;
        }
        // via表示
        String source = status.getSource();
        if (source.contains(">")) {
            source = source.substring(source.indexOf(">") + 1, source.indexOf("</"));
        }
        holder.via.setText("via " + source);
    }

    private static void setPostedAtTime(ViewHolder holder, Status status) {

        // 日付の設定
        if (status.isRetweet())
            status = status.getRetweetedStatus();
        String timeMode = PrefUtil.getString(R.string.date_display_mode, R.string.relative);
        String time = (timeMode.equals(AppUtil.getString(R.string.relative))) ? AppUtil.dateToRelativeTime(status
                .getCreatedAt()) : AppUtil.dateToAbsoluteTime(status.getCreatedAt());
        holder.postedAt.setText(time);
    }

    private static boolean isMention(Status status) {
        boolean isMention = false;
        if (sUserAccount.getScreenName() != null) {
            for (UserMentionEntity entity : status.getUserMentionEntities()) {
                if (entity.getId() == sUserAccount.getUserId()
                        || entity.getScreenName().equals(sUserAccount.getScreenName())) {
                    isMention = true;
                    break;
                }
            }
        }
        return isMention;
    }

    private static void setIcon(NetworkImageView icon, Status item) {
        String url = AppUtil.getIconURL(item.getUser());
        icon.setImageUrl(url, NetUtil.ICON_LOADER);
    }

    private static void setLockIcon(ImageView lockedIcon, Status item) {
        if (item.getUser().isProtected()) {
            lockedIcon.setVisibility(View.VISIBLE);
        } else {
            lockedIcon.setVisibility(View.GONE);
        }
    }

    private static void setFavIcon(ImageView favIcon, Status item) {
        // 星マーク
        if (item.isFavorited()) {
            favIcon.setVisibility(View.VISIBLE);
        } else {
            favIcon.setVisibility(View.GONE);
        }
    }

    public static void shouldShowInlinePreview(boolean showPreview) {
        shouldShowPreview = showPreview;
    }

    public void shouldPoolStatus(boolean shouldPool) {
        mShouldPool = shouldPool;
    }

    public void destroy(){
        sAdapters.remove(this);
    }

    public static List<TweetStatusAdapter> getAdapters(){
        return sAdapters;
    }
}