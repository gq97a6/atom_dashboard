package com.alteratom.dashboard.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.alteratom.R
import com.google.android.vending.licensing.AESObfuscator
import com.google.android.vending.licensing.LicenseChecker
import com.google.android.vending.licensing.LicenseCheckerCallback
import com.google.android.vending.licensing.ServerManagedPolicy

/**
 * Welcome to the world of Google Play licensing. We're so glad to have you
 * on board!
 *
 *
 * The first thing you need to do is get your hands on your public key.
 * Update the BASE64_PUBLIC_KEY constant below with the encoded public key
 * for your application, which you can find under Services and APIs/Licensing
 * & In-App Billing on the Google Play publisher site.
 *
 *
 * After you get this sample running, peruse the
 * [
 * licensing documentation.](http://developer.android.com/google/play/licensing/index.html)
 */
class LVLActivity : Activity() {
    private var mStatusText: TextView? = null
    private var mCheckLicenseButton: Button? = null
    private var mLicenseCheckerCallback: LicenseCheckerCallback? = null
    private var mChecker: LicenseChecker? = null

    // A handler on the UI thread.
    private var mHandler: Handler? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.main)
        mStatusText = findViewById<View>(R.id.status_text) as TextView
        mCheckLicenseButton = findViewById<View>(R.id.check_license_button) as Button
        mCheckLicenseButton!!.setOnClickListener { doCheck() }
        mHandler = Handler()

        // Try to use more data here. ANDROID_ID is a single point of attack.
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Library calls this when it's done.
        mLicenseCheckerCallback = MyLicenseCheckerCallback()
        // Construct the LicenseChecker with a policy.
        mChecker = LicenseChecker(
            this, ServerManagedPolicy(
                this,
                AESObfuscator(
                    SALT,
                    packageName, deviceId
                )
            ),
            BASE64_PUBLIC_KEY
        )
        doCheck()
    }

    override fun onCreateDialog(id: Int): Dialog {
        val bRetry = id == 1
        return AlertDialog.Builder(this)
            .setTitle(R.string.unlicensed_dialog_title)
            .setMessage(if (bRetry) R.string.unlicensed_dialog_retry_body else R.string.unlicensed_dialog_body)
            .setPositiveButton(if (bRetry) R.string.retry_button else R.string.restore_access_button,
                object : DialogInterface.OnClickListener {
                    var mRetry = bRetry
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        if (mRetry) {
                            doCheck()
                        } else {
                            mChecker?.followLastLicensingUrl(this@LVLActivity)
                        }
                    }
                })
            .setNegativeButton(R.string.quit_button) { _, _ -> finish() }.create()
    }

    private fun doCheck() {
        mCheckLicenseButton!!.isEnabled = false
        setProgressBarIndeterminateVisibility(true)
        mStatusText?.setText(R.string.checking_license)
        mChecker?.checkAccess(mLicenseCheckerCallback)
    }

    private fun displayResult(result: String) {
        mHandler!!.post {
            mStatusText!!.text = result
            setProgressBarIndeterminateVisibility(false)
            mCheckLicenseButton!!.isEnabled = true
        }
    }

    private fun displayDialog(showRetry: Boolean) {
        mHandler!!.post {
            setProgressBarIndeterminateVisibility(false)
            showDialog(if (showRetry) 1 else 0)
            mCheckLicenseButton!!.isEnabled = true
        }
    }

    private inner class MyLicenseCheckerCallback : LicenseCheckerCallback {
        override fun allow(policyReason: Int) {
            if (isFinishing) {
                // Don't update UI if Activity is finishing.
                return
            }
            // Should allow user access.
            displayResult(getString(R.string.allow))
        }

        override fun dontAllow(policyReason: Int) {
            if (isFinishing) {
                // Don't update UI if Activity is finishing.
                return
            }
            displayResult(getString(R.string.dont_allow))
            // Should not allow access. In most cases, the app should assume
            // the user has access unless it encounters this. If it does,
            // the app should inform the user of their unlicensed ways
            // and then either shut down the app or limit the user to a
            // restricted set of features.
            // In this example, we show a dialog that takes the user to a deep
            // link returned by the license checker.
            // If the reason for the lack of license is that the service is
            // unavailable or there is another problem, we display a
            // retry button on the dialog and a different message.
            displayDialog(policyReason == com.google.android.vending.licensing.Policy.RETRY)
        }

        override fun applicationError(errorCode: Int) {
            if (isFinishing) {
                // Don't update UI if Activity is finishing.
                return
            }
            // This is a polite way of saying the developer made a mistake
            // while setting up or calling the license checker library.
            // Please examine the error code and fix the error.
            val result = String.format(getString(R.string.application_error), errorCode)
            displayResult(result)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mChecker?.onDestroy()
    }

    companion object {
        private const val BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtSUgYbym5NBGygfks7CC6+YNXROLVBjJgNUrg1GOKmKF5dUEo0m+2c7dE6o/y9j+a4WbV3LO/QI9mKIn1qF8AKrCCSjWoHPWA5ng2T44FmR9Wn/pGTEUhs10iQhHX5E3QwZrg/5h2yEOk4CjUsNxrs3qy8W+1FO2jqnZ+g+qxSe4qmPnANdDeXgMu3CFFFAK9MhibMLqfhGUtfOCIAZ7z/ZxQsN4iwSaOJNLQdhqawxYptb/b7g5/YklXz37behFyXED+FOLijM5nZQeICmACY5EfxslJTB1N+van1HXxkpLlq0ZWaB+eP51vV6vu59pvFqcPnlZ7CRkRWyMhzQ41wIDAQAB"

        // Generate your own 20 random bytes, and put them here.
        private val SALT = byteArrayOf(
            -46,
            65,
            30,
            -128,
            -103,
            -57,
            74,
            -64,
            51,
            88,
            -95,
            -45,
            77,
            -117,
            -36,
            -113,
            -11,
            32,
            -64,
            89
        )
    }
}