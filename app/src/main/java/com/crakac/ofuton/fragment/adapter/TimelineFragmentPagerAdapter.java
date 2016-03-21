package com.crakac.ofuton.fragment.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.crakac.ofuton.C;
import com.crakac.ofuton.fragment.timeline.AbstractTimelineFragment;
import com.crakac.ofuton.fragment.timeline.FavoriteTimelineFragment;
import com.crakac.ofuton.fragment.timeline.HomeTimelineFragment;
import com.crakac.ofuton.fragment.timeline.ListTimelineFragment;
import com.crakac.ofuton.fragment.timeline.MentionsTimelineFragment;

import java.util.ArrayList;
import java.util.Stack;

public class TimelineFragmentPagerAdapter extends SimpleFragmentPagerAdapter<AbstractTimelineFragment> {

    public TimelineFragmentPagerAdapter(FragmentActivity context, ViewPager viewPager) {
        super(context, viewPager);
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
        add(ListTimelineFragment.class, bundle, title, listId);
    }
}