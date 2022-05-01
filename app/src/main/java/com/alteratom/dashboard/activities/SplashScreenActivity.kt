package com.alteratom.dashboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.Theme.Companion.artist
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.Theme.Companion.isDark
import com.alteratom.dashboard.compose.ComposeTheme

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    //private lateinit var b: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        //super.onCreate(savedInstanceState)
        //Activity.onCreate(this)
//
        //b = ActivitySplashScreenBinding.inflate(layoutInflater)
        //setContentView(b.root)
        //G.theme.apply(b.root, this)
//
        ////if (Build.VERSION.SDK_INT > 30) b.ssBox.visibility = GONE
//
        //b.ssIcon.setBackgroundResource(if (G.theme.a.isDark) R.drawable.ic_icon_light else R.drawable.ic_icon)
//
        //rootFolder = filesDir.canonicalPath.toString()
//
        //if (service != null && dashboards.isNotEmpty()) {
        //    service?.dgManager?.assign()
        //    onServiceReady()
        //} else {
        //    G.initialize()
//
        //    val foregroundServiceHandler = ForegroundServiceHandler(this)
        //    foregroundServiceHandler.service.observe(this) { s ->
        //        if (s != null) {
        //            service?.finishAffinity = { finishAffinity() }
        //            onServiceReady()
        //        }
        //    }
//
        //    foregroundServiceHandler.start()
        //    foregroundServiceHandler.bind()
        //}

        super.onCreate(savedInstanceState)

        window.statusBarColor = artist.colors.background
        WindowInsetsControllerCompat(this.window, window.decorView)
            .isAppearanceLightStatusBars = !isDark

        setContent {
            ComposeTheme(isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Test()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Activity.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        Activity.onPause()
        finish()
    }

    private fun onServiceReady() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            overridePendingTransition(0, 0)
            finish()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Test()
    }
}

private object RippleCustomTheme : RippleTheme {

    @Composable
    override fun defaultColor() =
        RippleTheme.defaultRippleColor(
            Color(255, 255, 255),
            lightTheme = false
        )

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        RippleTheme.defaultRippleAlpha(
            Color(255, 255, 255),
            lightTheme = true
        )
}

var textPrivate = ""
    set(value) {
        field = value
    }

@Composable
fun Test() {
    var counter by remember { mutableStateOf(0) }
    var state by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("false") }

    Surface(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(text = "Tile properties", fontSize = 45.sp, color = colors.color)
            Row(
                modifier = Modifier.padding(top = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    contentPadding = PaddingValues(13.dp),
                    onClick = {},
                    border = BorderStroke(0.dp, colors.color),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(52.dp)
                        .width(52.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_icon_light), "")
                }

                EditText(
                    label = { BoldStartText("Text ", "tile tag") },
                    value = textPrivate,
                    onValueChange = {
                        textPrivate = it
                    },
                    modifier = Modifier.padding(start = 20.dp)
                )
            }

            BoldStartText(
                a = "Communication: ",
                b = "MQTT",
                modifier = Modifier.padding(start = 5.dp, bottom = 3.dp, top = 15.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(BorderStroke(0.dp, colors.color), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .padding(bottom = 6.dp)
            ) {
                LabeledSwitch(
                    label = { Text("Enabled:", fontSize = 15.sp) },
                    checked = state,
                    onCheckedChange = {})
                EditText(label = { Text("Subscribe topic") }, value = text, onValueChange = { })
                EditText(
                    label = { Text("Publish topic") },
                    value = textPrivate,
                    onValueChange = { textPrivate = it },
                    modifier = Modifier.padding(top = 10.dp),
                    trailingIcon = {
                        IconButton(onClick = {}) {
                            Icon(painterResource(R.drawable.il_file_copy), "")
                        }
                    }
                )
                RadioGroup(
                    listOf(
                        "QoS 0: At most once. No guarantee.",
                        "QoS 1: At least once. (Recommended)",
                        "QoS 2: Delivery exactly once."
                    ), "Quality of Service (MQTT protocol):",
                    1,
                    {},
                    modifier = Modifier.padding(top = 20.dp)
                )

                LabeledSwitch(
                    label = { Text("Retain massages:", fontSize = 15.sp) },
                    checked = state,
                    onCheckedChange = {},
                    modifier = Modifier.padding(top = 10.dp)
                )

                LabeledSwitch(
                    label = { Text("Confirm publishing:", fontSize = 15.sp) },
                    checked = state,
                    onCheckedChange = {},
                    modifier = Modifier.padding(top = 0.dp)
                )

                LabeledSwitch(
                    label = { Text("Payload is JSON:", fontSize = 15.sp) },
                    checked = state,
                    onCheckedChange = {},
                    modifier = Modifier.padding(top = 0.dp)
                )

                EditText(
                    label = { Text("Payload JSON Pointer", fontSize = 15.sp) },
                    value = textPrivate,
                    onValueChange = { textPrivate = it },
                    modifier = Modifier.padding(top = 5.dp)
                )
            }

            Text(
                "Notifications and log",
                color = colors.a,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 5.dp, bottom = 3.dp, top = 15.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(BorderStroke(0.dp, colors.color), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                LabeledSwitch(
                    label = { Text("Log new values:", fontSize = 15.sp) },
                    checked = state,
                    onCheckedChange = {},
                    modifier = Modifier.padding(top = 0.dp)
                )

                LabeledSwitch(
                    label = { Text("Notify on receive:", fontSize = 15.sp) },
                    checked = state,
                    onCheckedChange = {},
                    modifier = Modifier.padding(top = 0.dp)
                )

                LabeledCheckbox(
                    label = { Text("Make notification quiet", fontSize = 15.sp) },
                    checked = state,
                    onCheckedChange = {},
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }

    //Column(modifier = Modifier.padding(16.dp)) {
    //    OutlinedTextField(
    //        value = text,
    //        onValueChange = { text = it },
    //        label = { Text("Label") }
    //    )
//
    //    CompositionLocalProvider(LocalRippleTheme provides RippleCustomTheme) {
    //        OutlinedButton(
    //            onClick = {},
    //            border = BorderStroke(0.dp, Color.White),
    //            shape = RectangleShape,
    //            modifier = Modifier.padding(top = 10.dp)
    //        ) {
    //            Text("TEST", color = Color.White)
    //        }
    //    }
//
    //    CustomView()
    //}
}

@Composable
fun CustomView() {
    val selectedItem = remember { mutableStateOf(0) }
    AndroidView(
        modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.fragment_tile_new, null, false)
            view
        },
        update = { view ->
        }
    )
}