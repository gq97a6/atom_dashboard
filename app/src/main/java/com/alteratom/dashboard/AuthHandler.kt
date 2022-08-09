package com.alteratom.dashboard
//
//import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
//import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
//import androidx.biometric.BiometricPrompt
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.DialogFragment
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentManager
//import androidx.lifecycle.lifecycleScope
//import com.alteratom.dashboard.activities.MainActivity.Companion.fm
//import kotlinx.coroutines.launch
//
//object AuthHandler {
//
//    inline fun Fragment.biometricAuthentication(
//        title: String = "Biometric authentication",
//        subtitle: String = "",
//        description: String = "",
//        crossinline onFail: () -> Unit = {},
//        crossinline onError: (Int, CharSequence) -> Unit = { _, _ -> },
//        crossinline onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit = {}
//    ) {
//        val executor = ContextCompat.getMainExecutor(this.requireContext())
//        val promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle(title)
//            .setDescription(description)
//            .setSubtitle(subtitle)
//            .setConfirmationRequired(false)
//            .setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
//            .build()
//
//        val biometricPrompt = BiometricPrompt(this, executor,
//            object : BiometricPrompt.AuthenticationCallback() {
//                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                    super.onAuthenticationError(errorCode, errString)
//                    onError(errorCode, errString)
//                }
//
//                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                    super.onAuthenticationSucceeded(result)
//                    onSuccess(result)
//                }
//
//                override fun onAuthenticationFailed() {
//                    super.onAuthenticationFailed()
//                    onFail()
//                }
//            })
//
//        biometricPrompt.authenticate(promptInfo)
//    }
//
//    inline fun Fragment.dashboardAuthentication(crossinline onSuccess: () -> Unit) {
//        lifecycleScope.launch {
//            if (ProVersion.status &&
//                (G.dashboard.securityLevel == 2)
//            ) {
//                this@dashboardAuthentication.biometricAuthentication(onError = { _, _ -> fm.popBackStack() }) {
//                    if (G.dashboard.securityLevel == 1) G.unlockedDashboards.add(G.dashboard.id)
//                    onSuccess()
//                }
//            } else onSuccess()
//        }
//    }
//}
