package com.alteratom.dashboard.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alteratom.dashboard.ActivityHandler
import com.alteratom.dashboard.BasicButton
import com.alteratom.dashboard.G
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeTheme
import com.google.android.vending.licensing.AESObfuscator
import com.google.android.vending.licensing.LicenseChecker
import com.google.android.vending.licensing.LicenseCheckerCallback
import com.google.android.vending.licensing.ServerManagedPolicy

class LVLActivity : AppCompatActivity() {

    private lateinit var checker: LicenseChecker

    companion object {
        private const val BASE64_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtSUgYbym5NBGygfks7CC6+YNXROLVBjJgNUrg1GOKmKF5dUEo0m+2c7dE6o/y9j+a4WbV3LO/QI9mKIn1qF8AKrCCSjWoHPWA5ng2T44FmR9Wn/pGTEUhs10iQhHX5E3QwZrg/5h2yEOk4CjUsNxrs3qy8W+1FO2jqnZ+g+qxSe4qmPnANdDeXgMu3CFFFAK9MhibMLqfhGUtfOCIAZ7z/ZxQsN4iwSaOJNLQdhqawxYptb/b7g5/YklXz37behFyXED+FOLijM5nZQeICmACY5EfxslJTB1N+van1HXxkpLlq0ZWaB+eP51vV6vu59pvFqcPnlZ7CRkRWyMhzQ41wIDAQAB"

        // Generate your own 20 random bytes, and put them here
        private val SALT = byteArrayOf(
            -46, 65, 30, -128,
            -103, -57, 74, -64,
            51, 88, -95, -45,
            77, -117, -36, -113,
            -11, 32, -64, 89
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHandler.onCreate(this)

        G.theme.apply(context = this)

        //Construct the LicenseChecker with a policy
        //Try to use more data here. ANDROID_ID is a single point of attack
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        checker = LicenseChecker(
            this,
            ServerManagedPolicy(this, AESObfuscator(SALT, packageName, deviceId)),
            BASE64_PUBLIC_KEY
        )

        setContent {
            var result by remember { mutableStateOf("NULL") }
            var isChecking by remember { mutableStateOf(false) }

            fun displayResult(r: String) {
                result = r
                isChecking = false
            }

            val licenseCheckerCallback = object : LicenseCheckerCallback {
                override fun allow(policyReason: Int) {
                    displayResult("Allow the user access")
                }

                override fun dontAllow(policyReason: Int) {
                    displayResult("Do not allow the user access")
                }

                override fun applicationError(errorCode: Int) {
                    displayResult("Application error: $errorCode")
                }
            }

            ComposeTheme(Theme.isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            result,
                            Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )

                        BasicButton(onClick = {
                            isChecking = true
                            result = "Checking license..."
                            checker.checkAccess(licenseCheckerCallback)
                        }, Modifier.padding(10.dp), enabled = !isChecking) {
                            Text("CHECK LICENSE", textAlign = TextAlign.Center, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        checker.onDestroy()
    }
}