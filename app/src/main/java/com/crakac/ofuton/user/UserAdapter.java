package com.crakac.ofuton.user;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.NetUtil;
import com.crakac.ofuton.util.PreferenceUtil;

public class UserAdapter extends ArrayAdapter<twitter4j.User> {
    private static LayoutInflater mInflater;

    private static class ViewHolder {
        TextView name;
        TextView text;
        TextView info;
        ImageView icon;
        ImageView lockedIcon;
    }

    public UserAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
        mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        twitter4j.User item = getItem(position);
        ViewHolder holder;

        //レイアウトの構築
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_user, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.info = (TextView) convertView.findViewById(R.id.info);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.lockedIcon = (ImageView)convertView.findViewById(R.id.lockedIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //フォントサイズの調整
        int fontSize = PreferenceUtil.getFontSize();
        float smallFontSize = PreferenceUtil.getSubFontSize();
        holder.name.setTextSize(fontSize);
        holder.text.setTextSize(fontSize);
        holder.info.setTextSize(smallFontSize);

        String url = AppUtil.getIconURL(item);
        ImageContainer imageContainer = (ImageContainer) holder.icon.getTag();
        if(imageContainer != null){
            imageContainer.cancelRequest();
        }
        imageContainer = NetUtil.fetchIconAsync(holder.icon, url);
        holder.icon.setTag(imageContainer);

        holder.name.setText(item.getName() + " @"+ item.getScreenName());
        holder.text.setText(item.getDescription());
        String counts = AppUtil.shapingNums(item.getStatusesCount());
        String friends = AppUtil.shapingNums(item.getFriendsCount());
        String followers = AppUtil.shapingNums(item.getFollowersCount());
        holder.info.setText( counts + " Tweets / " + friends + " Follows / " + followers + " Followers");
        if(item.isProtected()){
            holder.lockedIcon.setVisibility(View.VISIBLE);
        } else {
            holder.lockedIcon.setVisibility(View.GONE);
        }
        return convertView;
    }
}