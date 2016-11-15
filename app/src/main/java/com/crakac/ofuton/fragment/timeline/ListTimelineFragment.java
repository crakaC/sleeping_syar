package com.crakac.ofuton.fragment.timeline;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crakac.ofuton.C;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.TwitterException;

public class ListTimelineFragment extends AbstractTimelineFragment {
	private long listId;
	private String listTitle;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		listId = bundle.getLong(C.LIST_ID);
		listTitle = bundle.getString(C.LIST_TITLE);
		Log.d("ListTimeline", "onCreateView:" + listTitle + ", list_id:" + listId);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected List<Status> newStatuses(long id, int count) {
		try {
			return mTwitter.getUserListStatuses(listId, new Paging()
					.sinceId(id).count(count));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected List<Status> previousStatuses(long id, int count) {
		try {
			return mTwitter.getUserListStatuses(listId,
					new Paging().maxId(id - 1l).count(count));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getTimelineName() {
		return getArguments().getString(C.LIST_TITLE);
	}
}