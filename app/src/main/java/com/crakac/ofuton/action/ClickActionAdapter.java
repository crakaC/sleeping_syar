package com.crakac.ofuton.action;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crakac.ofuton.R;
import com.crakac.ofuton.action.status.ClickAction;

/**
 * ツイートをタップした時に出てくるダイアログのメニュー部分
 *
 * @author Kosuke
 *
 */
public class ClickActionAdapter extends ArrayAdapter<ClickAction> {
	private LayoutInflater mInflater;

	private static class ViewHolder {
		TextView actionName;
		ImageView icon;
	}

	public ClickActionAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.status_action_item, null);
			holder = new ViewHolder();
			holder.actionName = convertView
					.findViewById(R.id.action_name);
			holder.icon = convertView
					.findViewById(R.id.action_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ClickAction item = getItem(position);
		holder.actionName.setText(item.getText());
		holder.icon.setImageResource(item.getIconId());
		return convertView;
	}
}