package com.crakac.ofuton.user;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crakac.ofuton.C;

import twitter4j.User;

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
public class UserListClickListener implements AdapterView.OnItemClickListener {
    private Context mContext;
    public UserListClickListener(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        ListView lv = (ListView) parent;
        User user = (User) lv.getItemAtPosition(position);
        Intent intent = new Intent(mContext,
                UserDetailActivity.class);
        intent.putExtra(C.USER, user);
        mContext.startActivity(intent);
    }
}
