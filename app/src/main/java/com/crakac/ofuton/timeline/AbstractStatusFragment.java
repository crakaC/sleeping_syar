package com.crakac.ofuton.timeline;

import twitter4j.Status;
import twitter4j.Twitter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crakac.ofuton.AbstractPtrFragment;
import com.crakac.ofuton.R;
import com.crakac.ofuton.status.StatusClickListener;
import com.crakac.ofuton.status.TweetStatusAdapter;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterUtils;

public abstract class AbstractStatusFragment extends AbstractPtrFragment {

	protected TweetStatusAdapter mAdapter;// statusを保持してlistviewに表示する奴
	protected Twitter mTwitter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mTwitter == null) {
			mTwitter = TwitterUtils.getTwitterInstance();
		}
		if (mAdapter == null){
			mAdapter = new TweetStatusAdapter(getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new StatusClickListener(this));
		return v;
	}

	@Override
	public void onStart() {
	    super.onStart();
	    mAdapter.shouldShowInlinePreview(AppUtil.getBooleanPreference(R.string.show_image_in_timeline, true));
	}

    public TweetStatusAdapter getAdapter() {
		return mAdapter;
	}

    private Status mFirstVisibleStatus;
    private int mFirstVisibleOffset = -1;

    protected void savePosition() {
        if (mAdapter.isEmpty()) return;
        mFirstVisibleStatus = mAdapter.getItem(mListView.getFirstVisiblePosition());
        mFirstVisibleOffset = mListView.getChildAt(0).getTop();
    }

    protected void restorePosition() {
        if (mFirstVisibleStatus != null) {
            int pos = mAdapter.getPosition(mFirstVisibleStatus);
            mListView.setSelectionFromTop(pos, mFirstVisibleOffset);
        }
    }

    protected void insertQuietly(twitter4j.Status status) {
        if (isResumed()) {
            savePosition();
        }
        mAdapter.insert(status, 0);
        restorePosition();
    }

    protected boolean isFirstItemVisible() {
        return mListView.getFirstVisiblePosition() == 0;
    }

}