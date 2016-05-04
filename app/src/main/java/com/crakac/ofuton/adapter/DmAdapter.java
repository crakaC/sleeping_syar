package com.crakac.ofuton.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.PrefUtil;

import twitter4j.DirectMessage;

public class DmAdapter extends ArrayAdapter<DirectMessage> {

    private ViewConstructor mViewConstructor;

    public DmAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
        mViewConstructor = new ViewConstructor(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DirectMessage item = getItem(position);
        return mViewConstructor.createView(item, convertView);
    }

    public static class ViewConstructor{
        private LayoutInflater mInflater;

        public ViewConstructor(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View createView(DirectMessage item, View convertView) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_dm, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.sentTo = (TextView) convertView.findViewById(R.id.sentTo);
                holder.postedAt = (TextView) convertView.findViewById(R.id.postedAt);
                holder.icon = (NetworkImageView) convertView.findViewById(R.id.icon);
                holder.smallIcon = (NetworkImageView) convertView.findViewById(R.id.smallIcon);
                holder.lockedIcon = (ImageView) convertView.findViewById(R.id.lockedIcon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // フォントサイズの調整
            int fontSize = PrefUtil.getFontSize();
            float subFontSize = PrefUtil.getSubFontSize();
            holder.name.setTextSize(fontSize);
            holder.text.setTextSize(fontSize);
            holder.postedAt.setTextSize(subFontSize);
            holder.sentTo.setTextSize(fontSize);

            final String senderIconUrl = AppUtil.getIconURL(item.getSender());
            holder.icon.setImageUrl(senderIconUrl, NetUtil.ICON_LOADER);

            String recipientIconUrl = AppUtil.getIconURL(item.getRecipient());
            holder.smallIcon.setImageUrl(recipientIconUrl, NetUtil.ICON_LOADER);

            // ユーザー名＋スクリーンネーム
            holder.name.setText(item.getSender().getName() + " @" + item.getSenderScreenName());

            // 本文
            holder.text.setText(AppUtil.getColoredText(item.getText(), item));

            // 送信先
            holder.sentTo.setText(item.getRecipientScreenName());

            // 日付の設定
            holder.postedAt.setText(AppUtil.dateToAbsoluteTime(item.getCreatedAt()));
            holder.postedAt.setVisibility(View.VISIBLE);

            // 鍵アイコン
            if (item.getSender().isProtected()) {
                holder.lockedIcon.setVisibility(View.VISIBLE);
            } else {
                holder.lockedIcon.setVisibility(View.GONE);
            }
            return convertView;
        }

        private static class ViewHolder {
            TextView name;
            TextView text;
            TextView postedAt;
            TextView sentTo;
            NetworkImageView icon;
            NetworkImageView smallIcon;
            ImageView lockedIcon;
        }
    }
}