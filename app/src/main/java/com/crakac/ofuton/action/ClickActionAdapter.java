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

	private ImageView icon;
	private TextView actionName;

	public ClickActionAdapter(Context context) {
		super(context, android.R.layout.simple_list_item_1);
		mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.status_action_item, null);
		actionName = convertView.findViewById(R.id.action_name);
		icon =  convertView.findViewById(R.id.action_icon);
		ClickAction item = getItem(position);
		actionName.setText(item.getText());
		icon.setImageResource(item.getIconId());
		return convertView;
	}
}