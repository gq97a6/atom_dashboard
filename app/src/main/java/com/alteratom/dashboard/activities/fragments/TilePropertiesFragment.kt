import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.G.getIconColorPallet
import com.alteratom.dashboard.G.getIconHSV
import com.alteratom.dashboard.G.getIconRes
import com.alteratom.dashboard.G.setIconHSV
import com.alteratom.dashboard.G.setIconKey
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.Theme.Companion.artist
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.Theme.Companion.isDark
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.fragments.TileIconFragment
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.switcher.TileSwitcher
import com.alteratom.databinding.FragmentTilePropertiesBinding
import com.alteratom.tile.types.button.ButtonTile
import com.alteratom.tile.types.button.TextTile
import com.alteratom.tile.types.button.compose.ButtonTileCompose
import com.alteratom.tile.types.color.ColorTile
import com.alteratom.tile.types.color.compose.*
import com.alteratom.tile.types.lights.LightsTile
import com.alteratom.tile.types.pick.SelectTile
import com.alteratom.tile.types.slider.SliderTile
import com.alteratom.tile.types.switch.SwitchTile
import com.alteratom.tile.types.terminal.TerminalTile
import com.alteratom.tile.types.thermostat.ThermostatTile
import com.alteratom.tile.types.time.TimeTile
import java.util.*

class TilePropertiesFragment : Fragment(R.layout.fragment_tile_properties) {
    private lateinit var b: FragmentTilePropertiesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().window.statusBarColor = artist.colors.background
        WindowInsetsControllerCompat(requireActivity().window, requireActivity().window.decorView)
            .isAppearanceLightStatusBars = !isDark

        return ComposeView(requireContext()).apply {
            setContent {
                ComposeTheme(isDark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        G.tile.dashboard?.type?.let {
                            when (G.tile) {
                                is ButtonTile -> ButtonTileCompose
                                is ColorTile -> ColorTileCompose
                                is LightsTile -> LightsTileCompose
                                is SelectTile -> SelectTileCompose
                                is SliderTile -> SliderTileCompose
                                is SwitchTile -> SwitchTileCompose
                                is TerminalTile -> TerminalTileCompose
                                is TextTile -> TextTileCompose
                                is ThermostatTile -> ThermostatTileCompose
                                is TimeTile -> TimeTileCompose
                                else -> ButtonTileCompose
                            }.compose(it)
                        }
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.tpRoot.onInterceptTouch = { e ->
            TileSwitcher.handle(e)
        }
    }
}

object TilePropComp {
    @Composable
    inline fun Box(crossinline content: @Composable () -> Unit) {

        Surface(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = "Tile properties", fontSize = 45.sp, color = colors.color)
                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {
                            getIconHSV = { tile.hsv }
                            getIconRes = { tile.iconRes }
                            getIconColorPallet = { tile.colorPallet }

                            setIconHSV = { hsv -> tile.hsv = hsv }
                            setIconKey = { key -> tile.iconKey = key }

                            fm.replaceWith(TileIconFragment())
                        },
                        border = BorderStroke(0.dp, colors.color),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(painterResource(tile.iconRes), "")
                    }

                    val typeTag = tile.typeTag.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }

                    var tag by remember { mutableStateOf(tile.tag) }
                    EditText(
                        label = { BoldStartText("$typeTag ", "tile tag") },
                        value = tag,
                        onValueChange = {
                            tag = it
                            tile.tag = it
                        },
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                content()

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
        }

        /*b.tpLeft.setOnClickListener {
            TileSwitcher.switch(true)
        }

        b.tpRight.setOnClickListener {
            TileSwitcher.switch(false)
        }
         */

        NavigationArrows({}, {})
    }

    @Composable
    inline fun CommunicationBox(crossinline content: @Composable () -> Unit) {
        var state by remember { mutableStateOf(true) }
        val rotation = if (state) 0f else 180f

        //switchMqttTab(settings.mqttTabShow, 0)
        // dashboard.daemon.notifyOptionsChanged()
        //settings.mqttTabShow = state

        val angle: Float by animateFloatAsState(
            targetValue = if (rotation > 360 - rotation) {
                -(360 - rotation)
            } else rotation,
            animationSpec = tween(durationMillis = 200, easing = LinearEasing)
        )

        FrameBox(a = "Communication: ", b = "MQTT") {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabeledSwitch(
                        label = { Text("Enabled:", fontSize = 15.sp, color = colors.a) },
                        checked = state,
                        onCheckedChange = { state = it }
                    )

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = { state = !state }
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_arrow), "",
                            tint = colors.a,
                            modifier = Modifier
                                .size(40.dp)
                                .rotate(angle)
                        )
                    }
                }

                AnimatedVisibility(visible = state) {
                    Column {
                        content()
                    }
                }
            }
        }
    }

    @Composable
    fun Communication0() {
        var text by remember { mutableStateOf("false") }

        //b.tpMqttSub.setText(tile.mqtt.subs["base"])
        //b.tpMqttPub.setText(tile.mqtt.pubs["base"])

        /*
        b.tpMqttPub.addTextChangedListener {
            tile.mqtt.pubs["base"] = (it ?: "").toString()
            //dashboard.daemon.notifyOptionsChanged()
        }

        b.tpMqttSub.addTextChangedListener {
            tile.mqtt.subs["base"] = (it ?: "").toString()
            //dashboard.daemon.notifyOptionsChanged()
        }
         */

        EditText(
            label = { Text("Subscribe topic") },
            value = text,
            onValueChange = { text = it }
        )

        EditText(
            label = { Text("Publish topic") },
            value = text,
            onValueChange = { text = it },
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(painterResource(R.drawable.il_file_copy), "", tint = colors.b)
                }
            }
        )
    }

    @Composable
    fun Communication1(
        retain: Boolean = true, pointer: @Composable () -> Unit = {
            var text by remember { mutableStateOf("false") }
            EditText(
                label = { Text("Payload JSON pointer") },
                value = text,
                onValueChange = { text = it }
            )
        }
    ) {
        var index by remember { mutableStateOf(0) }
        var state by remember { mutableStateOf(true) }
        var text by remember { mutableStateOf("false") }

        /*
        b.tpQos.check(
            when (tile.mqtt.qos) {
                0 -> R.id.tp_qos0
                1 -> R.id.tp_qos1
                2 -> R.id.tp_qos2
                else -> R.id.tp_qos1
            }
        )

        b.tpQos.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
            tile.mqtt.qos = when (id) {
                R.id.tp_qos0 -> 0
                R.id.tp_qos1 -> 1
                R.id.tp_qos2 -> 2
                else -> 1
            }
            //dashboard.daemon.notifyOptionsChanged()
        }

        tile.mqtt.jsonPaths["base"] = (it ?: "").toString()
         */

        RadioGroup(
            listOf(
                "QoS 0: At most once. No guarantee.",
                "QoS 1: At least once. (Recommended)",
                "QoS 2: Delivery exactly once."
            ), "Quality of Service (MQTT protocol):",
            index,
            { index = it },
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
        )

        if (retain) {
            LabeledSwitch(
                label = { Text("Retain massages:", fontSize = 15.sp, color = colors.a) },
                checked = state,
                onCheckedChange = { state = it }
            )
        }

        LabeledSwitch(
            label = { Text("Confirm publishing:", fontSize = 15.sp, color = colors.a) },
            checked = state,
            onCheckedChange = { state = it }
        )

        LabeledSwitch(
            label = { Text("Payload is JSON:", fontSize = 15.sp, color = colors.a) },
            checked = state,
            onCheckedChange = { state = it }
        )

        pointer()
    }

    @Composable
    fun Communication() {
        Communication0()
        Communication1()
    }

    @Composable
    fun Notification() {
        var state by remember { mutableStateOf(true) }

        //b.tpNotSilentSwitch.visibility = if (tile.doNotify) VISIBLE else GONE

        FrameBox(a = "Notifications and log") {
            Column {
                LabeledSwitch(
                    label = { Text("Log new values:", fontSize = 15.sp, color = colors.a) },
                    checked = state,
                    onCheckedChange = { state = it },
                )

                LabeledSwitch(
                    label = {
                        Text(
                            "Notify on receive:",
                            fontSize = 15.sp,
                            color = colors.a
                        )
                    },
                    checked = state,
                    onCheckedChange = { state = it },
                )

                LabeledCheckbox(
                    label = {
                        Text(
                            "Make notification quiet",
                            fontSize = 15.sp,
                            color = colors.a
                        )
                    },
                    checked = state,
                    onCheckedChange = { state = it },
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }

    @Composable
    fun PairList(
        options: MutableList<Pair<String, String>>,
        onRemove: (Int) -> Unit = {},
        onAdd: () -> Unit = {},
        onFirst: (Int, String) -> Unit = { _, _ -> },
        onSecond: (Int, String) -> Unit = { _, _ -> }
    ) {
        var options = remember { options.toMutableStateList() }

        FrameBox(a = "Modes list") {
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {
                            options.add("" to "")
                            onAdd()
                        },
                        border = BorderStroke(0.dp, colors.color),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("ADD OPTION", color = colors.a)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .padding(end = 32.dp, top = 10.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "ALIAS",
                        fontSize = 13.sp,
                        color = colors.a,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "PAYLOAD",
                        fontSize = 13.sp,
                        color = colors.a,
                        letterSpacing = 2.sp
                    )
                }

                options.forEachIndexed { index, pair ->

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = 5.dp)
                    ) {
                        EditText(
                            label = {},
                            value = pair.first,
                            onValueChange = {
                                options[index] = options[index].copy(first = it)
                                onFirst(index, it)
                            },
                            modifier = Modifier
                                .weight(1f)
                        )

                        EditText(
                            label = {},
                            value = pair.second,
                            onValueChange = {
                                options[index] = options[index].copy(second = it)
                                onSecond(index, it)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        )

                        Icon(
                            painterResource(R.drawable.il_interface_multiply),
                            "",
                            tint = colors.b,
                            modifier = Modifier
                                .padding(start = 10.dp, bottom = 13.dp)
                                .size(30.dp)
                                .nrClickable {
                                    if (options.size > 2) {
                                        options.removeAt(index)
                                        onRemove(index)
                                    }
                                }
                        )
                    }
                }

                //LazyColumn(
                //    modifier = Modifier
                //        .height(500.dp)
                //        .padding(bottom = 16.dp),
                //) {
                //    itemsIndexed(items = opts, key = { i, p -> p.hashCode() }) { index, pair ->
//
                //
                //    }
                //}
            }
        }
    }
}

// Test ------------------------------------------------------------------------------------------

//private object RippleCustomTheme : RippleTheme {
//
//    @Composable
//    override fun defaultColor() =
//        RippleTheme.defaultRippleColor(
//            Color(255, 255, 255),
//            lightTheme = false
//        )
//
//    @Composable
//    override fun rippleAlpha(): RippleAlpha =
//        RippleTheme.defaultRippleAlpha(
//            Color(255, 255, 255),
//            lightTheme = true
//        )
//}

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