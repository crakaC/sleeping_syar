package com.crakac.ofuton;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import com.crakac.ofuton.status.StatusPool;
import com.crakac.ofuton.timeline.AbstractStatusFragment;

import java.util.List;

import twitter4j.Status;

public class SearchFragment extends AbstractStatusFragment implements LoaderCallbacks<List<Status>>{
    private final int SEARCH_LOADER = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(SEARCH_LOADER, getArguments(), this);
        removeFooterView();
    }

    @Override
    public Loader<List<Status>> onCreateLoader(int arg0, Bundle arg1) {
        Loader<List<Status>> loader = null;
        if (arg0 == SEARCH_LOADER){
            setEmptyViewLoading();
            loader = new AsyncSearchStatusLoader(getActivity(), arg1.getString("query"));
            loader.forceLoad();
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<Status>> loader, List<Status> statuses) {
        if(statuses.isEmpty()){
            setEmptyText(R.string.not_found);
        } else {
            for(Status status : statuses){
                mAdapter.add(status);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Status>> arg0) {
    }

    static class AsyncSearchStatusLoader extends AsyncTaskLoader<List<Status>>{
        String mQuery;
        public AsyncSearchStatusLoader(Context context, String query) {
            super(context);
            this.mQuery = query;
        }

        @Override
        public List<Status> loadInBackground() {
            return StatusPool.search(mQuery);
        }
    }
}
