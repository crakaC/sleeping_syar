package com.crakac.ofuton;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.crakac.ofuton.widget.ProgressTextView;

abstract public class AbstractPtrFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    protected SwipeRefreshLayout mSwipeWidget;
	protected ListView mListView;// 引っ張って更新できるやつの中身
	protected ProgressTextView mFooterView, mEmptyView;// 一番下のやつ,最初のやつ

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.ptr_list, null);
        mSwipeWidget = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefresh);
		// 引っ張って更新できるやつ
		mListView = (ListView) view
				.findViewById(R.id.listView1);
        mSwipeWidget.setOnRefreshListener(this);
		// 中身のListView
		mListView.setFastScrollEnabled(true);
		mFooterView = new ProgressTextView(getActivity());
		mFooterView.setText(R.string.read_more);
		mFooterView.setBackgroundResource(R.color.timeline_background);
		mEmptyView = (ProgressTextView) view.findViewById(R.id.emptyView);
		mFooterView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickFooterView();
			}
		});
		mEmptyView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickEmptyView();
			}
		});
		mListView.setEmptyView(mEmptyView);
		mListView.addFooterView(mFooterView);
		setEmptyViewStandby();
		return view;
	}

	protected void setEmptyViewLoading() {
		if (!isAdded()) return;
		mEmptyView.loading();
	}

	protected void setEmptyViewStandby() {
		if (!isAdded()) return;
		mEmptyView.standby();
	}

	protected void setEmptyText(int resId){
	    if(!isAdded()) return;
	    setEmptyViewStandby();
	    mEmptyView.setText(resId);
	}

	protected void setFooterViewLoading() {
		if (!isAdded()) return;
		mFooterView.loading();
	}

	protected void setFooterViewStandby() {
		if (!isAdded()) return;
		mFooterView.standby();
	}

	protected void removeFooterView() {
		mListView.removeFooterView(mFooterView);
	}

    @Override
    public void onRefresh(){}

    protected void onClickFooterView(){}

	protected void onClickEmptyView(){}
}
