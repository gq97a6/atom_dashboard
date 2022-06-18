package com.alteratom.dashboard.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.RemoteViews
import com.alteratom.R
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service
import com.alteratom.dashboard.foreground_service.demons.Mqttd


class ButtonWidgetProvider : AppWidgetProvider() {

    companion object : WidgetProviderCompanion() {
        var test = false
        override val PREFS_NAME = "button"

        override fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_button)

            views.setOnClickPendingIntent(
                R.id.widget_root,
                getPendingSelfIntent(
                    context,
                    "onClick",
                    appWidgetId,
                    ButtonWidgetProvider::class.java
                )
            )

            views.setInt(
                R.id.widget_root,
                "setBackgroundColor",
                if (test) Color.GREEN else Color.BLUE
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
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

        if (intent?.action == "onClick" && context != null) {
            try {
                when (dashboard.daemon) {
                    is Mqttd -> {
                        if (service?.isRunning == true) {
                            (dashboard.daemon as? Mqttd?)?.publish(
                                "gda_switch0s",
                                if (test) "1" else "0"
                            )
                        } else createToast(context!!, "err")
                    }
                }

                updateWidget(
                    context,
                    AppWidgetManager.getInstance(context),
                    intent.getIntExtra("id", -1)
                )

                test = !test
            } catch (e: Exception) {
                createToast(context!!, "Sever required")
            }
        }
    }
}

