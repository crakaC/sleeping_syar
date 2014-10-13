package com.crakac.ofuton.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.activity.UserDetailActivity;

import java.lang.ref.WeakReference;

import twitter4j.User;

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
public class UserListClickListener implements AdapterView.OnItemClickListener {
    private WeakReference<Activity> mContext;
    public UserListClickListener(Activity context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        Activity context = mContext.get();
        if(context == null) return;
        ListView lv = (ListView) parent;
        User user = (User) lv.getItemAtPosition(position);
        Intent intent = new Intent(context,
                UserDetailActivity.class);
        intent.putExtra(C.USER, user);

        context.startActivity(intent);
    }
}
