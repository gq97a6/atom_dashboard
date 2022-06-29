package com.alteratom.dashboard.activities.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.BasicButton
import com.alteratom.dashboard.FolderTree.parseListSave
import com.alteratom.dashboard.FolderTree.saveToFile
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.widgetDataHolder
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.widgets.WidgetDataHolder.Data
import com.alteratom.tile.types.button.ButtonTile
import com.alteratom.dashboard.widgets.ButtonWidgetProvider as Provider

class ButtonWidgetConActivity : AppCompatActivity() {
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
                    val context = this@ButtonWidgetConActivity

                    BasicButton(
                        modifier = Modifier.padding(top = 10.dp),
                        onClick = {
                            try {
                                widgetDataHolder.get()
                                widgetDataHolder.add(Provider.Data())
                                widgetDataHolder.saveToFile()
                            } catch (e: Exception) {
                                try {
                                    parseListSave<Data>().let {
                                        it.add(Provider.Data())
                                        saveToFile()
                                    }
                                } catch (e: Exception) {
                                    run {}
                                }
                            }


                            Provider.updateWidget(
                                context,
                                AppWidgetManager.getInstance(context),
                                id
                            )

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

class a() {
    val test = mutableListOf(ButtonTile())
}