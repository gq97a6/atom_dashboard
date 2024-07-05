package com.alteratom.dashboard.compose_global

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.helper_objects.G

fun composeConstruct(
    context: Context,
    isDark: Boolean = Theme.isDark,
    content: @Composable () -> Unit
): ComposeView {
    G.theme.apply(context = context)
    return ComposeView(context).apply { setContent { ComposeTheme(isDark, content) } }
}


