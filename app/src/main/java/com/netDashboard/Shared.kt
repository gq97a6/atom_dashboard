package com.netDashboard

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
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
        -1 //White
    } else {
        -16777216 //Black
    }
}

fun Int.alpha(a: Int): Int {
    val alpha = 255 * a / 100
    return Color.argb(alpha, this.red, this.green, this.blue)
}

//For fun ----------------------------------------------------------------
fun View.move(property: String, distance: Float, duration: Long = 300) {
    ObjectAnimator.ofFloat(this, property, distance)
        .apply {
            this.duration = duration
            start()
        }
}

fun View.rotate(duration: Long = 300) {
    ObjectAnimator.ofFloat(this, View.ROTATION, 0f, 360f)
        .apply {
            this.duration = duration
            start()
        }
}

fun View.scale(duration: Long = 300, by: Float) {
    this.animate()
        .scaleY(by)
        .scaleX(by)
        .setInterpolator(AccelerateDecelerateInterpolator()).duration = duration
}