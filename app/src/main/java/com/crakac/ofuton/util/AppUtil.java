package com.crakac.ofuton.util;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import twitter4j.EntitySupport;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crakac.ofuton.R;
import com.crakac.ofuton.status.TweetStatusAdapter;

public final class AppUtil {
    private static Context sContext;
    private static String FONT_SIZE_KEY;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
        FONT_SIZE_KEY = sContext.getString(R.string.font_size);
    }

    public static void showToast(String msg) {
        Toast.makeText(sContext, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int resId) {
        Toast.makeText(sContext, getString(resId), Toast.LENGTH_SHORT).show();
    }

    public static String shapingNums(int num) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        return nf.format(num);
    }

    /**
     * SharedPreferenceからフォントサイズを読み込む
     *
     * @return
     */
    public static int getFontSize() {
        return Integer.valueOf(getSharedPreference().getString(FONT_SIZE_KEY, "12"));// ListPreferenceは値をStringで保存すると思われるのでこうする
    }

    public static boolean getPreferenceValue(String key) {
        return getSharedPreference().getBoolean(key, false);
    }

    public static boolean getBooleanPreference(int resId) {
        return getBooleanPreference(resId, false);
    }

    public static boolean getBooleanPreference(int resId, boolean defaultValue) {
        return getSharedPreference().getBoolean(sContext.getString(resId), defaultValue);
    }

    private static SharedPreferences getSharedPreference() {
        return PreferenceManager.getDefaultSharedPreferences(sContext);
    }

    public static int getIntPreference(int resId) {
        return getSharedPreference().getInt(getString(resId), 0);
    }

    /**
     * Get icon url from user object.
     * 設定によって大きいアイコンを使うときはちゃんと大きいアイコンの方のURLを引っ張ってくる．
     *
     * @param user
     * @return the icon URL
     */
    public static String getIconURL(twitter4j.User user) {
        String url = null;
        if (getBooleanPreference(R.string.use_bigger_icon)) {
            url = user.getBiggerProfileImageURLHttps();
        } else {
            url = user.getProfileImageURLHttps();
        }
        return url;
    }

    /**
     * 日付表示とかの小さい文字のフォントサイズを読み込む
     *
     * @return
     */
    public static float getSubFontSize() {
        return (float) Math.ceil(getFontSize() * 0.8);
    }

    public static void showStatus(Status status) {
        Toast t = new Toast(sContext);
        View v = TweetStatusAdapter.createView(status, null);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 5, 10, 5);
        v.setLayoutParams(lp);
        v.requestLayout();
        t.setView(v);
        t.setDuration(Toast.LENGTH_LONG);
        t.show();
    }

    @SuppressLint("SimpleDateFormat")
    @SuppressWarnings("deprecation")
    public static String dateToRelativeTime(Date date) {
        final long SEC = 1000;
        final long MINUTE = SEC * 60;
        final long HOUR = MINUTE * 60;
        final long DAY = HOUR * 24;
        long diff = System.currentTimeMillis() - date.getTime();
        int diffYear = new Date().getYear() - date.getYear();
        if (diff < SEC) {
            return "just now";
        } else if (diff < MINUTE) {
            return diff / SEC + "s";
        } else if (diff < HOUR) {
            return diff / MINUTE + "m";// + ( diff % MINUTE / SEC ) + "s";
        } else if (diff < DAY) {
            return (diff / HOUR) + "h" + (diff % HOUR / MINUTE) + "m";
        } else if (diffYear == 0) {
            return new SimpleDateFormat("M/d").format(date);
        } else {
            return new SimpleDateFormat("yyyy/M/d").format(date);
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String dateToAbsoluteTime(Date date) {
        Calendar now = new GregorianCalendar();
        Calendar postedAt = new GregorianCalendar();
        postedAt.setTime(date);
        int diffYear = now.get(Calendar.YEAR) - postedAt.get(Calendar.YEAR);
        int diffDay = new GregorianCalendar().get(Calendar.DAY_OF_YEAR) - postedAt.get(Calendar.DAY_OF_YEAR);
        SimpleDateFormat sdf;
        if (diffYear == 0) {
            if (diffDay == 0) {
                sdf = new SimpleDateFormat("HH:mm:ss");
            } else {
                sdf = new SimpleDateFormat("M/d HH:mm");
            }
        } else {
            sdf = new SimpleDateFormat("yyyy/M/d HH:mm");
        }
        return sdf.format(date);
    }

    public static Spanned getColoredText(String text, EntitySupport entitySupport) {
        text = TextUtils.htmlEncode(text);// <>とかが含まれてると良くないのでhtmlEncodeする．
        for (URLEntity entity : entitySupport.getURLEntities()) {
            text = text.replace(entity.getURL(), "<font color=#33b5e5>" + entity.getDisplayURL() + "</font>");
        }
        for (MediaEntity entity : entitySupport.getMediaEntities()) {
            text = text.replace(entity.getURL(), "<font color=#33b5e5>" + entity.getDisplayURL() + "</font>");
        }
        text = text.replace("\n", "<br/>");
        return Html.fromHtml(text);
    }

    public static String getString(int resId) {
        return sContext.getResources().getString(resId);
    }

    /**
     * ( ˘ω˘)スヤァとツイートするだけのやつ
     */
    public static void syar() {
        syar(null);
    }

    public static void syar(final SyarListener listener) {
        ParallelTask<Void, Void, twitter4j.Status> task = new ParallelTask<Void, Void, twitter4j.Status>() {
            String syar;

            private int getSyarCount() {
                return AppUtil.getIntPreference(R.string.syar);
            }

            @Override
            protected void onPreExecute() {
                if (listener != null) {
                    listener.preSyar();
                }
                StringBuilder sb = new StringBuilder();
                Random r = new Random();
                if (r.nextInt(10) == 0) {
                    sb.append("＿人人人人人人＿\n＞　( ˘ω˘)ｽﾔｧ　＜\n￣Y^Y^Y^Y^Y^Y￣");
                } else {
                    sb.append(getString(R.string.syar));
                }
                syar = sb.toString();
            }

            @Override
            protected twitter4j.Status doInBackground(Void... arg) {
                try {
                    return TwitterUtils.getTwitterInstance().updateStatus(syar);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(twitter4j.Status result) {
                if (listener != null) {
                    listener.postSyar();
                }
                if (result == null) {
                    AppUtil.showToast(R.string.something_wrong);
                } else {
                    AppUtil.showToast(syar);
                    getSharedPreference().edit().putInt(getString(R.string.syar), getSyarCount() + 1).commit();
                }
            }
        };
        task.executeParallel();

    }

    public static interface SyarListener {

        void preSyar();

        void postSyar();
    }

    public static void showView(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    public static void hideView(View... views) {
        for (View v : views) {
            v.setVisibility(View.INVISIBLE);
        }
    }

    public static int dpToPx(int dp) {
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static float pxToDp(int px){
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return  (float)px / scale;
    }

    public static void setActionBarIcon(ActionBar actionbar, ImageView view) {
        final int ACTIONBAR_ICON_DP = 32;
        BitmapDrawable b = (BitmapDrawable) view.getDrawable();
        Bitmap bm = b.getBitmap();
        int length = dpToPx(ACTIONBAR_ICON_DP);
        Bitmap resized = Bitmap.createScaledBitmap(bm, length, length, true);
        actionbar.setIcon(new BitmapDrawable(sContext.getResources(), resized));

    }
}