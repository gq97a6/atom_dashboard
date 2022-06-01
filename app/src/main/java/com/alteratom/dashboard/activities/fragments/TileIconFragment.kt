package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.G.getIconColorPallet
import com.alteratom.dashboard.G.getIconRes
import com.alteratom.dashboard.G.setIconHSV
import com.alteratom.dashboard.G.setIconKey
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.icon.Icons
import java.util.*


class TileIconFragment : Fragment(R.layout.fragment_tile_icon) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theme.apply(context = requireContext())
    }


    val cols = mutableListOf<Pair<Color, FloatArray>>().apply {
        for (i in 40..100 step 20) {
            for (ii in 0..300 step 60) {
                val hsv = if (theme.a.isDark) floatArrayOf(
                    ii.toFloat(),
                    i.toFloat() / 100,
                    1f
                ) else floatArrayOf(ii.toFloat(), 1f, i.toFloat() / 100)

                add(theme.a.getColorPallet(hsv, true).cc.a to hsv)
            }
        }
    }.toList()

    val icons = Icons.cats.map { c ->
        val catUp = c.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
        catUp to Icons.icons.filter { it.value.cat == c }.values.toList()
    }.toMap()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                var iconRes by remember { mutableStateOf(getIconRes()) }
                var iconColor by remember { mutableStateOf(getIconColorPallet().cc.a) }

                ComposeTheme(Theme.isDark) {
                    //Background
                    Box(modifier = Modifier.background(colors.background))

                    Box {

                        LazyVerticalGrid(
                            cells = GridCells.Fixed(6),
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {

                            item({ GridItemSpan(6) }) {
                                Spacer(modifier = Modifier.height(140.dp))
                            }

                            items(cols, { GridItemSpan(1) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(it.first)
                                        .clickable {
                                            setIconHSV(it.second)
                                            iconColor = getIconColorPallet().cc.a
                                        }
                                )
                            }

                            item({ GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .height(45.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (theme.a.isDark) Color.White else Color.Black)
                                        .clickable {
                                            setIconHSV(floatArrayOf(0f, 0f, 0f))
                                            iconColor = getIconColorPallet().cc.a
                                        }
                                )
                            }

                            item({ GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .height(45.dp)
                                        .border(
                                            BorderStroke(0.dp, colors.color),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {

                                        }
                                        .padding(5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painterResource(R.drawable.il_interface_question),
                                        "",
                                        tint = colors.a,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .aspectRatio(1f)
                                    )
                                }
                            }

                            for (pair in icons) {
                                item({ GridItemSpan(6) }) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 15.dp)
                                            .padding(bottom = 5.dp)
                                            .padding(horizontal = 8.dp)
                                            .height(50.dp)
                                            .border(
                                                BorderStroke(0.dp, colors.color),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(start = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(pair.key, fontSize = 20.sp, color = colors.a)
                                    }
                                }

                                items(pair.value, { GridItemSpan(1) }) {
                                    Icon(
                                        painterResource(it.res),
                                        "",
                                        tint = colors.a,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .aspectRatio(1f)
                                            .clickable {
                                                val key =
                                                    Icons.icons.filterValues { i -> i.res == it.res }.keys
                                                setIconKey(key.first())
                                                iconRes = getIconRes()
                                            }
                                    )
                                }

                                item({ GridItemSpan(6) }) {
                                    Spacer(Modifier)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .size(100.dp)
                                .border(
                                    BorderStroke(0.dp, iconColor),
                                    RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.background.copy(.8f))
                                .padding(10.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            Icon(
                                painterResource(iconRes),
                                "",
                                tint = iconColor,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 7.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}