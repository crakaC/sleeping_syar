package com.crakac.ofuton.search;

import android.os.Bundle;

import com.crakac.ofuton.AbstractPtrFragment;
import com.crakac.ofuton.C;
import com.crakac.ofuton.user.AbstractUserFragment;
import com.crakac.ofuton.util.TwitterUtils;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * Created by kosukeshirakashi on 2014/10/03.
 */
public class UserSearchFragment extends AbstractPtrFragment {
    String mQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuery = getArguments().getString(C.QUERY);
    }
}
