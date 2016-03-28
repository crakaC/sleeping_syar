package com.crakac.ofuton.fragment.search;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.crakac.ofuton.util.StatusPool;

import java.util.List;

import twitter4j.Status;

/**
 * Created by kosukeshirakashi on 2014/10/04.
 */
public class LocalSearchFragment extends TweetSearchFragment{

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        removeFooterView();
        setSwipeRefreshEnable(false);
    }

    @Override
    protected List<Status> newStatuses(long sinceId, int count) {
        return null;
    }

    @Override
    protected List<Status> previousStatuses(long maxId, int count) {
        return StatusPool.search(getMQuery());
    }
}
