package com.alteratom.dashboard.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.alteratom.R
import com.alteratom.dashboard.G.widgetDataHolder
import com.alteratom.dashboard.createToast

class ButtonWidgetProvider : AppWidgetProvider() {

    companion object : WidgetProviderCompanion() {
        override val type = "button"

        override fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_button)

            views.setOnClickPendingIntent(
                R.id.widget_root,
                getPendingSelfIntent(context, ButtonWidgetProvider::class.java, appWidgetId)
            )

            views.setInt(
                R.id.widget_root,
                "setBackgroundColor",
                when (false) {
                    true -> Color.RED
                    false -> Color.GREEN
                    null -> Color.BLUE
                }
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == "self" && context != null) {
            val id = intent.getIntExtra("id", -1)

            updateWidget(context, AppWidgetManager.getInstance(context), id)
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

    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
    }

    class Data : WidgetDataHolder.Data() {
        var state = false
    }
}

/*
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
*/
