package com.alteratom.dashboard.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

abstract class WidgetProviderCompanion : AppWidgetProvider() {
    abstract val PREFS_NAME: String
    val PREF_PREFIX_KEY = "widget_"

    abstract fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    )

    open fun save(context: Context, appWidgetId: Int, text: String) {}
    open fun load(context: Context, appWidgetId: Int) {}
    open fun delete(context: Context, appWidgetId: Int)  {
        context.getSharedPreferences(PREFS_NAME, 0).edit()
            .remove(PREF_PREFIX_KEY + appWidgetId).apply()
    }

    fun getPendingSelfIntent(context: Context?, action: String?, provider: Class<*>?): PendingIntent? {
        val intent = Intent(context, provider)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}