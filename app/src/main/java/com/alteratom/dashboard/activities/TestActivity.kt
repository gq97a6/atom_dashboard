package com.alteratom.dashboard.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.ActivityHandler
import com.alteratom.dashboard.compose.BasicButton
import com.alteratom.dashboard.G
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.compose.ComposeTheme

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHandler.onCreate(this, false)

        G.theme.apply(context = this)

        setContent {
            ComposeTheme(Theme.isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Theme.colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp),
                    ) {
                        BasicButton(
                            onClick = {
                            },
                            Modifier
                        ) {
                            Text(
                                "LOGIN",
                                fontSize = 16.sp,
                                color = Theme.colors.a
                            )
                        }
                    }
                }
            }
        }
    }
}