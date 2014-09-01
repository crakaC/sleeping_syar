package com.crakac.ofuton;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.RemoteViews;

import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.AppUtil.SyarListener;
import com.crakac.ofuton.util.TwitterUtils;

public class SyarWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Intent i = new Intent(context, SyarService.class);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.startService(i);
        Log.d("SyarWidget", "onUpdate");
    }

    public static class SyarService extends Service {
        private static final String SYAR_ACTION = "SYAR_ACTION";
        private SparseBooleanArray mIsRunning = new SparseBooleanArray();
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.d("SyarService", "[onStartCommand] intent:" + (intent != null ? intent.toString() : "null") + "action:"
                    + (intent != null ? intent.getAction() : "null"));

            final RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_1x1);
            setPendingIntent(rv, intent);
            if (isSyarIntent(intent)) {
                int widgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
                Log.d("widgetId", widgetId + "");
                if (!mIsRunning.get(widgetId))
                    syarIfPossible(rv, widgetId);
            } else {
                stopSelf();
            }
            return START_NOT_STICKY;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d("SyarService", "onDestroy");
        }

        boolean isSyarIntent(Intent intent) {
            return intent != null && SYAR_ACTION.equals(intent.getAction());
        }

        void setPendingIntent(final RemoteViews rv, Intent intent) {
            if (isSyarIntent(intent))
                return;
            int[] appWidgetIds = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (appWidgetIds == null)
                return;
            for (int i = 0; i < appWidgetIds.length; i++) {
                int widgetId = appWidgetIds[i];
                Intent syarIntent = new Intent();
                syarIntent.setAction(SYAR_ACTION);
                syarIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                PendingIntent pi = PendingIntent.getService(this, widgetId, syarIntent, 0);
                rv.setOnClickPendingIntent(R.id.container, pi);
                updateWidget(rv, widgetId);
                mIsRunning.delete(widgetId);
                mIsRunning.put(widgetId, false);
            }

        }

        void updateWidget(RemoteViews rv, int appWidgetId) {
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(appWidgetId, rv);
        }

        void updateWidgets(RemoteViews rv) {
            ComponentName thisWidget = new ComponentName(this, SyarWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, rv);
        }

        void syarIfPossible(final RemoteViews rv, final int widgetId) {
            if (!TwitterUtils.existsCurrentAccount()) {
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                stopSelf();
            } else {
                AppUtil.syar(new SyarListener() {

                    @Override
                    public void preSyar() {
                        mIsRunning.put(widgetId, true);
                        rv.setViewVisibility(R.id.syar, View.INVISIBLE);
                        rv.setViewVisibility(R.id.progressBar, View.VISIBLE);
                        Log.d("preSyar", "preSyar");
                        updateWidget(rv, widgetId);
                    }

                    @Override
                    public void postSyar() {
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                rv.setViewVisibility(R.id.syar, View.VISIBLE);
                                rv.setViewVisibility(R.id.progressBar, View.INVISIBLE);
                                Log.d("postSyar", "postSyar");
                                updateWidget(rv, widgetId);
                                mIsRunning.put(widgetId, false);
                                stopSelf();
                            }
                        }, 200);
                    }
                });
            }

        }

    }
}
