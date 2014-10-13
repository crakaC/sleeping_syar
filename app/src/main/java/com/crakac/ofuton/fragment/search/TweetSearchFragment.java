package com.crakac.ofuton.fragment.search;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.SearchActivity;
import com.crakac.ofuton.fragment.timeline.AbstractTimelineFragment;
import com.crakac.ofuton.util.TwitterUtils;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
public class TweetSearchFragment extends AbstractTimelineFragment implements SearchActivity.Searchable{
    protected String mQuery;
    private Query.ResultType mResultType;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuery = getArguments().getString(C.QUERY);
        mResultType = (Query.ResultType) getArguments().getSerializable(C.TYPE);
        if(mResultType == null){
            mResultType = Query.RECENT;
        }
        if(savedInstanceState != null){
            mQuery = savedInstanceState.getString(C.QUERY);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(R.string.no_tweet);
        setPoolStatus(false);
    }

    @Override
    protected List<Status> newStatuses(long sinceId, int count) {
        try{
            QueryResult result = TwitterUtils.getTwitterInstance().search(new Query(mQuery).count(count).sinceId(sinceId).resultType(mResultType));
            return result.getTweets();
        }catch (TwitterException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected List<Status> previousStatuses(long maxId, int count) {
        try {
            QueryResult result = TwitterUtils.getTwitterInstance().search(new Query(mQuery).count(count).maxId(maxId - 1L).resultType(mResultType));
            return result.getTweets();
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getTimelineName() {
        return "ツイート";
    }

    @Override
    public void search(String query) {
        mQuery = query;
        stopTask();
        mAdapter.clear();
        initTimeline();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(C.QUERY, mQuery);
        super.onSaveInstanceState(outState);
    }
}
