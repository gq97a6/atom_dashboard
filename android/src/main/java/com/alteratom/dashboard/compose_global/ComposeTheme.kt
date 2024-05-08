package com.alteratom.dashboard.compose_global

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import androidx.compose.ui.graphics.Color as C

val DarkColorPalette: Colors
    get() = darkColors(
        primary = C.White,
        primaryVariant = C.White,
        secondary = C.White,
        secondaryVariant = C.White,
        background = colors.background,
        surface = C.Transparent,
        onPrimary = C.White,
        onSecondary = C.White,
        onBackground = C.White,
        onSurface = C.White
    )

val LightColorPalette: Colors
    get() = lightColors(
        primary = C.Black,
        primaryVariant = C.Black,
        secondary = C.Black,
        secondaryVariant = C.Black,
        background = colors.background,
        surface = C.Transparent,
        onPrimary = C.Black,
        onSecondary = C.Black,
        onBackground = C.Black,
        onSurface = C.Black
    )

//val Typo0
//    @Composable
//    get() = MaterialTheme.typography.copy(
//        h1 = MaterialTheme.typography.h1.copy(platformStyle = PlatformTextStyle(includeFontPadding = false))
//    )
//
//
//val Typo = Typography(
//    body1 = DefaultTextStyle.copy(
//        platformStyle = PlatformTextStyle(includeFontPadding = false)
//    )
//)

@Composable
fun ComposeTheme(
    isDark: Boolean = Theme.isDark,
    content: @Composable () -> Unit
) = MaterialTheme(
    colors = if (isDark) DarkColorPalette else LightColorPalette,
    //typography = Typo,
    shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(4.dp)
    )
) {
    CompositionLocalProvider(
        LocalRippleTheme provides object : RippleTheme {
            @Composable
            override fun defaultColor(): C = colors.background

            @Composable
            override fun rippleAlpha(): RippleAlpha = RippleAlpha(.2f, .2f, .1f, 1f)
            //RippleTheme.defaultRippleAlpha(androidx.compose.ui.graphics.Color.Black, lightTheme = !isSystemInDarkTheme())
        },
        content = content
    )
}