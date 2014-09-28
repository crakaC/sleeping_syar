package com.crakac.ofuton.timeline;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.crakac.ofuton.AbstractPtrFragment;
import com.crakac.ofuton.R;
import com.crakac.ofuton.status.StatusClickListener;
import com.crakac.ofuton.status.TweetStatusAdapter;
import com.crakac.ofuton.util.PreferenceUtil;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.widget.MultipleImagePreview;

import twitter4j.Status;
import twitter4j.Twitter;

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
        mListView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                ((ImageView) view.findViewById(R.id.icon)).setImageBitmap(null);
                ((ImageView) view.findViewById(R.id.smallIcon)).setImageBitmap(null);
                ((MultipleImagePreview)view.findViewById(R.id.inline_preview)).cleanUp();
            }
        });
		return v;
	}

	@Override
	public void onStart() {
	    super.onStart();
	    mAdapter.shouldShowInlinePreview(PreferenceUtil.getBoolean(R.string.show_image_in_timeline, true));
	}

    public TweetStatusAdapter getAdapter() {
		return mAdapter;
	}

    private Status mFirstVisibleStatus;
    private int mFirstVisibleOffset = -1;

    protected void savePosition() {
        if (mAdapter.isEmpty() || mListView.getChildAt(0) == null) return;
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