package com.crakac.ofuton.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.adapter.TweetStatusAdapter;
import com.crakac.ofuton.fragment.TweetFragment;
import com.crakac.ofuton.fragment.adapter.TimelineFragmentPagerAdapter;
import com.crakac.ofuton.fragment.timeline.AbstractTimelineFragment;
import com.crakac.ofuton.fragment.timeline.FavoriteTimelineFragment;
import com.crakac.ofuton.fragment.timeline.HomeTimelineFragment;
import com.crakac.ofuton.fragment.timeline.MentionsTimelineFragment;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.ParallelTask;
import com.crakac.ofuton.util.PrefUtil;
import com.crakac.ofuton.util.ReloadChecker;
import com.crakac.ofuton.util.TweetButtonPosition;
import com.crakac.ofuton.util.TwitterList;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.widget.TapToScrollTopListener;

import java.util.ArrayList;
import java.util.List;

import twitter4j.TwitterException;
import twitter4j.User;

public class MainActivity extends AppCompatActivity {
    public static final long TAB_ID_FAVORITE = 1;
    public static final long TAB_ID_MENTION = 2;
    public static final long TAB_ID_HOME = 3;

    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewPager mPager;
    private TimelineFragmentPagerAdapter mAdapter;
    private Menu mMenu;
    private SearchView mSearchView;
    FloatingActionButton mTweetBtn;
    /* Navigation drawer */
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private TweetFragment mTweetFragment;
    private TabLayout mTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // CurrentUserが存在しない時（初回起動時等）は，アカウント画面を起動する
        if (!TwitterUtils.existsCurrentAccount()) {
            Intent intent = new Intent(this, AccountActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            TwitterUtils.fetchApiConfigurationAsync(this);
        }
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);
        mTweetFragment = (TweetFragment) getSupportFragmentManager().findFragmentById(R.id.quick_tweet);
        mTweetBtn = findViewById(R.id.tweetEveryWhere);// 右下のツイートボタン
        mTweetBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TweetActivity.class);
            startActivity(intent);
        });
        mTweetBtn.setOnLongClickListener(v -> {
            mTweetBtn.hide();
            mTweetFragment.show();
            return true;
        });


        TweetButtonPosition buttonPosition = TweetButtonPosition.current();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mTweetBtn.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            switch (buttonPosition) {
                case Right:
                    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    break;
                case Center:
                    lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    break;
                case Left:
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    break;
            }
            mTweetBtn.setLayoutParams(lp);
        } else {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mTweetBtn.getLayoutParams();
            switch (buttonPosition) {
                case Right:
                    lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                    break;
                case Center:
                    lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    break;
                case Left:
                    lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
                    break;
            }
            mTweetBtn.setLayoutParams(lp);
        }

        /* actionbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);
        createNavigationDrawer();

        mTab = findViewById(R.id.tab);
        mPager = findViewById(R.id.pager);
        mAdapter = new TimelineFragmentPagerAdapter(this, mPager);
        setPages(mAdapter);
        if (savedInstanceState == null) {
            mPager.setCurrentItem(getFragmentPosition(TAB_ID_HOME));// HomeTimelineの位置に合わせる
        }
        mPager.setOffscreenPageLimit(13);// 保持するFragmentの数を指定．全フラグメントを保持するのでぬるぬる動くがメモリを食う
        mTab.setupWithViewPager(mPager);// PagerSlidingTabStripにViewPagerをセット．
        mTab.addOnTabSelectedListener(new TapToScrollTopListener(mAdapter, mPager));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (ReloadChecker.shouldHardReload()) {
            ReloadChecker.reset();
            finish();
            startActivity(new Intent(getIntent()));
            return;
        }
        showRefreshMenu(PrefUtil.getBoolean(R.string.enable_refresh_btn));
        if (PrefUtil.getBoolean(R.string.always_awake) && PrefUtil.getBoolean(R.string.streaming_mode)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        AppUtil.closeSearchView(mSearchView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TweetStatusAdapter.shouldShowInlinePreview(PrefUtil.getBoolean(R.string.show_image_in_timeline, true));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ReloadChecker.shouldSoftReload()) {
            ReloadChecker.reset();
            for (AbstractTimelineFragment f : getFragments()) {
                f.getAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggleDrawer();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        mMenu = menu;
        showRefreshMenu(PrefUtil.getBoolean(R.string.enable_refresh_btn));
        MenuItem search = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(search);
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mSearchView != null && !mSearchView.isIconified()) {
            AppUtil.closeSearchView(mSearchView);
        } else if (!mTweetBtn.isShown()) {
            mTweetBtn.show();
            mTweetFragment.hide();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mSearchView.isIconified()) {
                    toggleDrawer();
                } else {
                    mSearchView.setQuery("", false);
                    mSearchView.setIconified(true);
                }
                return true;
            case R.id.refresh:// reload
                for (AbstractTimelineFragment f : getFragments()) {
                    f.refresh();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppUtil.clearCache();
    }

    /**
     * set pages to adapter
     *
     * @param adapter
     */
    private void setPages(TimelineFragmentPagerAdapter adapter) {
        if (adapter.isEmpty()) {
            Bundle b = createFragmentArgs();
            adapter.add(FavoriteTimelineFragment.class, b, FavoriteTimelineFragment.TITLE, TAB_ID_FAVORITE);
            adapter.add(MentionsTimelineFragment.class, b, MentionsTimelineFragment.TITLE, TAB_ID_MENTION);
            adapter.add(HomeTimelineFragment.class, b, HomeTimelineFragment.TITLE, TAB_ID_HOME);
            for (TwitterList list : TwitterUtils.getListsOfCurrentAccount()) {
                adapter.addList(list.getListId(), list.getName());
            }
            adapter.notifyDataSetChanged();
        }
    }

    private Bundle createFragmentArgs() {
        Bundle args = new Bundle();
        long id = TwitterUtils.getCurrentAccountId();
        args.putLong(C.USER_ID, id);
        return args;
    }

    private void showRefreshMenu(boolean mode) {
        Log.d(TAG, "showRefreshMenu:" + mode);
        if (mMenu != null) {
            mMenu.findItem(R.id.refresh).setVisible(mode);
        }
    }

    private void toggleDrawer() {
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private int getFragmentPosition(long fragmentId) {
        if (mAdapter == null) {
            throw new RuntimeException("pagerAdapter is null");
        }
        return mAdapter.findPositionById(fragmentId);
    }

    private void createNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_header);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        mNavigationView = (NavigationView) findViewById(R.id.navigation_header);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                switch (menuItem.getItemId()) {
                    case R.id.menu_home:
                        mPager.setCurrentItem(getFragmentPosition(TAB_ID_HOME));
                        break;
                    case R.id.menu_account:
                        startActivity(new Intent(MainActivity.this, AccountActivity.class));
                        break;
                    case R.id.menu_mention:
                        mPager.setCurrentItem(getFragmentPosition(TAB_ID_MENTION));
                        break;
                    case R.id.manu_favorite:
                        mPager.setCurrentItem(getFragmentPosition(TAB_ID_FAVORITE));
                        break;
                    case R.id.menu_dm:
                        startActivity(new Intent(MainActivity.this, DmActivity.class));
                        break;
                    case R.id.menu_list:
                        startActivity(new Intent(MainActivity.this, ListSelectActivity.class));
                        break;
                    case R.id.menu_setting:
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        break;
                    case R.id.syar:
                        AppUtil.syarAsync();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        View v = mNavigationView.getHeaderView(0);
        final TextView userName = v.findViewById(R.id.screenName);
        final ImageView iv = v.findViewById(R.id.user_icon);
        final ImageView bv = v.findViewById(R.id.background);
        ParallelTask<Void, twitter4j.User> pt = new ParallelTask<Void, twitter4j.User>() {
            @Override
            protected void onPreExecute() {
                userName.setText("@" + TwitterUtils.getCurrentAccount().getScreenName());
            }

            @Override
            protected twitter4j.User doInBackground() {
                try {
                    return TwitterUtils.getTwitterInstance().showUser(TwitterUtils.getCurrentAccountId());
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(User user) {
                if (user == null) return;
                AppUtil.setImage(iv, user.getOriginalProfileImageURLHttps());
                AppUtil.setImage(bv, user.getProfileBannerURL());
            }
        };
        bv.setOnClickListener((view) -> {
            Intent intent = new Intent(MainActivity.this, UserDetailActivity.class);
            intent.putExtra(C.SCREEN_NAME, TwitterUtils.getCurrentAccount().getScreenName());
            startActivity(intent);
            mDrawerLayout.closeDrawer(GravityCompat.START);
        });
        pt.executeParallel();
    }

    public boolean isCurrentTab(long id) {
        return mAdapter.findPositionById(id) == mPager.getCurrentItem();
    }

    private List<AbstractTimelineFragment> getFragments() {
        List<AbstractTimelineFragment> list = new ArrayList<>();
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof AbstractTimelineFragment) list.add((AbstractTimelineFragment) f);
        }
        return list;
    }

    private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            //TODO display search history
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            Intent i = new Intent(getApplicationContext(), SearchActivity.class);
            i.putExtra(C.QUERY, query);
            startActivity(i);
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
    };
}