package com.crakac.ofuton;

import java.util.ArrayList;
import java.util.Stack;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.crakac.ofuton.timeline.AbstractTimelineFragment;
import com.crakac.ofuton.timeline.FavoriteTimelineFragment;
import com.crakac.ofuton.timeline.HomeTimelineFragment;
import com.crakac.ofuton.timeline.ListTimelineFragment;
import com.crakac.ofuton.timeline.MentionsTimelineFragment;

public class TimelineFragmentPagerAdapter extends FragmentPagerAdapter {

	private static final String TAG = TimelineFragmentPagerAdapter.class.getName();
	private ArrayList<AbstractTimelineFragment> mFragments;

	public TimelineFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		mFragments = new ArrayList<AbstractTimelineFragment>();
		if(fm.getFragments() != null){
			Stack<AbstractTimelineFragment> stack = new Stack<AbstractTimelineFragment>();
			for( Fragment f : fm.getFragments() ){
				if( f instanceof FavoriteTimelineFragment || f instanceof MentionsTimelineFragment || f instanceof HomeTimelineFragment){
					stack.push((AbstractTimelineFragment) f);
				} else if ( f instanceof AbstractTimelineFragment){
					if(stack.size() == 3){
						for(int i = 0; i < 3; i++) add(stack.pop());
					}
					add((AbstractTimelineFragment)f);
				}
			}
		}
	}

	@Override
	public Fragment getItem(int i) {
		return mFragments.get(i);
	}

	@Override
	public int getCount() {
		/* なぜかfragments.size()と実際の要素の数がずれる問題があったけどコンストラクタでinstanceofを使うことで解決できた
		// for(int i = 0; i < fragments.size(); i++){
		// if(fragments.get(i) == null){
		// Log.d(TAG, "fragments[" + i + "] is null");
		// fragments.remove(i);
		// }
		// }
		 */
		return mFragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Log.d(TAG,position + ":"+ ((AbstractTimelineFragment) getItem(position)).getTimelineName() );
		return mFragments.get(position).getTimelineName();
	}

	public void add(AbstractTimelineFragment fragment){
		mFragments.add(fragment);
		notifyDataSetChanged();
	}
	/**
	 * add user's List.
	 * @param listId List ID
	 * @param title List title. It is shown in Tab.
	 */
	public void addList(long listId, String title){
		ListTimelineFragment lf = new ListTimelineFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(C.LIST_ID, listId);
		bundle.putString(C.LIST_TITLE, title);
		lf.setArguments(bundle);
		add(lf);
	}

	
	
	public ArrayList<AbstractTimelineFragment> getFragments(){
		return mFragments;
	}
	
	public int getFragmentPosition(Class<? extends Fragment> c){
		int pos = -1;
		for(int i = 0; i < mFragments.size(); i++){
			if(c.isInstance(mFragments.get(i))){
				pos = i;
                break;
			}
		}
		if(pos < 0){
			throw new RuntimeException("target fragment doesn't exist");
		}
		return pos;
	}
    public int getFragmentPosition(Fragment f){
        int pos = -1;
        for(int i = 0; i < mFragments.size(); i++){
            if(f.equals(mFragments.get(i))){
                pos = i;
                break;
            }
        }
        return pos;
    }

}
