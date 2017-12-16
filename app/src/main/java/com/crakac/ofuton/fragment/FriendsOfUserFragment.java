package com.crakac.ofuton.fragment;

import twitter4j.PagableResponseList;
import twitter4j.TwitterException;
import twitter4j.User;

public class FriendsOfUserFragment extends AbstractUserFragment {

	@Override
	protected PagableResponseList<User> fetchNextUser(long cursor) {
		if (mUser.getFriendsCount() == 0) {
			return null;
		}
		try {
			return mTwitter.getFriendsList(mUser.getId(), cursor);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}
}