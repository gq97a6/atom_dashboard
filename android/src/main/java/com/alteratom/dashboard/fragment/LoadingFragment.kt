package com.alteratom.dashboard.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.lifecycle.lifecycleScope
import com.alteratom.R
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activity.MainActivity
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.compose_global.composeConstruct
import com.alteratom.dashboard.helper_objects.FragmentManager.Animations.fadeLong
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoadingFragment : Fragment() {

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
        var closed by remember { mutableStateOf(false) }

        val scale by animateFloatAsState(
            label = "",
            targetValue = if (closed) 0f else 1f,
            animationSpec = tween(600)
        )

        val rotation by rememberInfiniteTransition(label = "").animateFloat(
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
            Image(
                painterResource(
                    if (Theme.isDark) R.drawable.ic_icon_light
                    else R.drawable.ic_icon
                ), "",
                modifier = Modifier
                    .padding(bottom = 100.dp)
                    .scale(scale)
                    .rotate(rotation)
                    .size(250.dp),
                colorFilter = ColorFilter.tint(
                    Theme.colors.color.copy(alpha = .4f),
                    BlendMode.SrcAtop
                )
            )
        }

        remember {
            //Wait for app state to be initialized
            aps.isInitialized.observe(activity as MainActivity) {
                if (it != true) return@observe
                closed = true
                fm.popBackstack(false, fadeLong)
            }
        }
    }
}
