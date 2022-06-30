package com.alteratom.dashboard.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.alteratom.R
import com.alteratom.dashboard.FolderTree.parseSave
import com.alteratom.dashboard.FolderTree.rootFolder
import com.alteratom.dashboard.FolderTree.saveToFile
import com.alteratom.dashboard.G.widgetDataHolder
import com.alteratom.dashboard.createVibration
import com.alteratom.dashboard.performClick
import kotlinx.coroutines.runBlocking

class SwitchWidgetProvider : AppWidgetProvider() {

    companion object : WidgetProviderCompanion() {
        override val type = "switch"

        override fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_switch)

            views.setOnClickPendingIntent(
                R.id.widget_root,
                getPendingSelfIntent(context, SwitchWidgetProvider::class.java, appWidgetId)
            )

            val status = try {
                widgetDataHolder.switch[appWidgetId]?.state
            } catch (e: Exception) {
                null
            }

            //val drawable = this.background as? GradientDrawable
            //drawable?.setStroke(10, p.color)
            //drawable?.cornerRadius = 25f

            views.setInt(
                R.id.widget_root,
                "setBackgroundColor",
                when (status) {
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

            try {
                performClick(context)
                widgetDataHolder.switch[id]!!.state = !widgetDataHolder.switch[id]!!.state
            } catch (e: Exception) {
            }

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

        if (newWidgetIds == null || oldWidgetIds == null || context == null) return
        rootFolder = context.filesDir.canonicalPath.toString()

        val holder = try {
            widgetDataHolder
        } catch (e: Exception) {
            parseSave<WidgetDataHolder>() ?: return
        }

        for (i in oldWidgetIds.indices) {
            holder.switch.mapKeys { newWidgetIds[oldWidgetIds.indexOf(it.key)] }
        }

        holder.saveToFile()
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)

        if (appWidgetIds == null || context == null) return
        rootFolder = context.filesDir.canonicalPath.toString()

        val holder = try {
            widgetDataHolder
        } catch (e: Exception) {
            parseSave<WidgetDataHolder>() ?: return
        }

        for (id in appWidgetIds) {
            holder.switch.remove(id)
        }

        holder.saveToFile()
    }

    class Data {
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