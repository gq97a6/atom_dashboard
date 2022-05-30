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
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.remember
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
import com.alteratom.dashboard.icon.IconAdapter
import com.alteratom.dashboard.icon.Icons
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



    val cols = mutableListOf<Color>().apply {
        for (i in 40..100 step 20) {
            for (ii in 0..300 step 60) {
                val hsv = if (theme.a.isDark) floatArrayOf(
                    ii.toFloat(),
                    i.toFloat() / 100,
                    1f
                ) else floatArrayOf(ii.toFloat(), 1f, i.toFloat() / 100)

                add(theme.a.getColorPallet(hsv, true).cc.a)
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
                ComposeTheme(Theme.isDark) {
                    //Background
                    Box(modifier = Modifier.background(colors.background))
                    Box {

                        LazyVerticalGrid(
                            cells = GridCells.Fixed(6),
                            modifier = Modifier.padding(horizontal = 10.dp),
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
                                        .background(it)
                                )
                            }

                            item({ GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .height(45.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
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
                                            .aspectRatio(1f)
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
                                    BorderStroke(0.dp, colors.color),
                                    RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.background.copy(.8f))
                                .padding(10.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            Icon(
                                painterResource(getIconRes()),
                                "",
                                tint = colors.a,
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