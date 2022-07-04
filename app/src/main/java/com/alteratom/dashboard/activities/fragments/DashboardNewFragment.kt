package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.FolderTree.saveToFile
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.setCurrentDashboard
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.fragments.dashboard_properties.DashboardPropertiesFragment
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.demons.Daemon
import com.alteratom.dashboard.foreground_service.demons.DaemonsManager
import kotlin.random.Random

class DashboardNewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ComposeTheme(Theme.isDark) {
                    //Background
                    Box(modifier = Modifier.background(colors.background))

                    Column(
                        Modifier.padding(bottom = 80.dp),
                        Arrangement.Center,
                        CenterHorizontally
                    ) {
                        Text(
                            "Pick new dashboard",
                            fontSize = 40.sp,
                            color = colors.a,
                            modifier = Modifier.fillMaxWidth(.85f)
                        )

                        Text(
                            "You will be redirected to its\nproperties afterwards",
                            fontSize = 15.sp,
                            color = colors.b,
                            modifier = Modifier.fillMaxWidth(.85f)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth(.85f)
                                .padding(top = 20.dp)
                                .border(
                                    BorderStroke(0.dp, colors.color),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp)
                                .padding(top = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f)
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        val name = kotlin.math
                                            .abs(Random.nextInt())
                                            .toString()

                                        val dashboard = Dashboard(name, Daemon.Type.MQTTD)
                                        dashboards.add(dashboard)
                                        dashboards.saveToFile()

                                        DaemonsManager.notifyDashboardAdded(dashboard)

                                        if (setCurrentDashboard(dashboard.id)) fm.replaceWith(
                                            DashboardPropertiesFragment(), false
                                        )
                                    }
                                    .border(
                                        BorderStroke(0.dp, colors.color),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "MQTT",
                                    fontSize = 20.sp,
                                    color = colors.b
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f)
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        BorderStroke(0.dp, colors.color.copy(.3f)),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "COMING\nLATER",
                                    modifier = Modifier.rotate(20f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = colors.b
                                )
                                Text(
                                    "BLUETOOTH",
                                    fontSize = 20.sp,
                                    color = colors.b,
                                    modifier = Modifier.alpha(.3f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f)
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        BorderStroke(0.dp, colors.color.copy(.3f)),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "COMING\nLATER",
                                    modifier = Modifier.rotate(-15f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = colors.b
                                )
                                Image(
                                    painterResource(R.drawable.bg_espmesh),
                                    "",
                                    contentScale = ContentScale.FillHeight,
                                    colorFilter = ColorFilter.tint(colors.b),
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(14.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f)
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        BorderStroke(0.dp, colors.color.copy(.3f)),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "COMING\nLATER",
                                    modifier = Modifier.rotate(10f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = colors.b
                                )
                                Image(
                                    painterResource(R.drawable.bg_espnow),
                                    "",
                                    contentScale = ContentScale.FillHeight,
                                    colorFilter = ColorFilter.tint(colors.b),
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}