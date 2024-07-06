package com.alteratom.dashboard.compose_global

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.app.AtomApp.Companion.aps

fun composeConstruct(
    context: Context,
    isDark: Boolean = Theme.isDark,
    content: @Composable () -> Unit
): ComposeView {
    aps.theme.apply(context = context)
    return ComposeView(context).apply { setContent { ComposeTheme(isDark, content) } }
}


