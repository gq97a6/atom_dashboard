package com.alteratom.dashboard.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle

abstract class WidgetProviderCompanion : AppWidgetProvider() {
    abstract val type: String

    abstract fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    )

    fun getPendingSelfIntent(
        context: Context?,
        provider: Class<*>?,
        id: Int,
        extras: Bundle = Bundle()
    ): PendingIntent? = PendingIntent.getBroadcast(
        context,
        id,
        Intent(context, provider).apply {
            action = "self"
            putExtras(extras.apply { putInt("id", id) })
        },
        PendingIntent.FLAG_MUTABLE
    )
}