package com.crakac.ofuton.status;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.timeline.AbstractStatusFragment;

public class StatusClickListener implements AdapterView.OnItemClickListener {
    private AbstractStatusFragment mFragment;

    public StatusClickListener(AbstractStatusFragment f) {
        mFragment = f;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        twitter4j.Status status = (twitter4j.Status) parent
                .getItemAtPosition(position);
        StatusDialogFragment dialog = new StatusDialogFragment(mFragment);
        Bundle b = new Bundle();
        b.putSerializable(C.STATUS, status);
        dialog.setArguments(b);
        dialog.show(mFragment.getFragmentManager(), "dialog");
    }
}
