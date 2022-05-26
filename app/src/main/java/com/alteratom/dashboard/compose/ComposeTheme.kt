package com.alteratom.dashboard.compose

import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.isDark
import androidx.compose.ui.graphics.Color as C

val DarkColorPalette: Colors
    get() = darkColors(
        primary = C.LightGray,
        primaryVariant = C.Gray,
        secondary = C.DarkGray,
        secondaryVariant = C.Black,
        background = C.Blue,
        surface = C.Transparent,
        onPrimary = C.White,
        onSecondary = C.White,
        onBackground = C.White,
        onSurface = C.White
    )

val LightColorPalette: Colors
    get() = lightColors(
        primary = C.Black,
        primaryVariant = C.DarkGray,
        secondary = C.Gray,
        secondaryVariant = C.LightGray,
        background = C.Red,
        surface = C.Transparent,
        onPrimary = C.Black,
        onSecondary = C.Black,
        onBackground = C.Black,
        onSurface = C.Black
    )

@Composable
fun ComposeTheme(
    darkTheme: Boolean = isDark,
    colors: Theme.ComposeColorPallet? = null,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorPalette else LightColorPalette,
        shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(4.dp),
            large = RoundedCornerShape(0.dp)
        ),
        content = content
    )
}