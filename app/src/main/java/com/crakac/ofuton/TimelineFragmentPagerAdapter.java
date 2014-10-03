package com.crakac.ofuton;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.crakac.ofuton.timeline.AbstractTimelineFragment;
import com.crakac.ofuton.timeline.FavoriteTimelineFragment;
import com.crakac.ofuton.timeline.HomeTimelineFragment;
import com.crakac.ofuton.timeline.ListTimelineFragment;
import com.crakac.ofuton.timeline.MentionsTimelineFragment;

import java.util.ArrayList;
import java.util.Stack;

public class TimelineFragmentPagerAdapter extends SimpleFragmentPagerAdapter<AbstractTimelineFragment> {

    private static final String TAG = TimelineFragmentPagerAdapter.class.getName();

    public TimelineFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        if (fm.getFragments() != null) {
            Stack<AbstractTimelineFragment> stack = new Stack<>();
            for (Fragment f : fm.getFragments()) {
                if (f instanceof FavoriteTimelineFragment || f instanceof MentionsTimelineFragment || f instanceof HomeTimelineFragment) {
                    stack.push((AbstractTimelineFragment) f);
                } else if (f instanceof AbstractTimelineFragment) {
                    if (stack.size() == 3) {
                        for (int i = 0; i < 3; i++) add(stack.pop());
                    }
                    add((AbstractTimelineFragment) f);
                }
            }
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Log.d(TAG, position + ":" + getItem(position).getTimelineName());
        return mFragments.get(position).getTimelineName();
    }

    /**
     * add user's List.
     *
     * @param listId List ID
     * @param title  List title. It is shown in Tab.
     */
    public void addList(long listId, String title) {
        ListTimelineFragment lf = new ListTimelineFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(C.LIST_ID, listId);
        bundle.putString(C.LIST_TITLE, title);
        lf.setArguments(bundle);
        add(lf);
    }

    public ArrayList<AbstractTimelineFragment> getFragments() {
        return mFragments;
    }

    public int getFragmentPosition(Class<? extends AbstractTimelineFragment> c) {
        int pos = -1;
        for (int i = 0; i < getCount(); i++) {
            if (c.isInstance(getItem(i))) {
                pos = i;
                break;
            }
        }
        if (pos < 0) {
            throw new RuntimeException("target fragment doesn't exist");
        }
        return pos;
    }
}
