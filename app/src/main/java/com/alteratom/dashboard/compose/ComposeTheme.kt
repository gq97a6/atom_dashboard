package com.alteratom.dashboard.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.Theme.Companion.isDark
import com.alteratom.testjetpackcompose.ui.theme.ComposeShapes
import com.alteratom.testjetpackcompose.ui.theme.ComposeTypography
import androidx.compose.ui.graphics.Color as C

val DarkColorPalette = darkColors(
    primary = C.LightGray,
    primaryVariant = C.Gray,
    secondary = C.DarkGray,
    secondaryVariant = C.Black,
    background = colors.background,
    surface = C.Transparent,
    onPrimary = C.White,
    onSecondary = C.White,
    onBackground = C.White,
    onSurface = C.White
)

val LightColorPalette = lightColors(
    primary = C.Black,
    primaryVariant = C.DarkGray,
    secondary = C.Gray,
    secondaryVariant = C.LightGray,
    background = colors.background,
    surface = C.Transparent,
    onPrimary = C.Black,
    onSecondary = C.Black,
    onBackground = C.Black,
    onSurface = C.Black
)

@Composable
fun ComposeTheme(
    darkTheme: Boolean = isDark,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        typography = ComposeTypography,
        shapes = ComposeShapes,
        content = content
    )
}