package com.crakac.ofuton.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.PrefUtil;

public class UserAdapter extends ArrayAdapter<twitter4j.User> {
    private static LayoutInflater mInflater;

    private static class ViewHolder {
        TextView name;
        TextView screenName;
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
            holder.name = convertView.findViewById(R.id.name);
            holder.screenName = convertView.findViewById(R.id.screenName);
            holder.text = convertView.findViewById(R.id.text);
            holder.info = convertView.findViewById(R.id.info);
            holder.icon = convertView.findViewById(R.id.icon);
            holder.lockedIcon = convertView.findViewById(R.id.lockedIcon);
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
        //フォントサイズの調整
        int fontSize = PrefUtil.getFontSize();
        float smallFontSize = PrefUtil.getSubFontSize();
        holder.name.setTextSize(fontSize);
        holder.text.setTextSize(fontSize);
        holder.info.setTextSize(smallFontSize);
        holder.screenName.setTextSize(smallFontSize);

        String url = AppUtil.getIconURL(item);
        AppUtil.setImage(holder.icon,url);

        holder.name.setText(item.getName());
        holder.screenName.setText(" @" + item.getScreenName());
        holder.text.setText(item.getDescription());
        String counts = AppUtil.shapingNums(item.getStatusesCount());
        String friends = AppUtil.shapingNums(item.getFriendsCount());
        String followers = AppUtil.shapingNums(item.getFollowersCount());
        holder.info.setText(counts + " Tweets / " + friends + " Follows / " + followers + " Followers");
        if (item.isProtected()) {
            holder.lockedIcon.setVisibility(View.VISIBLE);
        } else {
            holder.lockedIcon.setVisibility(View.GONE);
        }
        return convertView;
    }
}