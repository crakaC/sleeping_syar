package com.crakac.ofuton.fragment.search;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.activity.SearchActivity;
import com.crakac.ofuton.adapter.UserAdapter;
import com.crakac.ofuton.fragment.AbstractPtrFragment;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.util.UserListClickListener;

import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
public class UserSearchFragment extends AbstractPtrFragment implements SearchActivity.Searchable {
    private String mQuery;
    private UserAdapter mAdapter;
    private boolean mExistsNewUser = true;
    private boolean mErrorOccurred = false;

    private UserSearchTask mSearchTask;
    private int mPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuery = getArguments().getString(C.QUERY);
        if(mAdapter == null){
            mAdapter = new UserAdapter(getActivity());
        }
        if (savedInstanceState != null) {
            mPage = savedInstanceState.getInt("page");
            mQuery = savedInstanceState.getString(C.QUERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        setSwipeRefreshEnable(false);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new UserListClickListener(getActivity()));
        searchUser();
        return v;
    }

    private void searchUser() {
        if (mSearchTask != null && mSearchTask.getStatus().equals(AsyncTask.Status.RUNNING)) return;
        if (!mExistsNewUser) return;
        mSearchTask = new UserSearchTask();
        mSearchTask.executeParallel();
    }

    @Override
    public void onBottomOfLastItemShown() {
        searchUser();
    }

    private class UserSearchTask extends ParallelTask<Void, ResponseList<User>> {
        @Override
        protected void onPreExecute() {
            setEmptyViewLoading();
        }

        @Override
        protected ResponseList<User> doInBackground() {
            try {
                return TwitterUtils.getTwitterInstance().searchUsers(mQuery, mPage);
            } catch (TwitterException e) {
                e.printStackTrace();
                mErrorOccurred = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseList<User> users) {
            setEmptyViewStandby();
            if (users == null) return;
            mExistsNewUser = false;
            for (User user : users) {
                if(mAdapter.getPosition(user) < 0){
                    mAdapter.add(user);
                    mExistsNewUser = true;
                }
            }
            if(!mExistsNewUser){
                if(!mErrorOccurred && mAdapter.isEmpty()){
                    setEmptyText(R.string.no_user);
                }
                removeFooterView();
            }

            mPage++;
        }
    }

    @Override
    protected void onClickEmptyView() {
        if(!mErrorOccurred){
            search(mQuery);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("page", mPage);
        outState.putString(C.QUERY, mQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void search(String query) {
        mAdapter.clear();
        mPage = 1;
        mQuery = query;
        mAdapter.clear();
        mSearchTask.cancel(true);
        searchUser();
    }

}
