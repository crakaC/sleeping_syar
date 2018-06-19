package com.crakac.ofuton;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.crakac.ofuton.activity.MainActivity;
import com.crakac.ofuton.util.AppUtil;
import com.crakac.ofuton.util.TwitterUtils;
import com.crakac.ofuton.util.Util;

public class SyarWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_1x1);
        Intent syarIntent = new Intent(context, SyarService.class);
        for (int widgetId : appWidgetIds) {
            syarIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            PendingIntent pi = PendingIntent.getService(context, widgetId, syarIntent, 0);
            rv.setOnClickPendingIntent(R.id.container, pi);
        }
        appWidgetManager.updateAppWidget(appWidgetIds, rv);
    }

    public static class SyarService extends IntentService {
        private static String CHANNEL_ID = "syar_service";
        private static String CHANNEL_NAME = "syar";
        private static int SYAR_ID = 38;

        public SyarService() {
            super("SyarService");
        }

        @Override
        public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
            if (Util.isAfterOreo()) {
                Util.createNotificationChannel(this, CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
            builder.setSmallIcon(R.drawable.ic_send_white_24dp)
                    .setProgress(0, 0, true)
                    .setContentText(getString(R.string.syar))
                    .setOngoing(true);
            startForeground(SYAR_ID, builder.build());
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            if (!TwitterUtils.existsCurrentAccount()) {
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return;
            }
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            final RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_1x1);
            rv.setViewVisibility(R.id.syar, View.INVISIBLE);
            rv.setViewVisibility(R.id.progressBar, View.VISIBLE);
            AppWidgetManager.getInstance(this).updateAppWidget(widgetId, rv);
            AppUtil.syar();
            rv.setViewVisibility(R.id.syar, View.VISIBLE);
            rv.setViewVisibility(R.id.progressBar, View.INVISIBLE);
            AppWidgetManager.getInstance(this).updateAppWidget(widgetId, rv);
        }
    }
}
