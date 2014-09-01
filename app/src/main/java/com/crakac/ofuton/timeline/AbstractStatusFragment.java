package com.crakac.ofuton.timeline;

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

	/**
	 *
	 * @return
	 */
	public TweetStatusAdapter getAdapter() {
		return mAdapter;
	}
}