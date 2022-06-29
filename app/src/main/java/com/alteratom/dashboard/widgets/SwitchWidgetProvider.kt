package com.alteratom.dashboard.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.alteratom.R
import com.alteratom.dashboard.G
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.foreground_service.ForegroundService
import com.alteratom.dashboard.foreground_service.demons.Mqttd

class SwitchWidgetProvider : AppWidgetProvider() {

    companion object : WidgetProviderCompanion() {
        override val type = "switch"

        override fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_button)

            views.setOnClickPendingIntent(
                R.id.widget_root,
                getPendingSelfIntent(context, ShortWidgetProvider::class.java, appWidgetId)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == "self" && context != null) {
            val id = intent.getIntExtra("id", -1)

        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    class Data : WidgetDataHolder.Data() {
    }
}