package com.crakac.ofuton.search;

import android.os.Bundle;
import android.util.Log;

import com.crakac.ofuton.C;
import com.crakac.ofuton.timeline.AbstractTimelineFragment;
import com.crakac.ofuton.util.TwitterUtils;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
public class TweetSearchFragment extends AbstractTimelineFragment {
    private String mQuery;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuery = getArguments().getString(C.QUERY);
    }

    @Override
    protected List<Status> newStatuses(long sinceId, int count) {
        try{
            QueryResult result = TwitterUtils.getTwitterInstance().search(new Query(mQuery).count(count).sinceId(sinceId).resultType(Query.ResultType.recent));
            return result.getTweets();
        }catch (TwitterException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected List<Status> previousStatuses(long maxId, int count) {
        try {
            QueryResult result = TwitterUtils.getTwitterInstance().search(new Query(mQuery).count(count).maxId(maxId - 1L).resultType(Query.ResultType.recent));
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

    public void clear(){
        mAdapter.clear();
    }
}
