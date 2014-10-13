package com.crakac.ofuton.fragment.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SimpleFragmentPagerAdapter<T extends Fragment> extends FragmentStatePagerAdapter {
    protected ArrayList<T> mFragments;

    public SimpleFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new ArrayList<>();
    }

    @Override
    public T getItem(int cnt) {
        return mFragments.get(cnt);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public int getItemPosition(Object obj) {
        int pos = -1;
        for (int i = 0; i < getCount(); i++) {
            if (obj.equals(getItem(i))) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public void add(T fragment) {
        mFragments.add(fragment);
        notifyDataSetChanged();
    }

    public boolean isEmpty(){
        return mFragments.size() == 0;
    }
}