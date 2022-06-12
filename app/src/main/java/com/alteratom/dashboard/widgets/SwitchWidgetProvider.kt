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
        override val PREFS_NAME = "switch"

        override fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_button)

            views.setOnClickPendingIntent(
                R.id.widget_root,
                getPendingSelfIntent(context, "c", SwitchWidgetProvider::class.java)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
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

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            delete(context, id)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.getAction() == "c") {
            when (G.dashboard.daemon) {
                is Mqttd -> {
                    if (ForegroundService.service?.isRunning == true) {
                        (G.dashboard.daemon as? Mqttd?)?.publish("gda_switch0b", "")
                    } else createToast(context!!, "err")
                }
            }
        }
    }
}