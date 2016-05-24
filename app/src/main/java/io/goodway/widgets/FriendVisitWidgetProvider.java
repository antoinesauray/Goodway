package io.goodway.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import io.goodway.R;
import io.goodway.activities.WayActivity;
import io.goodway.navitia_android.Address;

/**
 * Created by Antoine Sauray on 5/23/2016.
 */

public class FriendVisitWidgetProvider extends AppWidgetProvider {

    public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // TODO Auto-generated method stub
        final int count = appWidgetIds.length;
        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.friendvisit_appwidget);
            Intent intent = new Intent(context, FriendVisitWidgetProvider.class);
            intent.setAction(ACTION_WIDGET_RECEIVER);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        /*
        Log.d("update widget", "update widget");
        for(int i=0;i<appWidgetIds.length;i++){
            RemoteViews remoteViews;
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.friendvisit_appwidget);
            Log.d("update widget", "id="+appWidgetIds[i]);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            double latitude = prefs.getFloat("latitude_"+appWidgetIds[i], 0);
            double longitude = prefs.getFloat("longitude_"+appWidgetIds[i], 0);
            String name = prefs.getString("user_name_"+appWidgetIds[i], "");
            String location = prefs.getString("location_"+appWidgetIds[i], "");

            Intent intent = new Intent(context, FriendVisitWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            intent.putExtra(ACTION_WIDGET_RECEIVER, ACTION_WIDGET_RECEIVER);
            intent.putExtra("address", new Address(name, latitude, longitude));

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        */
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.d("SWitch Widget", "On Receive");
        Log.d("ACTION", intent.getAction());
        final String action = intent.getAction();

        if (ACTION_WIDGET_RECEIVER.equals(action))
        {
            Log.d("YES", "YES");
            Address arrival = intent.getExtras().getParcelable("address");
            Log.d("address to send", arrival.toString());
            Intent i = new Intent(context, WayActivity.class);
            i.putExtra("arrival", arrival);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        super.onReceive(context, intent);
    }
}