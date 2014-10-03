package com.crakac.ofuton.search;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.crakac.ofuton.R;
import com.crakac.ofuton.SimpleFragmentPagerAdapter;

/**
 * Created by kosukeshirakashi on 2014/09/05.
 */
public class SearchActivity extends ActionBarActivity {

    private SearchView mSearchView;
    private SearchFragment mSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_tab);
        SimpleFragmentPagerAdapter<SearchFragment> pagerAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
        pagerAdapter.add(new SearchFragment());
        //pagerAdapter.add(new GlobalSearchFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
        mSearchView.setOnCloseListener(mOnCloseListener);

        MenuItemCompat.expandActionView(menu.findItem(R.id.search));
        mSearchView.requestFocus();
        return true;
    }

    private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            searchStatus(query);
            mSearchView.clearFocus();// clear focus and hide software keyboard
            return true;
        }

    };

    private final SearchView.OnCloseListener mOnCloseListener = new SearchView.OnCloseListener() {
        @Override
        public boolean onClose() {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.remove(mSearchFragment);
                ft.commit();
                getSupportFragmentManager().popBackStack("timelines",
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            return false;
        }
    };

    private void searchStatus(String query) {
        mSearchFragment = new SearchFragment();
        Bundle b = new Bundle(1);
        b.putSerializable("query", query);
        mSearchFragment.setArguments(b);
        FragmentManager m = getSupportFragmentManager();
        FragmentTransaction ft = m.beginTransaction();
        ft.replace(R.id.main_container, mSearchFragment, "search");
        ft.addToBackStack("timelines");
        ft.commit();
    }
}