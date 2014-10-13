package com.crakac.ofuton.fragment.timeline;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;

public class FavoriteTimelineFragment extends AbstractTimelineFragment {
    public static final String TITLE = "Favorites";
	@Override
	protected List<Status> newStatuses(long id, int count) {
		try {
			return mTwitter.getFavorites(mUserId, new Paging().sinceId(id).count(count));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected List<Status> previousStatuses(long id, int count) {
		try {
			return mTwitter.getFavorites(mUserId, new Paging().maxId(id - 1l).count(
					count));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getTimelineName() {
		return TITLE;
	}
}