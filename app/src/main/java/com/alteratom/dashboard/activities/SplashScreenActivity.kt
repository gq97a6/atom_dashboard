package com.alteratom.dashboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.get
import com.alteratom.R
import com.alteratom.dashboard.Activity
import com.alteratom.dashboard.FolderTree.rootFolder
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service
import com.alteratom.dashboard.foreground_service.ForegroundServiceHandler
import com.alteratom.dashboard.jiggle
import com.alteratom.databinding.ActivitySplashScreenBinding
import com.alteratom.testjetpackcompose.ui.theme.TestJetpackComposeTheme

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

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
        //b.ssIcon.setBackgroundResource(if (G.theme.a.isDark) R.drawable.ic_icon_light  else R.drawable.ic_icon)
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
        window.statusBarColor = android.graphics.Color.rgb(18, 18, 18)
        setContent {
            TestJetpackComposeTheme {
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

@Composable
fun rect() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Red)
        )
    }
}

@Composable
fun Test() {
    var counter by remember { mutableStateOf(0) }
    var state by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("false") }

    Surface(modifier = Modifier.padding(16.dp)) {
        Column {
            Text(text = "Tile properties", fontSize = 45.sp)
            Row(modifier = Modifier.padding(top = 20.dp)) {
                OutlinedButton(
                    onClick = {},
                    border = BorderStroke(0.dp, Color.White),
                    modifier = Modifier
                        .height(80.dp)
                        .width(80.dp)
                ) {
                    Text("TEST", color = Color.White)
                }
                Row(modifier = Modifier.height(80.dp).padding(bottom = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = text,
                        modifier = Modifier.padding(start = 20.dp),
                        onValueChange = { text = it },
                        label = {
                            Row {
                                Text(text = "Text ", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "tile tag", fontSize = 15.sp)
                            }
                        }
                    )
                }
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