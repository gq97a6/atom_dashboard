@file:Suppress("UNUSED")

package com.alteratom.dashboard

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color.Companion.hsv
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.alteratom.R
import com.alteratom.dashboard.activity.PayActivity
import com.alteratom.dashboard.manager.BillingManager
import com.alteratom.dashboard.objects.DialogBuilder.buildConfirm
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.Pro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.security.cert.Certificate
import java.util.Base64
import java.util.Random
import kotlin.math.roundToInt


val screenHeight
    get() = Resources.getSystem().displayMetrics.heightPixels
val screenWidth
    get() = Resources.getSystem().displayMetrics.widthPixels
val screenVertical
    get() = screenHeight / screenWidth > 1

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
fun Float.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

@Composable
fun Dp.toPx() = LocalDensity.current.run { this@toPx.toPx() }

@Composable
fun Float.toDp() = LocalDensity.current.run { this@toDp.toDp() }

fun getRandomColor(alpha: Int = 255, R: Int = 255, G: Int = 255, B: Int = 255): Int {
    val r = Random()
    return Color.argb(alpha, r.nextInt(R + 1), r.nextInt(G + 1), r.nextInt(B + 1))
}

infix fun Int.alpha(@IntRange(from = 0, to = 255) a: Int): Int =
    Color.argb(a, this.red, this.green, this.blue)

fun Int.isDark(): Boolean {

    val whiteContrast = ColorUtils.calculateContrast(this, Color.WHITE)
    val blackContrast = ColorUtils.calculateContrast(this, Color.BLACK)

    return whiteContrast > blackContrast
}

fun contrastColor(isDark: Boolean, @IntRange(from = 0, to = 255) alpha: Int = 255): Int =
    (if (isDark) Color.WHITE else Color.BLACK).alpha(alpha)

fun Int.contrastColor(@IntRange(from = 0, to = 255) alpha: Int = 255): Int =
    (if (this.isDark()) Color.WHITE else Color.BLACK).alpha(alpha)

fun Int.contrast(
    @FloatRange(from = 0.0, to = 1.0) ratio: Float,
    @IntRange(from = 0, to = 255) alpha: Int = 255
): Int = this.contrast(this.isDark(), ratio, alpha)

fun Int.contrast(
    isDark: Boolean,
    @FloatRange(from = 0.0, to = 1.0) ratio: Float,
    @IntRange(from = 0, to = 255) alpha: Int = 255
): Int = ColorUtils.blendARGB(this, contrastColor(isDark), ratio).alpha(alpha)

infix fun Int.darkened(@FloatRange(from = 0.0, to = 1.0) by: Float): Int =
    ColorUtils.blendARGB(this, Color.BLACK, by)

infix fun Int.lightened(@FloatRange(from = 0.0, to = 1.0) by: Float): Int =
    ColorUtils.blendARGB(this, Color.WHITE, by)

fun Float.dezero(): String {
    return when (this - this.toInt()) {
        0f -> this.toInt()
        else -> this
    }.toString()
}

fun Float.roundCloser(step: Int): Int {
    return (this / step).roundToInt() * step
}

fun performClick(context: Context) {
    createVibration(
        context,
        vibe = VibrationEffect.createWaveform(longArrayOf(5, 3, 5), intArrayOf(200, 0, 100), -1)
    )
}

@Suppress("DEPRECATION")
fun createVibration(
    context: Context,
    ms: Long = 500,
    am: Int = VibrationEffect.DEFAULT_AMPLITUDE,
    vibe: VibrationEffect? = null,
) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if (vibrator.hasVibrator()) {
        vibrator.vibrate(vibe ?: VibrationEffect.createOneShot(ms, am))
    }
}

fun createToast(context: Context, msg: String, time: Int = 0) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        Toast.makeText(context, msg, time).show()
    } else {
        Handler(Looper.getMainLooper()).post { Toast.makeText(context, msg, time).show() }
    }
}

fun View.blink(
    times: Int = Animation.INFINITE,
    duration: Long = 50,
    offset: Long = 20,
    minAlpha: Float = 0.0f,
    maxAlpha: Float = 1.0f,
    repeatMode: Int = Animation.REVERSE
) {
    startAnimation(AlphaAnimation(maxAlpha, minAlpha).also {
        it.duration = duration
        it.startOffset = offset
        it.repeatMode = repeatMode
        it.repeatCount = times
    })
}

fun View.jiggle() = this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.jiggle))
fun View.attentate() =
    this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.attentate))

fun View.click() {
    this.performClick()
    this.isPressed = true
    this.invalidate()
    this.isPressed = false
    this.invalidate()
}


fun String.digitsOnly(): String = Regex("\\D").replace(this, "")

infix fun Int.rangedIn(r: kotlin.ranges.IntRange): Int =
    minOf(r.first, maxOf(r.last, this))

infix fun Double.rangedIn(r: ClosedFloatingPointRange<Double>): Double =
    minOf(r.start, maxOf(r.endInclusive, this))

infix fun Float.rangedIn(r: ClosedFloatingPointRange<Float>): Float =
    minOf(r.start, maxOf(r.endInclusive, this))

infix fun Long.rangedIn(r: LongRange): Long = minOf(r.first, maxOf(r.last, this))

fun ViewGroup.iterate(setOnClick: (View) -> Unit) {
    for (i in 0 until this.childCount) {
        val v = this.getChildAt(i)

        if (v is ViewGroup) v.iterate(setOnClick)
        setOnClick(v)
    }

    setOnClick(this)
}

fun Float.round(d: Int): Float = this.toBigDecimal().setScale(d, RoundingMode.FLOOR).toFloat()

fun Certificate.toPem(): String {
    val BEGIN_CERT = "-----BEGIN CERTIFICATE-----"
    val END_CERT = "-----END CERTIFICATE-----"
    val LINE_SEPARATOR = System.getProperty("line.separator") ?: ""

    val encoder: Base64.Encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.toByteArray())
    val rawCrtText: ByteArray = this.encoded
    val encodedCertText = String(encoder.encode(rawCrtText))
    return BEGIN_CERT + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + END_CERT
}

inline infix fun <reified T> (() -> T).ifShitHitsTheFan(catch: () -> T): T {
    return try {
        this()
    } catch (e: Exception) {
        catch()
    }
}

fun pHsv(h: Float, s: Float, v: Float) = hsv(
    if (h.isNaN()) 180f else h,
    if (s.isNaN()) .5f else s,
    if (v.isNaN()) .5f else v
)

fun Context.proAlert(activity: Activity) {
    with(this) {
        buildConfirm(
            message = "This feature requires pro",
            label = "UNLOCK"
        ) {
            activity.apply {
                Intent(
                    this,
                    PayActivity::class.java
                ).also {
                    it.action = Intent.ACTION_VIEW
                    startActivity(it)
                }
            }
        }
    }
}

fun Context.isBatteryOptimized(): Boolean =
    !(this.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager)
        .isIgnoringBatteryOptimizations(this.applicationContext.packageName)

fun Context.openBatterySettings() {
    val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    this.startActivity(intent)
}

fun Activity.areNotificationsAllowed() =
    with(this) { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }.let { manager ->
        when {
            !manager.areNotificationsEnabled() -> false
            SDK_INT >= TIRAMISU -> manager.notificationChannels.none {
                it.importance == NotificationManager.IMPORTANCE_NONE
            }

            else -> true
        }
    }

fun Activity.requestNotifications() {
    this.apply {
        if (SDK_INT >= TIRAMISU) requestPermissions(arrayOf(POST_NOTIFICATIONS), 1)
    }
}

internal fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        "notification_id",
        "Other notification",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "com/alteratom/notification_channel"
    }

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

fun createNotification(
    context: Context,
    title: String = "Title",
    text: String = "Text",
    isSilent: Boolean = false,
    id: Int = Random().nextInt()
) {
    if (ActivityCompat.checkSelfPermission(
            context,
            POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) return

    createNotificationChannel(context)

    val notification = NotificationCompat.Builder(context, "notification_id")
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.drawable.ic_icon)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setVisibility(NotificationCompat.VISIBILITY_SECRET)

    if (isSilent) notification.setSilent(true)

    with(NotificationManagerCompat.from(context)) {
        notify(id, notification.build())
    }
}

fun generateNewId() = kotlin.math.abs(Random().nextLong())

fun Context.checkBilling() {
    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
        BillingManager(this@checkBilling).apply {
            enable()
            checkPurchases(
                0,
                {
                    !it.isAcknowledged || (!G.isLicensed && it.products.contains(
                        BillingManager.PRO
                    ))
                }
            )
            disable()
        }

        if (!G.isLicensed) {
            for (dash in G.dashboards.slice(2 until G.dashboards.size)) {
                dash.mqtt.isEnabled = false
                dash.daemon?.notifyConfigChanged()
            }
        }
    }
}