package com.crakac.ofuton.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.crakac.ofuton.R;

/**
 * Created by kosukeshirakashi on 2014/09/12.
 */
public class PrefUtil {

    private static Context sContext;
    private static String FONT_SIZE_KEY;

    public static void init(Context context){
        sContext = context;
        FONT_SIZE_KEY = sContext.getString(R.string.font_size);

    }
    public static boolean getBoolean(int resId) {
        return getBoolean(resId, false);
    }

    public static boolean getBoolean(int resId, boolean defaultValue) {
        return getSharedPreference().getBoolean(sContext.getString(resId), defaultValue);
    }

    static SharedPreferences getSharedPreference() {
        return PreferenceManager.getDefaultSharedPreferences(sContext);
    }

    public static SharedPreferences getSharedPreference(String preferenceName){
        return sContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    public static int getInt(int resId) {
        return getSharedPreference().getInt(AppUtil.getString(resId), 0);
    }

    public static String getString(String key, String defValue){
        return getSharedPreference().getString(key, defValue);
    }

    public static String getString(int keyResId, int defResId){
        return getSharedPreference().getString(sContext.getResources().getString(keyResId), sContext.getResources().getString(defResId));
    }

    public static String getString(int keyResId){
        return getSharedPreference().getString(sContext.getResources().getString(keyResId), null);
    }

    public static void put(int prefKeyResid, boolean value){
        getSharedPreference().edit().putBoolean(sContext.getString(prefKeyResid), value).apply();
    }

    /**
     * SharedPreferenceからフォントサイズを読み込む
     *
     * @return
     */
    public static int getFontSize() {
        return Integer.valueOf(getString(FONT_SIZE_KEY, "13"));// ListPreferenceは値をStringで保存すると思われるのでこうする
    }

    public static float getLargeFontSize(){
        return getFontSize() * 1.125f;
    }

    /**
     * 日付表示とかの小さい文字のフォントサイズを読み込む
     *
     * @return
     */
    public static float getSubFontSize() {
        return getFontSize() * 0.875f;
    }
}

