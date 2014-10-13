package com.crakac.ofuton.fragment.dm;

import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.TwitterException;

public class DmSentFragment extends AbstractDmFragment {

	@Override
	protected List<DirectMessage> fetchMessages(long maxId, int counts) {
		try {
			return mTwitter.getSentDirectMessages(new Paging().count(counts).maxId(maxId-1l));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected List<DirectMessage> newMessages(long sinceId, int counts) {
		try {
			return mTwitter.getSentDirectMessages(new Paging().count(counts).sinceId(sinceId));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

}
