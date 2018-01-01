package com.crakac.ofuton.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crakac.ofuton.R;
import com.crakac.ofuton.util.PrefUtil;
import com.crakac.ofuton.widget.ListViewEx;
import com.crakac.ofuton.widget.ProgressTextView;

abstract public class AbstractPtrFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ListViewEx.OnLastItemVisibleListener {
    private SwipeRefreshLayout mSwipeWidget;
    protected ListViewEx mListView;// 引っ張って更新できるやつの中身
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

        // 引っ張って更新できるやつ
        mSwipeWidget = view.findViewById(R.id.swipeRefresh);
        mSwipeWidget.setOnRefreshListener(this);
        mSwipeWidget.setColorSchemeResources(R.color.refresh1, R.color.refresh2, R.color.refresh3, R.color.refresh4);

        mListView = view
                .findViewById(R.id.listView1);
        mListView.setOnLastItemVisibleListener(this);
        mListView.setFastScrollEnabled(PrefUtil.getBoolean(R.string.enable_fast_scroll));
        mListView.setSmoothScrollbarEnabled(true);

        mFooterView = new ProgressTextView(getActivity());
        mFooterView.setText(R.string.read_more);
        mFooterView.setBackgroundResource(R.color.clickable_background);
        mEmptyView = view.findViewById(R.id.emptyView);
        mFooterView.setOnClickListener((v) ->
                onClickFooterView());
        mEmptyView.setOnClickListener(v ->
                onClickEmptyView());
        mListView.setEmptyView(mEmptyView);
        mListView.addFooterView(mFooterView);
        setEmptyViewStandby();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mListView.setNestedScrollingEnabled(true);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFooterView.loading();
    }

    public void setSwipeRefreshEnable(boolean enable) {
        mSwipeWidget.setEnabled(enable);
    }

    public void setSwipeWidgetRefreshing(boolean refreshing) {
        mSwipeWidget.setRefreshing(refreshing);
    }

    protected void setEmptyViewLoading() {
        if (!isAdded()) return;
        mEmptyView.loading();
    }

    protected void setEmptyViewStandby() {
        if (!isAdded()) return;
        mEmptyView.standby();
    }

    protected void setEmptyText(int resId) {
        if (!isAdded()) return;
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
    public void onRefresh() {
    }

    @Override
    public void onBottomOfLastItemShown() {
    }

    @Override
    public void onLastItemVisible() {
    }

    protected void onClickFooterView() {
    }

    protected void onClickEmptyView() {
    }

    //一番上まで瞬間スクロールする
    public void scrollToTop() {
        if (!isAdded())
            return;
        mListView.setSelectionAfterHeaderView();
    }
}
