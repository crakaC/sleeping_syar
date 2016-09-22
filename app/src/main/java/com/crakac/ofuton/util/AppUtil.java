package com.crakac.ofuton.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crakac.ofuton.C;
import com.crakac.ofuton.R;
import com.crakac.ofuton.adapter.TweetStatusAdapter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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

public final class AppUtil {
    private static Context sContext;

    private AppUtil(){}
    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static void showToast(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(sContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showToast(final int resId) {
        showToast(getString(resId));
    }

    public static boolean existsPackage(String packageName) {
        PackageManager pm = sContext.getPackageManager();
        try {
            pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public static String shapingNums(int num) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        return nf.format(num);
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
        if (PrefUtil.getBoolean(R.string.use_bigger_icon)) {
            url = user.getBiggerProfileImageURLHttps();
        } else {
            url = user.getProfileImageURLHttps();
        }
        return url;
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
            return diff / MINUTE + "m";
        } else if (diff < DAY) {
            long minites = diff % HOUR / MINUTE;
            if(minites == 0){
                return (diff / HOUR) + "h";
            } else {
                return (diff / HOUR) + "h" + (diff % HOUR / MINUTE) + "m";
            }
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
            text = text.replace(entity.getURL(), "<font color=#55acee>" + entity.getDisplayURL() + "</font>");
        }
        for (MediaEntity entity : entitySupport.getMediaEntities()) {
            text = text.replace(entity.getURL(), "<font color=#55acee>" + entity.getDisplayURL() + "</font>");
        }
        text = text.replace("\n", "<br/>");
        return Html.fromHtml(text);
    }

    public static String trimUrl(twitter4j.Status status) {
        String text = status.getText();
        for (MediaEntity entity : TwitterUtils.getMediaEntities(status)) {
            if (entity instanceof GuessedMediaEntity) continue;//pic.twitter.com以外はテキストを残す．
            text = text.replace(entity.getURL(), "");
        }
        return text.trim();
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
                return PrefUtil.getInt(R.string.syar);
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
                    PrefUtil.getSharedPreference().edit().putInt(getString(R.string.syar), getSyarCount() + 1).apply();
                }
            }
        };
        task.executeParallel();

    }

    public static int getMemoryMB() {
        int memory = (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));
        Log.w("memory MB", memory + "");
        return memory;
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

    public static void fadeout(final View v, long duration) {
        AlphaAnimation a = new AlphaAnimation(1f, 0f);
        a.setDuration(duration);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (v.isShown()) v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        v.startAnimation(a);
    }

    public static void fadein(final View v, long duration) {
        AlphaAnimation a = new AlphaAnimation(0f, 1f);
        a.setDuration(duration);
        v.setVisibility(View.VISIBLE);
        v.startAnimation(a);
    }

    public static void slideOut(final View v, long duration) {
        Animation a = AnimationUtils.loadAnimation(sContext, R.anim.slide_out);
        a.setDuration(duration);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (v.isShown()) v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        v.startAnimation(a);
    }

    public static void slideIn(final View v, long duration) {
        Animation a = AnimationUtils.loadAnimation(sContext, R.anim.slide_in);
        a.setDuration(duration);
        v.setVisibility(View.VISIBLE);
        v.startAnimation(a);
    }

    public static int dpToPx(int dp) {
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static float pxToDp(int px) {
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (float) px / scale;
    }

    public static void setActionBarIcon(ActionBar actionbar, ImageView view) {
        final int ACTIONBAR_ICON_DP = 32;
        BitmapDrawable b = (BitmapDrawable) view.getDrawable();
        Bitmap bm = b.getBitmap();
        int length = dpToPx(ACTIONBAR_ICON_DP);
        Bitmap resized = Bitmap.createScaledBitmap(bm, length, length, true);
        actionbar.setIcon(new BitmapDrawable(sContext.getResources(), resized));
    }

    public static void closeSearchView(SearchView searchView) {
        searchView.setQuery(null, false);
        searchView.setIconified(true);
    }

    public static File convertUriToFile(Context context, Uri uri){
        File imageFile = null;
        if (uri.getScheme().equals("file")) {
            try {
                imageFile = new File(new URI(uri.toString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            c.moveToFirst();
            imageFile = new File(c.getString(0));
        }
        return imageFile;
    }

    public static void checkTofuBuster() {
        if(existsPackage(getString(R.string.tofubuster_package))){
            PrefUtil.put(R.string.pref_tofu, true);
        } else {
            PrefUtil.put(R.string.pref_tofu, false);
        }
    }

    public static boolean existsTofuBuster(){
        return PrefUtil.getBoolean(R.string.pref_tofu);
    }

    public static interface SyarListener {

        void preSyar();

        void postSyar();
    }

}