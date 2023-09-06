package com.alteratom.dashboard.compose_daemon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontStyle.Companion.Normal
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activity.fragments.DashboardPropertiesFragment
import com.alteratom.dashboard.compose_global.BasicButton
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.compose_global.FrameBox
import com.alteratom.dashboard.compose_global.LabeledCheckbox
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.compose_global.nrClickable
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.daemon.daemons.mqttd.MqttConfig
import com.alteratom.dashboard.daemon.daemons.mqttd.Mqttd
import com.alteratom.dashboard.`object`.DialogBuilder.buildConfirm
import com.alteratom.dashboard.`object`.FragmentManager.fm
import com.alteratom.dashboard.`object`.G.dashboard
import com.alteratom.dashboard.`object`.G.dashboardIndex
import com.alteratom.dashboard.`object`.G.dashboards
import com.alteratom.dashboard.`object`.Pro
import com.alteratom.dashboard.proAlert
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random


object DashboardPropertiesCompose : DaemonBasedCompose {

    @Composable
    override fun Mqttd(fragment: Fragment) {
        fragment as DashboardPropertiesFragment

        FrameBox("Communication:", " MQTT") {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    var enabled by remember { mutableStateOf(dashboard.mqtt.isEnabled) }
                    LabeledSwitch(
                        label = {
                            Text(
                                "Enabled:",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = enabled,
                        onCheckedChange = {
                            if (Pro.status || dashboardIndex < 2) {
                                enabled = it
                                dashboard.mqtt.isEnabled = it
                                dashboard.daemon?.notifyConfigChanged()
                            } else {
                                with(fragment) { requireContext().proAlert(requireActivity()) }
                            }
                        }
                    )

                    var conStatus by remember { mutableStateOf("") }

                    dashboard.daemon?.let {
                        it.statePing.observe(fragment.viewLifecycleOwner) { _ ->
                            when (it) {
                                is Mqttd -> {
                                    conStatus = when (it.state) {
                                        Mqttd.State.DISCONNECTED -> "DISCONNECTED"
                                        Mqttd.State.FAILED -> "FAILED"
                                        Mqttd.State.ATTEMPTING -> "ATTEMPTING"
                                        Mqttd.State.CONNECTED -> "CONNECTED"
                                        Mqttd.State.CONNECTED_SSL -> "CONNECTED"
                                    }
                                }
                            }
                        }
                    }

                    val alpha = rememberInfiniteTransition(label = "").animateFloat(
                        initialValue = 1f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(300, 200),
                            repeatMode = RepeatMode.Reverse,
                        ), label = ""
                    )

                    Box(
                        modifier = Modifier
                            .alpha(if (conStatus == "ATTEMPTING") alpha.value else 1f)
                            .fillMaxWidth(.9f)
                            .aspectRatio(4f)
                            .border(
                                BorderStroke(2.dp, colors.a),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Text(
                            conStatus,
                            modifier = Modifier.align(Alignment.Center),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = colors.a
                        )
                    }
                }

                var copyShow by remember { mutableStateOf(false) }

                BasicButton(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(.8f),
                    border = BorderStroke(2.dp, colors.b),
                    onClick = {
                        if (dashboards.size <= 1)
                            createToast(fragment.requireContext(), "No dashboards to copy from")
                        else if (Pro.status || dashboardIndex < 2) copyShow = true
                        else with(fragment) { requireContext().proAlert(requireActivity()) }
                    }
                ) {
                    Text("COPY PROPERTIES", fontSize = 10.sp, color = colors.a)
                }

                if (copyShow) {
                    Dialog({ copyShow = false }) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    BorderStroke(1.dp, colors.color),
                                    RoundedCornerShape(6.dp)
                                )
                                .background(colors.background.copy(.8f))
                                .padding(15.dp),
                        ) {
                            Text(
                                text = "Pick dashboard to copy from",
                                fontSize = 35.sp,
                                color = colors.color
                            )
                            LazyColumn(Modifier.fillMaxHeight(.8f)) {
                                items(dashboards.filter { it != dashboard }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 15.dp)
                                            .height(50.dp)
                                            .clickable {
                                                dashboard.mqtt =
                                                    it.mqtt.deepCopy() ?: MqttConfig()
                                                dashboard.mqtt.clientId =
                                                    abs(Random.nextInt()).toString()
                                                dashboard.daemon?.notifyConfigChanged()

                                                copyShow = false
                                                fm.replaceWith(
                                                    DashboardPropertiesFragment(),
                                                    false
                                                )
                                            }
                                            .border(
                                                BorderStroke(1.dp, colors.color),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(start = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            it.name.uppercase(Locale.getDefault()),
                                            fontSize = 20.sp,
                                            color = colors.a
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                var address by remember { mutableStateOf(dashboard.mqtt.address) }
                EditText(
                    label = { Text("Address") },
                    value = address,
                    onValueChange = { it ->
                        address = it
                        it.trim().let {
                            if (dashboard.mqtt.address != it) {
                                dashboard.mqtt.address = it
                                dashboard.daemon?.notifyConfigChanged()
                            }
                        }
                    }
                )

                var port by remember {
                    mutableStateOf(dashboard.mqtt.port.let {
                        if (it != -1) it.toString() else ""
                    })
                }
                EditText(
                    label = { Text("Port") },
                    value = port,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { it ->
                        port = it
                        (it.trim().toIntOrNull() ?: (-1)).let {
                            if (dashboard.mqtt.port != it) {
                                dashboard.mqtt.port = it
                                dashboard.daemon?.notifyConfigChanged()
                            }
                        }
                    }
                )

                var id by remember { mutableStateOf(dashboard.mqtt.clientId) }
                EditText(
                    label = { Text("Unique client ID") },
                    value = id,
                    onValueChange = { it ->
                        id = it
                        it.trim().let {
                            when {
                                it.isBlank() -> {
                                    dashboard.mqtt.clientId =
                                        abs(Random.nextInt()).toString()
                                    id = dashboard.mqtt.clientId
                                    dashboard.daemon?.notifyConfigChanged()
                                }

                                dashboard.mqtt.clientId != it -> {
                                    dashboard.mqtt.clientId = it
                                    dashboard.daemon?.notifyConfigChanged()
                                }
                            }
                        }
                    }
                )

                var sslShow by remember { mutableStateOf(false) }

                BasicButton(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(.8f)
                        .padding(top = 14.dp),
                    border = BorderStroke(2.dp, colors.b),
                    onClick = { sslShow = true }
                ) {
                    Text("CONFIGURE SSL", fontSize = 10.sp, color = colors.a)
                }

                if (sslShow) {
                    Dialog({ sslShow = false }) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    BorderStroke(1.dp, colors.color),
                                    RoundedCornerShape(6.dp)
                                )
                                .background(colors.background.copy(.9f))
                                .padding(15.dp),
                        ) {
                            var enable by remember { mutableStateOf(dashboard.mqtt.ssl) }
                            var trust by remember { mutableStateOf(dashboard.mqtt.sslTrustAll) }

                            var ca by remember { mutableStateOf(dashboard.mqtt.caFileName) }
                            var client by remember { mutableStateOf(dashboard.mqtt.clientFileName) }
                            var key by remember { mutableStateOf(dashboard.mqtt.keyFileName) }

                            fun getCaCert() =
                                fragment.openFile { str, file ->
                                    dashboard.mqtt.let { m ->
                                        m.caCertStr = str
                                        if (m.caCert != null) {
                                            m.caFileName = file ?: "ca.crt"
                                            ca = dashboard.mqtt.caFileName
                                            dashboard.daemon?.notifyConfigChanged()
                                        } else createToast(
                                            fragment.requireContext(),
                                            "Certificate error"
                                        )
                                    }
                                }

                            fun getClientCert() =
                                fragment.openFile { str, file ->
                                    dashboard.mqtt.let { m ->
                                        m.clientCertStr = str
                                        if (m.clientCert != null) {
                                            m.clientFileName = file ?: "client.crt"
                                            client = dashboard.mqtt.clientFileName
                                            dashboard.daemon?.notifyConfigChanged()
                                        } else createToast(
                                            fragment.requireContext(),
                                            "Certificate error"
                                        )
                                    }
                                }

                            fun getClientKey() =
                                fragment.openFile { str, file ->
                                    dashboard.mqtt.let { m ->
                                        m.clientKeyStr = str
                                        if (m.clientKey != null) {
                                            m.keyFileName = file ?: "client.key"
                                            key = dashboard.mqtt.keyFileName
                                            dashboard.daemon?.notifyConfigChanged()
                                        } else createToast(
                                            fragment.requireContext(),
                                            "Key error"
                                        )
                                    }
                                }

                            LabeledCheckbox(
                                label = {
                                    Text(
                                        "Enable SSL",
                                        fontSize = 15.sp,
                                        color = colors.a
                                    )
                                },
                                checked = enable,
                                onCheckedChange = {
                                    enable = it
                                    dashboard.mqtt.ssl = it
                                    dashboard.daemon?.notifyConfigChanged()
                                },
                                modifier = Modifier.padding(vertical = 10.dp)
                            )

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LabeledCheckbox(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    label = {
                                        Text(
                                            "Trust all certificates",
                                            fontSize = 15.sp,
                                            color = colors.a
                                        )
                                    },
                                    checked = trust,
                                    onCheckedChange = {
                                        if (it) {
                                            fragment.requireContext()
                                                .buildConfirm(
                                                    "Confirm override", "CONFIRM"
                                                ) {
                                                    trust = true
                                                    dashboard.mqtt.sslTrustAll = true
                                                    dashboard.daemon?.notifyConfigChanged()
                                                }
                                        } else {
                                            trust = false
                                            dashboard.mqtt.sslTrustAll = false
                                            dashboard.daemon?.notifyConfigChanged()
                                        }
                                    }
                                )

                                val alpha = rememberInfiniteTransition(label = "").animateFloat(
                                    initialValue = 1f,
                                    targetValue = 0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(300, 200),
                                        repeatMode = RepeatMode.Reverse,
                                    ), label = ""
                                )

                                Icon(
                                    painterResource(R.drawable.il_interface_exclamation_triangle),
                                    "",
                                    tint = colors.a,
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .alpha(if (trust) alpha.value else 0f)
                                        .size(20.dp)
                                )
                            }

                            EditText(
                                enabled = false,
                                label = { Text("Custom CA certificate") },
                                value = ca,
                                onValueChange = {},
                                modifier = Modifier
                                    .nrClickable {
                                        if (dashboard.mqtt.caCert == null) getCaCert()
                                    }
                                    .padding(top = 10.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (dashboard.mqtt.caCert == null) getCaCert()
                                            else {
                                                ca = ""
                                                dashboard.mqtt.caFileName = ""
                                                dashboard.mqtt.caCertStr = null
                                                dashboard.daemon?.notifyConfigChanged()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painterResource(if (ca.isEmpty()) R.drawable.il_interface_paperclip else R.drawable.il_interface_multiply),
                                            "",
                                            tint = colors.b
                                        )
                                    }
                                }
                            )

                            EditText(
                                enabled = false,
                                label = { Text("Client certificate") },
                                value = client,
                                onValueChange = {},
                                modifier = Modifier
                                    .nrClickable {
                                        if (dashboard.mqtt.clientCert == null) getClientCert()
                                    }
                                    .padding(top = 5.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (dashboard.mqtt.clientCert == null) getClientCert()
                                            else {
                                                client = ""
                                                dashboard.mqtt.clientFileName = ""
                                                dashboard.mqtt.clientCertStr = null
                                                dashboard.daemon?.notifyConfigChanged()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painterResource(if (client.isEmpty()) R.drawable.il_interface_paperclip else R.drawable.il_interface_multiply),
                                            "",
                                            tint = colors.b
                                        )
                                    }
                                }
                            )

                            EditText(
                                enabled = false,
                                label = { Text("Client key") },
                                value = key,
                                onValueChange = {},
                                modifier = Modifier
                                    .nrClickable {
                                        if (dashboard.mqtt.clientKey == null) getClientKey()
                                    }
                                    .padding(top = 5.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (dashboard.mqtt.clientKey == null) getClientKey()
                                            else {
                                                key = ""
                                                dashboard.mqtt.keyFileName = ""
                                                dashboard.mqtt.clientKeyStr = null
                                                dashboard.daemon?.notifyConfigChanged()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painterResource(if (key.isEmpty()) R.drawable.il_interface_paperclip else R.drawable.il_interface_multiply),
                                            "",
                                            tint = colors.b
                                        )
                                    }
                                }
                            )

                            var passwordHidden by remember { mutableStateOf(dashboard.mqtt.clientKeyPassword.isNotEmpty()) }
                            var password by remember { mutableStateOf(if (passwordHidden) "hidden" else "") }
                            EditText(
                                label = { Text("Key password") },
                                modifier = Modifier.padding(top = 5.dp),
                                value = password,
                                textStyle = TextStyle(fontStyle = if (passwordHidden) Italic else Normal),
                                onValueChange = { it ->
                                    if (passwordHidden) {
                                        password = ""
                                        passwordHidden = false
                                    } else password = it

                                    password.trim().let {
                                        if (dashboard.mqtt.clientKeyPassword != it) {
                                            dashboard.mqtt.clientKeyPassword = it
                                            dashboard.daemon?.notifyConfigChanged()
                                        }
                                    }
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        password = ""
                                        passwordHidden = false
                                        dashboard.mqtt.clientKeyPassword = ""
                                    }) {
                                        Icon(
                                            painterResource(R.drawable.il_interface_multiply),
                                            "",
                                            tint = colors.b
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                var cred by remember { mutableStateOf(dashboard.mqtt.includeCred) }
                var show by remember { mutableStateOf(false) }

                val rotation = if (show) 0f else 180f
                val angle: Float by animateFloatAsState(
                    targetValue = if (rotation > 360 - rotation) {
                        -(360 - rotation)
                    } else rotation,
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing), label = ""
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabeledCheckbox(
                        label = {
                            Text(
                                "Include login credentials",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = cred,
                        onCheckedChange = {
                            cred = it
                            show = it
                            dashboard.mqtt.includeCred = it
                            dashboard.daemon?.notifyConfigChanged()
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = {
                            show = !show
                        }
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

                AnimatedVisibility(
                    visible = show, enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        var userHidden by remember { mutableStateOf(dashboard.mqtt.username.isNotEmpty()) }
                        var user by remember { mutableStateOf(if (userHidden) "hidden" else "") }
                        EditText(
                            label = { Text("User name") },
                            value = user,
                            textStyle = TextStyle(fontStyle = if (userHidden) Italic else Normal),
                            onValueChange = { it ->
                                if (userHidden) {
                                    user = ""
                                    userHidden = false
                                } else user = it

                                user.trim().let {
                                    if (dashboard.mqtt.username != it) {
                                        dashboard.mqtt.username = it
                                        dashboard.daemon?.notifyConfigChanged()
                                    }
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    user = ""
                                    userHidden = false
                                    dashboard.mqtt.username = ""
                                }) {
                                    Icon(
                                        painterResource(R.drawable.il_interface_multiply),
                                        "",
                                        tint = colors.b
                                    )
                                }
                            }
                        )

                        var passHidden by remember { mutableStateOf(dashboard.mqtt.pass.isNotEmpty()) }
                        var pass by remember { mutableStateOf(if (passHidden) "hidden" else "") }
                        EditText(
                            label = { Text("Password") },
                            value = pass,
                            textStyle = TextStyle(fontStyle = if (passHidden) Italic else Normal),
                            onValueChange = { it ->
                                if (passHidden) {
                                    pass = ""
                                    passHidden = false
                                } else pass = it

                                pass.trim().let {
                                    if (dashboard.mqtt.pass != it) {
                                        dashboard.mqtt.pass = it
                                        dashboard.daemon?.notifyConfigChanged()
                                    }
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    pass = ""
                                    passHidden = false
                                    dashboard.mqtt.pass = ""
                                }) {
                                    Icon(
                                        painterResource(R.drawable.il_interface_multiply),
                                        "",
                                        tint = colors.b
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun Bluetoothd(fragment: Fragment) {
    }
}