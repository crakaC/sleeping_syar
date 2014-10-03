package com.crakac.ofuton.search;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crakac.ofuton.AbstractPtrFragment;
import com.crakac.ofuton.C;
import com.crakac.ofuton.user.UserAdapter;
import com.crakac.ofuton.user.UserListClickListener;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
public class UserSearchFragment extends AbstractPtrFragment implements SearchActivity.Searchable {
    private String mQuery;
    private UserAdapter mAdapter;

    private UserSearchTask mSearchTask;
    private int mPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuery = getArguments().getString(C.QUERY);
        mAdapter = new UserAdapter(getActivity());
        if (savedInstanceState != null) {
            mPage = savedInstanceState.getInt("page");
            mQuery = savedInstanceState.getString(C.QUERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mSwipeWidget.setEnabled(false);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new UserListClickListener(getActivity()));
        searchUser();
        return v;
    }

    private void searchUser() {
        if (mSearchTask != null && mSearchTask.getStatus().equals(AsyncTask.Status.RUNNING)) return;
        mSearchTask = new UserSearchTask();
        mSearchTask.executeParallel();
    }

    @Override
    public void onBottomOfLastItemShown() {
        searchUser();
    }

    private class UserSearchTask extends ParallelTask<Void, Void, ResponseList<User>> {
        @Override
        protected void onPreExecute() {
            setEmptyViewLoading();
        }

        @Override
        protected ResponseList<User> doInBackground(Void... params) {
            try {
                return TwitterUtils.getTwitterInstance().searchUsers(mQuery, mPage);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseList<User> users) {
            setEmptyViewStandby();
            if (users == null) return;
            for (User user : users) {
                mAdapter.add(user);
            }
            mPage++;
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
