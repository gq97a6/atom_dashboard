package com.alteratom.dashboard.activities

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.BasicButton
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.G
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.widgets.updateAppWidget

class WidgetConfigureActivity : AppCompatActivity() {
    private var id = INVALID_APPWIDGET_ID

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setResult(RESULT_CANCELED)

        intent.extras?.let {
            id = it.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        }

        if (id == INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        G.theme.apply(context = this)

        setContent {
            ComposeTheme(Theme.isDark) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                        .padding(20.dp)
                ) {
                    val context = this@WidgetConfigureActivity
                    var title by remember {
                        mutableStateOf(
                            loadTitlePref(
                                this@WidgetConfigureActivity,
                                id
                            )
                        )
                    }
                    EditText(
                        label = { Text("Widget title") },
                        value = title,
                        onValueChange = {
                            title = it
                        }
                    )

                    BasicButton(
                        modifier = Modifier.padding(top = 10.dp),
                        onClick = {
                            saveTitlePref(context, id, title)
                            updateAppWidget(context, AppWidgetManager.getInstance(context), id)

                            setResult(RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, id))
                            finish()
                        }
                    ) {
                        Text("ADD WIDGET", fontSize = 10.sp, color = colors.a)
                    }
                }
            }
        }
    }

}

private const val PREFS_NAME = "com.alteratom.dashboard.Widget"
private const val PREF_PREFIX_KEY = "widget_"

internal fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
    context.getSharedPreferences(PREFS_NAME, 0).edit()
        .putString(PREF_PREFIX_KEY + appWidgetId, text).apply()
}

internal fun deleteTitlePref(context: Context, appWidgetId: Int) {
    context.getSharedPreferences(PREFS_NAME, 0).edit()
        .remove(PREF_PREFIX_KEY + appWidgetId).apply()
}

internal fun loadTitlePref(context: Context, appWidgetId: Int) =
    context.getSharedPreferences(PREFS_NAME, 0)
        .getString(PREF_PREFIX_KEY + appWidgetId, null) ?: "error_title"
