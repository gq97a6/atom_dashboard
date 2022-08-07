@file:Suppress("UNUSED")

package com.alteratom.dashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Handler
import android.os.Looper
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
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alteratom.R
import com.alteratom.dashboard.activities.MainActivity
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.security.cert.Certificate
import java.util.*

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

fun Int.roundCloser(step: Int): Int {
    return this / step * step
}

fun createNotification(
    context: Context,
    title: String = "Title",
    text: String = "Text",
    isSilent: Boolean = false,
    id: Int = Random().nextInt()
) {
    createNotificationChannel(context)

    val notification = NotificationCompat.Builder(context, "notification_id")
        .setAutoCancel(true)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.mipmap.ic_icon_bold_round)
        .setPriority(NotificationCompat.PRIORITY_MIN)
        .setVisibility(NotificationCompat.VISIBILITY_SECRET)

    if (isSilent) notification.setSilent(true)

    with(NotificationManagerCompat.from(context)) {
        notify(id, notification.build())
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


fun String.digitsOnly(): String = Regex("[^0-9]").replace(this, "")

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

inline fun Fragment.biometricAuthentication(
    title: String = "Biometric authentication",
    subtitle: String = "",
    description: String = "",
    crossinline onFail: () -> Unit = {},
    crossinline onError: (Int, CharSequence) -> Unit = { _, _ -> },
    crossinline onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit = {}
) {
    val executor = ContextCompat.getMainExecutor(this.requireContext())
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setDescription(description)
        .setSubtitle(subtitle)
        .setConfirmationRequired(false)
        .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        .build()

    val biometricPrompt = BiometricPrompt(this, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFail()
            }
        })

    biometricPrompt.authenticate(promptInfo)
}

inline fun Fragment.dashboardAuthentication(crossinline onSuccess: () -> Unit) {
    lifecycleScope.launch {
        if (ProVersion.status &&
            (G.dashboard.securityLevel == 2 ||
                    (G.dashboard.securityLevel == 1 && !G.unlockedDashboards.contains(G.dashboard.id)))
        ) {
            this@dashboardAuthentication.biometricAuthentication(onError = { _, _ -> MainActivity.fm.popBackStack() }) {
                if (G.dashboard.securityLevel == 1) G.unlockedDashboards.add(G.dashboard.id)
                onSuccess()
            }
        } else onSuccess()
    }
}