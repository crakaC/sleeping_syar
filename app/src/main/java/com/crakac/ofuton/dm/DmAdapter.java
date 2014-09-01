package com.crakac.ofuton.dm;

import twitter4j.DirectMessage;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;

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
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.smallIcon = (ImageView) convertView.findViewById(R.id.smallIcon);
                holder.lockedIcon = (ImageView) convertView.findViewById(R.id.lockedIcon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // フォントサイズの調整
            int fontSize = AppUtil.getFontSize();
            float subFontSize = AppUtil.getSubFontSize();
            holder.name.setTextSize(fontSize);
            holder.text.setTextSize(fontSize);
            holder.postedAt.setTextSize(subFontSize);
            holder.sentTo.setTextSize(fontSize);

            final String senderIconUrl = AppUtil.getIconURL(item.getSender());
            NetUtil.fetchIconAsync(holder.icon, senderIconUrl, android.R.color.transparent, android.R.color.transparent);

            String recipientIconUrl = AppUtil.getIconURL(item.getRecipient());
            NetUtil.fetchIconAsync(holder.smallIcon, recipientIconUrl, android.R.color.transparent,
                    android.R.color.transparent);

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

        private class ViewHolder {
            TextView name;
            TextView text;
            TextView postedAt;
            TextView sentTo;
            ImageView icon;
            ImageView smallIcon;
            ImageView lockedIcon;
        }
    }
}