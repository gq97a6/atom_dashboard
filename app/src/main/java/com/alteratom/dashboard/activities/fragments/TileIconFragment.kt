package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
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
import com.alteratom.dashboard.G.getIconRes
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.icon.*
import com.alteratom.dashboard.icon.Icons.icons
import com.alteratom.databinding.FragmentTileIconBinding
import java.util.*


class TileIconFragment : Fragment(R.layout.fragment_tile_icon) {
    private lateinit var b: FragmentTileIconBinding

    private lateinit var adapter: IconAdapter

    //override fun onCreateView(
    //    inflater: LayoutInflater,
    //    container: ViewGroup?,
    //    savedInstanceState: Bundle?
    //): View {
    //    b = FragmentTileIconBinding.inflate(inflater, container, false)
    //    return b.root
    //}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theme.apply(context = requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ComposeTheme(Theme.isDark) {
                    //Background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.background)
                    )

                    LazyColumn(modifier = Modifier.padding(horizontal = 15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        item {
                            Box(
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .size(100.dp)
                                    .border(
                                        BorderStroke(0.dp, colors.color),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    painterResource(getIconRes()),
                                    "",
                                    tint = colors.a,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .aspectRatio(1f)
                                        .padding(top = 7.dp)
                                )
                            }
                        }

                        item {
                            Column(modifier = Modifier.fillMaxWidth().aspectRatio(6/5f), verticalArrangement = Arrangement.SpaceEvenly) {
                                for (i in 40..100 step 20) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        for (ii in 0..300 step 60) {
                                            val hsv = if (theme.a.isDark) floatArrayOf(ii.toFloat(), i.toFloat() / 100, 1f)
                                            else floatArrayOf(ii.toFloat(), 1f, i.toFloat() / 100)
                                            val colorPallet = theme.a.getColorPallet(hsv, true)

                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(colorPallet.cc.a)
                                            )
                                        }
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Box(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(40.dp)
                                            .border(
                                                BorderStroke(0.dp, colors.color),
                                                RoundedCornerShape(6.dp)
                                            )
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
                            }
                        }

                        for (c in Icons.cats) {
                            val catUp =
                                c.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                            item {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .border(
                                            BorderStroke(0.dp, colors.color),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(5.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(catUp, fontSize = 20.sp, color = colors.a)
                                }
                            }

                            items(icons.filter { it.value.cat == c }.values.toList()) {
                                icons.filter { it.value.cat == c }.values.toList()
                            }
                            //for(i in icons.filter { it.cat == c }) {
                            //}
                        }
                    }
                }
            }
        }
    }

    /*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        theme.apply(b.root, requireContext(), true)
        viewConfig()
        setupRecyclerView()
    }

    private fun viewConfig() {
        b.tiIcon.setBackgroundResource(getIconRes())
        onColorChange(getIconColorPallet())
    }

    private fun onColorChange(colorPallet: Theme.ColorPallet) {
        b.tiIcon.backgroundTintList = ColorStateList.valueOf(colorPallet.color)
        val drawable = b.tiIconFrame.background as? GradientDrawable
        drawable?.setStroke(1, colorPallet.color)
        drawable?.cornerRadius = 15f
    }

    private fun setupRecyclerView() {
        val spanCount = 6

        adapter = IconAdapter(requireContext(), spanCount)
        adapter.setHasStableIds(true)
        adapter.applyIconSet("l")

        adapter.onColorChange = { _, colorPallet ->
            onColorChange(colorPallet)
        }

        adapter.onIconChange = {
            b.tiIcon.setBackgroundResource(it)
        }

        class CustomGridLayoutManager(c: Context, sc: Int) : GridLayoutManager(c, sc) {
            override fun supportsPredictiveItemAnimations(): Boolean = false
        }

        val layoutManager =
            CustomGridLayoutManager(requireContext(), spanCount)

        layoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    adapter.list[position].spanCount.let {
                        return if (it == -1) spanCount else it
                    }
                }
            }

        b.tiRecyclerView.layoutManager = layoutManager
        b.tiRecyclerView.adapter = adapter
    }
     */
}