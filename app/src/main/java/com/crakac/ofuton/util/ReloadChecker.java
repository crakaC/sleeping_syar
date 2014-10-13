package com.crakac.ofuton.util;

import android.util.Log;

/**
 * メインアクティビティのリロードに使う．
 * @author Kosuke
 *
 */
public class ReloadChecker {
	private static final String TAG = ReloadChecker.class.getSimpleName();
	private static boolean hardReload= false;
	private static boolean softReload = false;

	public static boolean shouldHardReload() {
		Log.d(TAG, "isChanged() : " + hardReload);
		return hardReload;
	}

	public static boolean shouldSoftReload() {
	    return softReload;
	}

	public static void reset(){
		hardReload = false;
		softReload = false;
	}

	public static void requestHardReload(boolean flag) {
		hardReload = flag;
		Log.d(TAG, "changed() : " + hardReload);
	}

	public static void requestSoftReload(boolean flag){
	    softReload = flag;
	}
}
