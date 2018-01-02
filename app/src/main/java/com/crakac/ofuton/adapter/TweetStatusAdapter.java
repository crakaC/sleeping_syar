package com.crakac.ofuton.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.UserDetailActivity;
import com.crakac.ofuton.util.Account;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.PrefUtil;
import com.crakac.ofuton.util.StatusPool;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.util.Util;
import com.crakac.ofuton.widget.ColorOverlayOnTouch;
import com.crakac.ofuton.widget.MultipleImagePreview;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

public class TweetStatusAdapter extends BaseAdapter {
    private static ArrayList<TweetStatusAdapter> sAdapters = new ArrayList<>(3);//fav, mention, home
    private static LayoutInflater sInflater;
    private static Account sUserAccount;
    private static boolean shouldShowPreview = false;
    private boolean mShouldPool = true;

    private ArrayList<Status> items = new ArrayList<>(50);

    private static class ViewHolder {
        View base;
        TextView name;
        TextView screenName;
        TextView text;
        TextView postedAt;
        View retweeterInfo;
        TextView via;
        TextView retweetedBy;
        ImageView avatarIcon;
        ImageView retweetAvatar;
        ImageView favedAndRetweetedIcon;
        MultipleImagePreview imagePreview;
        ImageView lockedIcon;
    }

    public void add(Status status) {
        items.add(status);
        if (mShouldPool) {
            StatusPool.put(status.getId(), status);
        }
        notifyDataSetChanged();
    }

    public void insert(Status status, int index) {
        items.add(index, status);
        if (mShouldPool) {
            StatusPool.put(status.getId(), status);
        }
        notifyDataSetChanged();
    }

    public TweetStatusAdapter(Context context) {
        sInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        sUserAccount = TwitterUtils.getCurrentAccount();
        sAdapters.add(this);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Status getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    public void remove(Status item) {
        items.remove(item);
    }

    public int getPosition(Status item) {
        return items.indexOf(item);
    }

    public void clear() {
        items.clear();
    }

    public int getPositionById(long id) {
        for (int i = 0; i < items.size(); i++) {
            if (getItem(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final Status item = getItem(position);
        return createView(item, convertView);
    }

    public void updateDisplayTime(int position, View view) {
        if (view.getTag() == null || items.size() <= position) {
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
            convertView.setOnTouchListener((v, event) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            v.getForeground().setHotspot(event.getX(), event.getY());
                        }
                        return false;
                    }
            );

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
        holder.avatarIcon.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra(C.USER, (item.isRetweet()) ? item.getRetweetedStatus().getUser() : item.getUser());
            context.startActivity(intent);
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
        AppUtil.setImage(holder.avatarIcon, AppUtil.getIconURL(status.getUser()));
        // 鍵アイコン
        setLockIcon(holder.lockedIcon, status);
        // ☆
        setIcons(holder.favedAndRetweetedIcon, status);

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
                holder.base.setBackgroundColor(AppUtil.getColor(R.color.dark_green));
            }

        } else if (isMention(status)) {
            // mention
            holder.name.setTextColor(AppUtil.getColor(R.color.droid_red));
            holder.screenName.setTextColor(AppUtil.getColor(R.color.droid_red));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                holder.base.setBackgroundResource(R.color.mention_background);
            } else {
                holder.base.setBackgroundColor(AppUtil.getColor(R.color.dark_red));
            }
        } else {
            // others' tweet
            holder.name.setTextColor(AppUtil.getColor(R.color.twitter_blue));
            holder.screenName.setTextColor(AppUtil.getColor(R.color.twitter_blue));
            if (status.getUser().getId() == sUserAccount.getUserId()) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    holder.base.setBackgroundResource(R.color.mytweet_background);
                } else {
                    holder.base.setBackgroundColor(AppUtil.getColor(R.color.dark_blue));
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
        holder.avatarIcon = convertView.findViewById(R.id.icon);
        if (Util.isPreLollipop()) {
            holder.avatarIcon.setOnTouchListener(new ColorOverlayOnTouch());
        }
        holder.retweeterInfo = convertView.findViewById(R.id.retweeterInfo);
        holder.retweetAvatar = convertView.findViewById(R.id.smallIcon);
        holder.retweetedBy = convertView.findViewById(R.id.retweeted_by);
        holder.imagePreview = convertView.findViewById(R.id.inline_preview);
        holder.favedAndRetweetedIcon = convertView.findViewById(R.id.fav_and_rt_icon);
        holder.lockedIcon = convertView.findViewById(R.id.lockedIcon);
    }

    private static void setRetweetView(ViewHolder holder, Status origStatus) {
        Status status = origStatus.getRetweetedStatus();

        // 不要な部分を非表示に
        holder.via.setVisibility(View.GONE);
        // 必要な部分を表示
        holder.retweeterInfo.setVisibility(View.VISIBLE);
        AppUtil.setImage(holder.retweetAvatar, AppUtil.getIconURL(origStatus.getUser()));
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

    private static void setLockIcon(ImageView lockedIcon, Status item) {
        if (item.getUser().isProtected()) {
            lockedIcon.setVisibility(View.VISIBLE);
        } else {
            lockedIcon.setVisibility(View.GONE);
        }
    }

    private static void setIcons(ImageView icon, Status item) {
        icon.setVisibility(View.VISIBLE);
        if (item.isFavorited() && item.isRetweeted()) {
            icon.setImageResource(R.drawable.ic_fav_and_retweet);
        } else if (item.isFavorited()) {
            icon.setImageResource(R.drawable.ic_fav);
        } else if (item.isRetweeted()) {
            icon.setImageResource(R.drawable.ic_repeat);
        } else {
            icon.setVisibility(View.GONE);
        }
    }

    public static void shouldShowInlinePreview(boolean showPreview) {
        shouldShowPreview = showPreview;
    }

    public void shouldPoolStatus(boolean shouldPool) {
        mShouldPool = shouldPool;
    }

    public void destroy() {
        sAdapters.remove(this);
    }

    public static void updateItem(Status oldStatus, Status newStatus) {
        if (sAdapters == null) return;
        for (TweetStatusAdapter adapter : sAdapters) {
            int pos = adapter.getPositionById(oldStatus.getId());
            if (pos < 0)
                continue;
            adapter.remove(oldStatus);
            adapter.insert(newStatus, pos);
            adapter.notifyDataSetChanged();
        }
    }

    public static void removeItem(Status status) {
        if (sAdapters == null) return;
        for (TweetStatusAdapter adapter : sAdapters) {
            if (adapter.getPositionById(status.getId()) < 0) continue;
            adapter.remove(status);
            adapter.notifyDataSetChanged();
        }
    }
}