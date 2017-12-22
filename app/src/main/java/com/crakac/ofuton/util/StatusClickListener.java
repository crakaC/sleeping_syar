package com.crakac.ofuton.util;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.fragment.dialog.StatusDialogFragment;

import java.lang.ref.WeakReference;

/**
 * Created by Kosuke on 2017/12/20.
 */

public class StatusClickListener implements AdapterView.OnItemClickListener {
    WeakReference<FragmentActivity> mActivityRef;
    public StatusClickListener(FragmentActivity a){
        mActivityRef = new WeakReference<>(a);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        FragmentActivity activity = mActivityRef.get();
        if(activity == null)
            return;

        twitter4j.Status status = (twitter4j.Status) parent
                .getItemAtPosition(position);
        StatusDialogFragment dialog = new StatusDialogFragment();
        Bundle b = new Bundle();
        b.putSerializable(C.STATUS, status);
        dialog.setArguments(b);
        dialog.show(activity.getSupportFragmentManager(), "dialog");
    }
}