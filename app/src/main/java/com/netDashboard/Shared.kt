package com.netDashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.*
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

fun Float.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Float.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Snackbar.margins(): Snackbar {
    14f.toPx()
    val params = this.view.layoutParams as android.widget.FrameLayout.LayoutParams

    params.setMargins(60.toPx(), 60.toPx(), 60.toPx(), 60.toPx())

    this.view.layoutParams = params

    return this
}

fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
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

fun Int.alpha(@FloatRange(from = 0.0, to = 1.0) a: Float): Int {
    val alpha = (255 * a).toInt()
    return Color.argb(alpha, this.red, this.green, this.blue)
}

fun createNotification(
    context: Context,
    title: String = "Title",
    text: String = "Text",
    isSilent: Boolean = false,
    id: Int = Random().nextInt()
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel(context)
    }

    val notification = NotificationCompat.Builder(context, "notification_id")
        .setAutoCancel(true)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.icon_main)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setVisibility(NotificationCompat.VISIBILITY_SECRET)

    if (isSilent) notification.setSilent(true)

    with(NotificationManagerCompat.from(context)) {
        notify(id, notification.build())
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        "notification_id",
        "Other notification",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "com/netDashboard/notification_channel"
    }

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

@Suppress("DEPRECATION")
fun createVibration(context: Context, ms: Long = 500) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    ms,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(ms)
        }
    }
}

fun Float.dezero(): String {
    return when (this - this.toInt()) {
        0f -> this.toInt()
        else -> this
    }.toString()
}

fun Float.roundCloser(step: Float): Float {
    return (this / step).toInt() / (1 / step)
}