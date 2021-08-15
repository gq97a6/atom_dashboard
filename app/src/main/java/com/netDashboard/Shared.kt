package com.netDashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.*
import android.widget.Toast
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import java.util.*

const val A = 255 //100%
const val B = 150 //60%
const val C = 75 //30%
const val D = 25 //10%

val screenHeight = Resources.getSystem().displayMetrics.heightPixels
val screenWidth = Resources.getSystem().displayMetrics.widthPixels

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Float.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Float.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun getRandomColor(alpha: Int = 255, R: Int = 255, G: Int = 255, B: Int = 255): Int {
    val r = Random()
    return Color.argb(alpha, r.nextInt(R + 1), r.nextInt(G + 1), r.nextInt(B + 1))
}

fun Int.contrast(@IntRange(from = 0, to = 255) alpha: Int = 255): Int =
    if (ColorUtils.calculateLuminance(this) < 0.5) {
        -1 //White
    } else {
        -16777216 //Black
    }.alpha(alpha)

infix fun Int.alpha(@IntRange(from = 0, to = 255) a: Int): Int =
    Color.argb(a, this.red, this.green, this.blue)

fun Int.isDark(): Boolean = ColorUtils.calculateLuminance(this) > 0.5

infix fun Int.darkened(by: Float): Int = ColorUtils.blendARGB(this, Color.BLACK, by)
infix fun Int.lightened(by: Float): Int = ColorUtils.blendARGB(this, Color.WHITE, by)

fun Float.dezero(): String {
    return when (this - this.toInt()) {
        0f -> this.toInt()
        else -> this
    }.toString()
}

fun Float.roundCloser(step: Float): Float {
    return (this / step).toInt() / (1 / step)
}

fun createNotification(
    context: Context,
    title: String = "Title",
    text: String = "Text",
    isSilent: Boolean = false,
    id: Int = Random().nextInt()
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(context)

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

fun createToast(context: Context, msg: String, time: Int = 0) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Toast.makeText(context, msg, time).show()
    } else {
        Handler(Looper.getMainLooper()).post { Toast.makeText(context, msg, time).show() }
    }
}