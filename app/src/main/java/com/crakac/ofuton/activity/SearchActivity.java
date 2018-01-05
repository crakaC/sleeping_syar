package com.crakac.ofuton.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.fragment.adapter.SearchFragmentPagerAdapter;
import com.crakac.ofuton.fragment.search.LocalSearchFragment;
import com.crakac.ofuton.fragment.search.TweetSearchFragment;
import com.crakac.ofuton.fragment.search.UserSearchFragment;
import com.crakac.ofuton.util.AppUtil;

/**
 * Created by kosukeshirakashi on 2014/09/05.
 */
public class SearchActivity extends FinishableActionbarActivity {

    private TabLayout mTab;
    private ViewPager mPager;
    private SearchFragmentPagerAdapter mAdapter;
    private SearchView mSearchView;
    private String mQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_tab);
        mQuery = getIntent().getStringExtra(C.QUERY);
        mTab = findViewById(R.id.tab);
        mPager = findViewById(R.id.pager);
        mAdapter = new SearchFragmentPagerAdapter(this, mPager);
        setFragments();
        mPager.setOffscreenPageLimit(mAdapter.getCount());
        mTab.setupWithViewPager(mPager);
    }

    private Bundle createArgs(String query) {
        Bundle b = new Bundle(1);
        b.putString(C.QUERY, query);
        return b;
    }

    private Bundle createArgs(String query, String optionQuery){
        Bundle b = new Bundle(3);
        b.putString(C.QUERY, query);
        b.putString(C.OPTION_QUERY, optionQuery);
        return b;
    }

    private void setFragments(){
        mAdapter.add(LocalSearchFragment.class, createArgs(mQuery), 0);
        mAdapter.add(TweetSearchFragment.class, createArgs(buildQuery(mQuery)), 1);
        mAdapter.add(UserSearchFragment.class, createArgs(buildQuery(mQuery)), 2);
        mAdapter.add(TweetSearchFragment.class, createArgs(buildQuery(mQuery), "filter:images"), 3);
        Bundle b = createArgs(buildQuery(mQuery), "filter:videos");
        b.putBoolean(C.NEED_LOOK_UP, true);
        mAdapter.add(TweetSearchFragment.class, b, 4);
        mAdapter.notifyDataSetChanged();
    }

    private String buildQuery(String query){
        return query + " exclude:retweets";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        MenuItem search = menu.findItem(R.id.search);
        mSearchView = (SearchView) search.getActionView();
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
        mSearchView.setIconified(false);
        mSearchView.clearFocus();
        mSearchView.setQuery(mQuery, false);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mSearchView != null && !mSearchView.isIconified()) {
            AppUtil.closeSearchView(mSearchView);
        } else {
            super.onBackPressed();
        }
    }

    private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            search(buildQuery(query));
            mSearchView.clearFocus();// clear focus and hide software keyboard
            return true;
        }

    };

    private void search(String query) {
        for(Fragment f : mAdapter.getFragments()){
            if(f instanceof Searchable){
                ((Searchable) f).search(query);
            }
        }
    }

    public interface Searchable{
        void search(String query);
    }
}