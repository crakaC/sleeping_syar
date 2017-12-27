package com.crakac.ofuton.fragment.adapter;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleFragmentPagerAdapter<T extends Fragment> extends FragmentPagerAdapter {
    private final Context mContext;
    private final ArrayList<TabInfo> mTabs = new ArrayList<>();
    private final HashMap<Integer, T> mFragments = new HashMap<>();
    private final FragmentManager mFragmentManager;

    public SimpleFragmentPagerAdapter(AppCompatActivity context, ViewPager viewPager) {
        super(context.getSupportFragmentManager());
        mFragmentManager = context.getSupportFragmentManager();
        viewPager.setAdapter(this);
        mContext = context;
    }

    public T get(int position){
        return mFragments.get(position);
    }

    @Override
    public T getItem(int cnt) {
        TabInfo info = mTabs.get(cnt);
        T fragment = (T) Fragment.instantiate(mContext, info.clazz.getName(), info.mArgs);
        mFragments.put(cnt, fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public int getItemPosition(Object obj) {
        for(Map.Entry<Integer, T>entry : mFragments.entrySet()){
            if(obj.equals(entry.getValue())){
                return entry.getKey();
            }
        }
        return -1;
    }

    public int findPositionById(long id) {
        for (int i = 0; i < mTabs.size(); i++) {
            if (mTabs.get(i).mId == id) {
                return i;
            }
        }
        return -1;
    }

    public List<Fragment> getFragments(){
        return mFragmentManager.getFragments();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs.get(position).mTitle;
    }

    public void add(Class<? extends T> clazz, long id) {
        add(clazz, null, null, id);
    }

    public void add(Class<? extends T> clazz, Bundle args, long id) {
        add(clazz, args, null, id);
    }

    public void add(Class<? extends  T> clazz, Bundle args, String title, long id) {
        TabInfo info = new TabInfo(clazz, args, title, id);
        mTabs.add(info);
    }

    public boolean isEmpty() {
        return mTabs.isEmpty();
    }

    private static final class TabInfo {
        private final Class<? extends Fragment> clazz;
        private final Bundle mArgs;
        final String mTitle;
        final long mId;

        TabInfo(Class<? extends Fragment> clazz, Bundle args, String title, long id) {
            this.clazz = clazz;
            mArgs = args;
            mTitle = title;
            mId = id;
        }
    }
}