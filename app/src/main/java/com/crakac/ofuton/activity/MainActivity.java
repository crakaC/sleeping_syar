package com.crakac.ofuton.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
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
import com.crakac.ofuton.util.PreferenceUtil;
import com.crakac.ofuton.util.RelativeTimeUpdater;
import com.crakac.ofuton.util.ReloadChecker;
import com.crakac.ofuton.util.TwitterList;
import com.crakac.ofuton.util.TwitterUtils;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {
    public static final long TAB_ID_FAVORITE = 1;
    public static final long TAB_ID_MENTION = 2;
    public static final long TAB_ID_HOME = 3;

    private static final String TAG = MainActivity.class.getSimpleName();
    private PagerSlidingTabStrip mTabs;
    private ViewPager mPager;
    private TimelineFragmentPagerAdapter mAdapter;
    private Menu mMenu;
    private SearchView mSearchView;
    FloatingActionButton mTweetBtn;
    /* Navigation drawer */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment mTweetFragment, mDummyFragment;

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
        mTweetBtn = (FloatingActionButton) findViewById(R.id.tweetEveryWhere);// 右下のツイートボタン
        mTweetBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TweetActivity.class);
                startActivity(intent);
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        mDummyFragment = fm.findFragmentByTag("dummy");
        if (mDummyFragment == null){
            mDummyFragment = TweetFragment.getDummy();
        }
        mTweetFragment = fm.findFragmentByTag("tweet");
        if (mTweetFragment == null){
            mTweetFragment = new TweetFragment();
        }

        int counts = fm.getBackStackEntryCount();
        if (counts == 0) {
            Log.i("Fragment BackStack", "added 2 fragments");
            fm.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.quick_tweet, mTweetFragment, "tweet")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
            fm.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.quick_tweet, mDummyFragment, "dummy")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
        mTweetBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                for(AbstractTimelineFragment f : getFragments()){
                    f.enableFabListener(false);
                }
                mTweetBtn.hide();
                getSupportFragmentManager().popBackStack();
                return true;
            }
        });

        /* actionbar */
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);
        createNavigationDrawer();

        mTabs = (PagerSlidingTabStrip) findViewById(R.id.pagerTabStrip);
        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TimelineFragmentPagerAdapter(this, mPager);
        mTabs.setOnPageChangeListener(new RelativeTimeUpdater(mAdapter));
        setPages(mAdapter);
        if (savedInstanceState == null) {
            mPager.setCurrentItem(getFragmentPosition(TAB_ID_HOME));// HomeTimelineの位置に合わせる
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mPager.setOffscreenPageLimit(12);// 保持するFragmentの数を指定．全フラグメントを保持するのでぬるぬる動くがメモリを食う
        }
        mTabs.setViewPager(mPager);// PagerSlidingTabStripにViewPagerをセット．

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Resources res = getResources();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else {
                int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
                int navHeight = res.getDimensionPixelSize(resourceId);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTweetBtn.getLayoutParams();
                params.bottomMargin += navHeight;
                mTweetBtn.setLayoutParams(params);
            }
        }
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
        showRefreshMenu(PreferenceUtil.getBoolean(R.string.enable_refresh_btn));
        if (PreferenceUtil.getBoolean(R.string.always_awake) && PreferenceUtil.getBoolean(R.string.streaming_mode)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        AppUtil.closeSearchView(mSearchView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TweetStatusAdapter.shouldShowInlinePreview(PreferenceUtil.getBoolean(R.string.show_image_in_timeline, true));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ReloadChecker.shouldSoftReload()) {
            ReloadChecker.reset();
            for (AbstractTimelineFragment f : getFragments()) {
                f.getViews();
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
        showRefreshMenu(PreferenceUtil.getBoolean(R.string.enable_refresh_btn));
        MenuItem search = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(search);
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mSearchView != null && !mSearchView.isIconified()) {
            AppUtil.closeSearchView(mSearchView);
        } else if (mTweetFragment.isVisible()) {
            if(((TweetFragment)mTweetFragment).hasFocus()){
                ((TweetFragment) mTweetFragment).clearFocus();
                return;
            }
            mTweetBtn.show();
            for (AbstractTimelineFragment f :  getFragments()){
                f.enableFabListener(true);
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.quick_tweet, mDummyFragment).addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        } else if (getSupportFragmentManager().getBackStackEntryCount() == 2) {
            finish();
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
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        MenuAdapter adapter = new MenuAdapter(this);
        adapter.add(new SlideMenu("Home", R.drawable.ic_home_white_36dp, MenuID.home));
        adapter.add(new SlideMenu("Mentions", R.drawable.ic_reply_all_white_36dp, MenuID.mentions));
        adapter.add(new SlideMenu("Favorites", R.drawable.ic_star_white_36dp, MenuID.favorites));
        adapter.add(new SlideMenu("DM", R.drawable.ic_email_white_36dp, MenuID.dm));
        adapter.add(new SlideMenu("Lists", R.drawable.ic_list_white_36dp, MenuID.lists));
        adapter.add(new SlideMenu("Accounts", R.drawable.ic_group_white_36dp, MenuID.account));
        adapter.add(new SlideMenu("Settings", R.drawable.ic_settings_white_36dp, MenuID.settings));
        adapter.add(new SlideMenu("( ˘ω˘)ｽﾔｧ…", R.drawable.ic_syar, MenuID.syar));
        mDrawerList.setAdapter(adapter);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
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
    }

    public boolean isCurrentTab(long id) {
        return mAdapter.findPositionById(id) == mPager.getCurrentItem();
    }

    private enum MenuID {
        account, home, mentions, favorites, dm, lists, settings, syar
    }

    private class SlideMenu {
        private String mText;
        private int mIconId;
        private MenuID mId;

        public SlideMenu(String text, int resId, MenuID menuId) {
            mText = text;
            mIconId = resId;
            mId = menuId;
        }

        public String getText() {
            return mText;
        }

        public int getIconId() {
            return mIconId;
        }

        public MenuID getMenuId() {
            return mId;
        }
    }

    public class MenuAdapter extends ArrayAdapter<SlideMenu> {
        private LayoutInflater mInflater;

        public MenuAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
            mInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SlideMenu item = getItem(position);
            if (item.getMenuId() == MenuID.syar) {
                convertView = mInflater.inflate(R.layout.slide_menu_syar, null);
            } else {
                convertView = mInflater.inflate(R.layout.slide_menu_item, null);
                TextView text = (TextView) convertView.findViewById(R.id.menu_name);
                text.setText(item.getText());
                ImageView icon = (ImageView) convertView.findViewById(R.id.menu_icon);
                icon.setImageResource(item.getIconId());
            }
            return convertView;
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SlideMenu item = (SlideMenu) parent.getItemAtPosition(position);
            switch (item.getMenuId()) {
                case account:
                    startActivity(new Intent(MainActivity.this, AccountActivity.class));
                    break;
                case home:
                    mPager.setCurrentItem(getFragmentPosition(TAB_ID_HOME));
                    break;
                case mentions:
                    mPager.setCurrentItem(getFragmentPosition(TAB_ID_MENTION));
                    break;
                case favorites:
                    mPager.setCurrentItem(getFragmentPosition(TAB_ID_FAVORITE));
                    break;
                case dm:
                    startActivity(new Intent(MainActivity.this, DmActivity.class));
                    break;
                case lists:
                    startActivity(new Intent(MainActivity.this, ListSelectActivity.class));
                    break;
                case settings:
                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                    break;
                case syar:
                    AppUtil.syar();
                    break;
                default:
                    break;
            }
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
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

    public FloatingActionButton getTweetButton(){
        return mTweetBtn;
    }

    public boolean isTranslucentNav(){
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT );
    }

    public int getNavHeight(){
        Resources res = getResources();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        return res.getDimensionPixelSize(resourceId);
    }
}