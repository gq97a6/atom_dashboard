package com.netDashboard

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.graphics.*
import com.google.android.material.snackbar.Snackbar
import java.util.*


fun createToast(context: Context, msg: String, time: Int = 0) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Toast.makeText(context, msg, time).show()
    } else {
        Handler(Looper.getMainLooper()).post { Toast.makeText(context, msg, time).show() }
    }
}

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Snackbar.margins(): Snackbar {
    val params = this.view.layoutParams as android.widget.FrameLayout.LayoutParams

    params.setMargins(30.toPx(), 30.toPx(), 30.toPx(), 30.toPx())

    this.view.layoutParams = params

    return this
}

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getRandomColor(alpha: Int = 255, R: Int = 255, G: Int = 255, B: Int = 255): Int {
    val r = Random()
    return Color.argb(alpha, r.nextInt(R + 1), r.nextInt(G + 1), r.nextInt(B + 1))
}

fun getContrastColor(color: Int, negative: Boolean = false): Int {

    return if ((ColorUtils.calculateLuminance(color) < 0.5) xor negative) {
        (-1) //White
    } else {
        (-16777216) //Black
    }
}

fun Int.alpha(a: Int): Int {
    val alpha = 255 * a / 100
    return Color.argb(alpha, this.red, this.green, this.blue)
}

fun Int.darken(d: Int): Int {

    val r = this.red * (100 - d) / 100
    val g = this.green * (100 - d) / 100
    val b = this.blue * (100 - d) / 100

    return Color.argb(this.alpha, r, g, b)
}

fun Int.lighten(l: Int): Int { //TODO

    val r = if(this.red + (this.red * l / 100) < 255) this.red + (this.red * l / 100) else 255
    val g = if(this.green + (this.green * l / 100) < 255) this.green + (this.green * l / 100) else 255
    val b = if(this.blue + (this.blue * l / 100) < 255) this.blue + (this.blue * l / 100) else 255

    return Color.argb(this.alpha, r, g, b)
}