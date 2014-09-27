package com.crakac.ofuton;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.crakac.ofuton.accounts.AccountActivity;
import com.crakac.ofuton.dm.DmActivity;
import com.crakac.ofuton.lists.ListSelectActivity;
import com.crakac.ofuton.timeline.AbstractTimelineFragment;
import com.crakac.ofuton.timeline.FavoriteTimelineFragment;
import com.crakac.ofuton.timeline.HomeTimelineFragment;
import com.crakac.ofuton.timeline.MentionsTimelineFragment;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.PreferenceUtil;
import com.crakac.ofuton.util.TwitterList;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.widget.ColorOverlayOnTouch;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AbstractPtrFragment mSearchFragment;
    private PagerSlidingTabStrip mTabs;
    private ViewPager mPager;
    private TimelineFragmentPagerAdapter mAdapter;
    private ImageView mTweetBtn;
    private Menu mMenu;
    private SearchView mSearchView;
    /* Navigation drawer */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

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
        setContentView(R.layout.activity_main);
        mTweetBtn = (ImageView) findViewById(R.id.tweetEveryWhere);// 右下のツイートボタン
        mTweetBtn.setOnTouchListener(new ColorOverlayOnTouch(PorterDuff.Mode.SRC_ATOP));
        mTweetBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TweetActivity.class);
                startActivity(intent);
            }
        });

        /* actionbar */
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);
        createNavigationDrawer();

        mTabs = (PagerSlidingTabStrip) findViewById(R.id.pagerTabStrip);
        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TimelineFragmentPagerAdapter(getSupportFragmentManager());
        mTabs.setOnPageChangeListener(new RelativeTimeUpdater(mAdapter));
        setPages(mAdapter);
        mPager.setAdapter(mAdapter);// ViewPagerにアダプタ(fragment)をセット．
        mPager.setCurrentItem(getFragmentPosition(HomeTimelineFragment.class));// HomeTimelineの位置に合わせる
        if(AppUtil.getMemoryMB() > 30){
            mPager.setOffscreenPageLimit(mAdapter.getCount());// 保持するFragmentの数を指定．全フラグメントを保持するのでぬるぬる動くがメモリを食う
        }
        mTabs.setViewPager(mPager);// PagerSlidingTabStripにViewPagerをセット．
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
        if(PreferenceUtil.getBoolean(R.string.always_awake) && PreferenceUtil.getBoolean(R.string.streaming_mode)){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ReloadChecker.shouldSoftReload()){
            ReloadChecker.reset();
            for(AbstractTimelineFragment f : mAdapter.getFragments()){
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
        this.mMenu = menu;
        showRefreshMenu(PreferenceUtil.getBoolean(R.string.enable_refresh_btn));
        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
        mSearchView.setOnCloseListener(mOnCloseListener);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mSearchView != null && !mSearchView.isIconified()) {
            mSearchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            toggleDrawer();
            return true;
        case R.id.refresh:// reload
            for (AbstractTimelineFragment f : mAdapter.getFragments()) {
                f.refresh();
            }
            AppUtil.showToast("Refresh！");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * set pages to adapter
     * @param adapter
     */
    private void setPages(TimelineFragmentPagerAdapter adapter) {
        if (adapter.getFragments().isEmpty()) {
            adapter.add(setArguments(new FavoriteTimelineFragment()));
            adapter.add(setArguments(new MentionsTimelineFragment()));
            adapter.add(setArguments(new HomeTimelineFragment()));
            for (TwitterList list : TwitterUtils.getListsOfCurrentAccount()) {
                adapter.addList(list.getListId(), list.getName());
            }
        }
    }

    private AbstractTimelineFragment setArguments(AbstractTimelineFragment f) {
        Bundle args = new Bundle();
        long id = TwitterUtils.getCurrentAccountId();
        args.putLong(C.USER_ID, id);
        f.setArguments(args);
        return f;
    }

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

    private int getFragmentPosition(Class<? extends AbstractTimelineFragment> c) {
        if (mAdapter == null) {
            throw new RuntimeException("pagerAdapter is null");
        }
        return mAdapter.getFragmentPosition(c);
    }

    private void createNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        MenuAdapter adapter = new MenuAdapter(this);
        adapter.add(new SlideMenu("Home", R.drawable.ic_menu_home, MenuID.home));
        adapter.add(new SlideMenu("Mentions", R.drawable.ic_menu_mentions, MenuID.mentions));
        adapter.add(new SlideMenu("Favorites", R.drawable.ic_menu_favorite, MenuID.favorites));
        adapter.add(new SlideMenu("DM", R.drawable.ic_dm, MenuID.dm));
        adapter.add(new SlideMenu("Lists", R.drawable.ic_menu_lists, MenuID.lists));
        adapter.add(new SlideMenu("Accounts", R.drawable.ic_menu_accounts, MenuID.account));
        adapter.add(new SlideMenu("Settings", R.drawable.ic_menu_settings, MenuID.settings));
        adapter.add(new SlideMenu("( ˘ω˘)ｽﾔｧ…", R.drawable.ic_syar, MenuID.syar));
        mDrawerList.setAdapter(adapter);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close) {
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

    public boolean isCurrentTab(Fragment f){
        return mAdapter.getFragmentPosition(f) == mPager.getCurrentItem();
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
            if(item.getMenuId() == MenuID.syar){
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
                mPager.setCurrentItem(getFragmentPosition(HomeTimelineFragment.class));
                break;
            case mentions:
                mPager.setCurrentItem(getFragmentPosition(MentionsTimelineFragment.class));
                break;
            case favorites:
                mPager.setCurrentItem(getFragmentPosition(FavoriteTimelineFragment.class));
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

    private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            AppUtil.hideView(mTabs, mPager, mTweetBtn);
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
                AppUtil.showView(mTabs, mPager, mTweetBtn);
            }
            return false;
        }
    };
}