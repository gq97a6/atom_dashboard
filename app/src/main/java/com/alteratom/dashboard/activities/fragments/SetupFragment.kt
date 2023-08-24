package com.alteratom.dashboard.activities.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.alteratom.R
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.compose_global.composeConstruct


class SetupFragment : Fragment() {

    var ready = MutableLiveData(false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fm.doOverrideOnBackPress = { true }
    }

    @SuppressLint("CoroutineCreationDuringComposition", "RememberReturnType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeConstruct(requireContext()) {
        var scaleInitialValue by remember { mutableFloatStateOf(1f) }
        var scaleTargetValue by remember { mutableFloatStateOf(.8f) }
        var scaleDuration by remember { mutableIntStateOf(2500) }

        val scale = rememberInfiniteTransition(label = "").animateFloat(
            initialValue = scaleInitialValue,
            targetValue = scaleTargetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(scaleDuration),
                repeatMode = RepeatMode.Reverse,
            ), label = ""
        )

        val rotation = rememberInfiniteTransition(label = "").animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500),
                repeatMode = RepeatMode.Reverse,
            ), label = ""
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Image(
                    painterResource(
                        if (Theme.isDark) R.drawable.ic_icon_light
                        else R.drawable.ic_icon
                    ), "",
                    modifier = Modifier
                        .padding(bottom = 100.dp)
                        .scale(scale.value)
                        .rotate(rotation.value)
                        .size(300.dp),
                    colorFilter = ColorFilter.tint(
                        Theme.colors.color.copy(alpha = .4f),
                        BlendMode.SrcAtop
                    )
                )
            }
        }

        remember {
            ready.observe(activity as MainActivity) {
                if (it == true) {
                    scaleDuration = 1000
                    scaleInitialValue = scale.value
                    scaleTargetValue = 0f
                }
            }
        }
    }
}
