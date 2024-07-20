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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontStyle.Companion.Normal
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activity.MainActivity.Companion.fm
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.compose_global.BasicButton
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.compose_global.FrameBox
import com.alteratom.dashboard.compose_global.LabeledCheckbox
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.compose_global.nrClickable
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.daemon.daemons.mqttd.MqttConfig
import com.alteratom.dashboard.daemon.daemons.mqttd.Mqttd
import com.alteratom.dashboard.fragment.DashboardPropertiesFragment
import com.alteratom.dashboard.helper_objects.DialogBuilder.buildConfirm
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
                var conStatus by remember { mutableStateOf("") }
                var conReason by remember { mutableStateOf("") }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    var enabled by remember { mutableStateOf(aps.dashboard.mqtt.isEnabled) }
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
                            if (aps.isLicensed || aps.dashboardIndex < 2) {
                                enabled = it
                                aps.dashboard.mqtt.isEnabled = it
                                aps.dashboard.daemon?.notifyConfigChanged()
                            } else {
                                with(fragment) { requireContext().proAlert(requireActivity()) }
                            }
                        }
                    )

                    aps.dashboard.daemon?.let { daemon ->
                        daemon.statePing.observe(fragment.viewLifecycleOwner) { state ->
                            when (daemon) {
                                is Mqttd -> {
                                    conStatus = when (daemon.state) {
                                        Mqttd.State.DISCONNECTED -> "DISCONNECTED"
                                        Mqttd.State.FAILED -> "ATTEMPTING"
                                        Mqttd.State.ATTEMPTING -> "ATTEMPTING"
                                        Mqttd.State.CONNECTED -> "CONNECTED"
                                        Mqttd.State.CONNECTED_SSL -> "CONNECTED"
                                    }

                                    conReason = state ?: ""
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
                            .height(35.dp)
                            .fillMaxWidth(.9f)
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

                if (conReason.isNotEmpty()) Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = colors.a)) { append("Details: ") }
                        withStyle(style = SpanStyle(color = colors.b)) { append(conReason) }
                    },
                    fontSize = 11.sp,
                    color = colors.b,
                    modifier = Modifier.padding(vertical = 5.dp)
                )

                var address by remember { mutableStateOf(aps.dashboard.mqtt.address) }
                EditText(
                    label = { Text("Address and protocol") },
                    value = address,
                    shape = RoundedCornerShape(6.dp, 6.dp, 0.dp, 0.dp),
                    onValueChange = { it ->
                        address = it
                        it.trim().let {
                            if (aps.dashboard.mqtt.address != it) {
                                aps.dashboard.mqtt.address = it
                                aps.dashboard.daemon?.notifyConfigChanged()
                            }
                        }
                    }
                )

                var protocol by remember { mutableStateOf(aps.dashboard.mqtt.protocol) }
                Row(
                    Modifier
                        .offset(y = (-1).dp)
                        .height(40.dp)
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            color = colors.b,
                            shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                        )
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .background(
                                color = if (protocol == Mqttd.Protocol.TCP) colors.d.copy(alpha = .4f) else Color.Transparent,
                                shape = RoundedCornerShape(bottomStart = 6.dp)
                            )
                            .clickable {
                                protocol = Mqttd.Protocol.TCP
                                aps.dashboard.mqtt.protocol = Mqttd.Protocol.TCP
                                aps.dashboard.daemon?.notifyConfigChanged()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TCP", fontSize = 12.sp, color = colors.a)
                    }

                    Divider(
                        Modifier
                            .fillMaxHeight()
                            .width(1.dp), color = colors.b
                    )

                    Box(
                        Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .background(
                                color = if (protocol == Mqttd.Protocol.SSL) colors.d.copy(alpha = .4f) else Color.Transparent,
                                shape = RoundedCornerShape(0.dp)
                            )
                            .clickable {
                                protocol = Mqttd.Protocol.SSL
                                aps.dashboard.mqtt.protocol = Mqttd.Protocol.SSL
                                aps.dashboard.daemon?.notifyConfigChanged()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SSL", fontSize = 12.sp, color = colors.a)
                    }

                    Divider(
                        Modifier
                            .fillMaxHeight()
                            .width(1.dp), color = colors.b
                    )

                    Box(
                        Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .background(
                                color = if (protocol == Mqttd.Protocol.WS) colors.d.copy(alpha = .4f) else Color.Transparent,
                                shape = RoundedCornerShape(0.dp)
                            )
                            .clickable {
                                protocol = Mqttd.Protocol.WS
                                aps.dashboard.mqtt.protocol = Mqttd.Protocol.WS
                                aps.dashboard.daemon?.notifyConfigChanged()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("WS", fontSize = 12.sp, color = colors.a)
                    }

                    Divider(
                        Modifier
                            .fillMaxHeight()
                            .width(1.dp), color = colors.b
                    )

                    Box(
                        Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .background(
                                color = if (protocol == Mqttd.Protocol.WSS) colors.d.copy(alpha = .4f) else Color.Transparent,
                                shape = RoundedCornerShape(bottomEnd = 6.dp)
                            )
                            .clickable {
                                protocol = Mqttd.Protocol.WSS
                                aps.dashboard.mqtt.protocol = Mqttd.Protocol.WSS
                                aps.dashboard.daemon?.notifyConfigChanged()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("WSS", fontSize = 12.sp, color = colors.a)
                    }
                }

                var port by remember {
                    mutableStateOf(aps.dashboard.mqtt.port.let {
                        if (it != -1) it.toString() else ""
                    })
                }
                EditText(
                    label = { Text("Port") },
                    value = port,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { it ->
                        port = it.filter { it.isDigit() }.take(5)
                        (it.trim().take(5).toIntOrNull() ?: (-1)).let {
                            if (aps.dashboard.mqtt.port != it) {
                                aps.dashboard.mqtt.port = it
                                aps.dashboard.daemon?.notifyConfigChanged()
                            }
                        }
                    }
                )

                var id by remember { mutableStateOf(aps.dashboard.mqtt.clientId) }
                EditText(
                    label = { Text("Unique client ID") },
                    value = id,
                    onValueChange = {
                        id = it
                        aps.dashboard.mqtt.clientId = it.trim().ifBlank {
                            abs(Random.nextInt(100000000, 999999999)).toString()
                        }
                        aps.dashboard.daemon?.notifyConfigChanged()
                    },
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .onFocusChanged {
                            //Update field after unFocus in case user left it blank
                            //and it got generated in the background
                            if (!it.isFocused) id = aps.dashboard.mqtt.clientId
                        }
                )

                var keepAliveInterval by remember { mutableStateOf(aps.dashboard.mqtt.keepAlive.toString()) }
                EditText(
                    label = { Text("Keep alive interval") },
                    value = keepAliveInterval,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        keepAliveInterval = it.filter { it.isDigit() }.take(5)
                        aps.dashboard.mqtt.keepAlive =
                            (it.trim().take(5).toIntOrNull() ?: 60).coerceIn(0..65535)
                        aps.dashboard.daemon?.notifyConfigChanged()
                    },
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .onFocusChanged {
                            //Update field after unFocus in case user has left invalid value
                            if (!it.isFocused) keepAliveInterval =
                                aps.dashboard.mqtt.keepAlive.toString()
                        }
                )

                var queryString by remember { mutableStateOf(aps.dashboard.mqtt.queryString) }
                var serverPath by remember { mutableStateOf(aps.dashboard.mqtt.serverPath) }
                if (protocol == Mqttd.Protocol.WSS || protocol == Mqttd.Protocol.WS) {
                    EditText(
                        label = { Text("Query string") },
                        value = queryString,
                        onValueChange = {
                            queryString = it.take(30)
                            aps.dashboard.mqtt.queryString = queryString
                            aps.dashboard.daemon?.notifyConfigChanged()
                        },
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    EditText(
                        label = { Text("Server path") },
                        value = serverPath,
                        onValueChange = {
                            serverPath = it.take(30)
                            aps.dashboard.mqtt.serverPath = serverPath
                            aps.dashboard.daemon?.notifyConfigChanged()
                        },
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "Example: mqtt",
                        fontSize = 11.sp,
                        color = colors.b,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                var sslShow by remember { mutableStateOf(false) }
                var copyShow by remember { mutableStateOf(false) }

                if (protocol == Mqttd.Protocol.SSL || protocol == Mqttd.Protocol.WSS) BasicButton(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .fillMaxWidth(.8f)
                        .padding(top = 12.dp),
                    onClick = { sslShow = true }
                ) {
                    Text("CONFIGURE SSL", fontSize = 10.sp, color = colors.a)
                }

                BasicButton(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .align(Alignment.Start)
                        .fillMaxWidth(.6f),
                    onClick = {
                        if (aps.dashboards.size <= 1)
                            createToast(fragment.requireContext(), "No dashboards to copy from")
                        else if (aps.isLicensed || aps.dashboardIndex < 2) copyShow = true
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
                                .heightIn(max = 500.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.background.copy(.8f))
                                .padding(15.dp),
                        ) {
                            Text(
                                text = "Select dashboard\nto copy from",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.color
                            )
                            LazyColumn(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 5.dp)
                            ) {
                                items(aps.dashboards.filter { it != aps.dashboard }) {
                                    Row(
                                        modifier = Modifier
                                            .padding(top = 10.dp)
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .clickable {
                                                aps.dashboard.apply {
                                                    mqtt = it.mqtt.deepCopy() ?: MqttConfig()
                                                    mqtt.clientId = abs(Random.nextInt()).toString()
                                                    daemon?.notifyConfigChanged()
                                                }
                                                copyShow = false
                                                fm.replaceWith(
                                                    DashboardPropertiesFragment(),
                                                    false
                                                )
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .fillMaxSize()
                                                .border(
                                                    BorderStroke(1.dp, Color(it.pallet.color)),
                                                    RoundedCornerShape(6.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painterResource(it.iconRes),
                                                contentDescription = "",
                                                tint = Color(it.pallet.a),
                                                modifier = Modifier
                                                    .padding(15.dp)
                                                    .fillMaxSize()
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(
                                                    BorderStroke(1.dp, Color(it.pallet.color)),
                                                    RoundedCornerShape(6.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                it.name.uppercase(Locale.getDefault()),
                                                fontSize = 20.sp,
                                                color = Color(it.pallet.a)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (sslShow) {
                    Dialog({ sslShow = false }) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.background.copy(.9f))
                                .padding(15.dp),
                        ) {
                            var trust by remember { mutableStateOf(aps.dashboard.mqtt.sslTrustAll) }

                            var ca by remember { mutableStateOf(aps.dashboard.mqtt.caFileName) }
                            var client by remember { mutableStateOf(aps.dashboard.mqtt.clientFileName) }
                            var key by remember { mutableStateOf(aps.dashboard.mqtt.keyFileName) }

                            fun getCaCert() =
                                fragment.openFile { str, file ->
                                    aps.dashboard.mqtt.let { m ->
                                        m.caCertStr = str
                                        if (m.caCert != null) {
                                            m.caFileName = file ?: "ca.crt"
                                            ca = aps.dashboard.mqtt.caFileName
                                            aps.dashboard.daemon?.notifyConfigChanged()
                                        } else createToast(
                                            fragment.requireContext(),
                                            "Certificate error"
                                        )
                                    }
                                }

                            fun getClientCert() =
                                fragment.openFile { str, file ->
                                    aps.dashboard.mqtt.let { m ->
                                        m.clientCertStr = str
                                        if (m.clientCert != null) {
                                            m.clientFileName = file ?: "client.crt"
                                            client = aps.dashboard.mqtt.clientFileName
                                            aps.dashboard.daemon?.notifyConfigChanged()
                                        } else createToast(
                                            fragment.requireContext(),
                                            "Certificate error"
                                        )
                                    }
                                }

                            fun getClientKey() = fragment.openFile { str, file ->
                                aps.dashboard.mqtt.let { m ->
                                    m.clientKeyStr = str
                                    if (m.clientKey != null) {
                                        m.keyFileName = file ?: "client.key"
                                        key = aps.dashboard.mqtt.keyFileName
                                        aps.dashboard.daemon?.notifyConfigChanged()
                                    } else createToast(
                                        fragment.requireContext(),
                                        "Key error"
                                    )
                                }
                            }

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LabeledCheckbox(
                                    modifier = Modifier,
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
                                                    message = "Confirm override",
                                                    label = "CONFIRM"
                                                ) {
                                                    trust = true
                                                    aps.dashboard.mqtt.sslTrustAll = true
                                                    aps.dashboard.daemon?.notifyConfigChanged()
                                                }
                                        } else {
                                            trust = false
                                            aps.dashboard.mqtt.sslTrustAll = false
                                            aps.dashboard.daemon?.notifyConfigChanged()
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
                            Text(
                                "This option disables server certificate verification. " +
                                        "Proceed with caution as this makes your connection vulnerable to man-in-the-middle attacks.",
                                fontSize = 11.sp,
                                color = colors.b,
                                modifier = Modifier.padding(top = 3.dp)
                            )

                            EditText(
                                enabled = false,
                                label = { Text("Custom CA certificate") },
                                value = ca,
                                onValueChange = {},
                                modifier = Modifier
                                    .nrClickable {
                                        if (aps.dashboard.mqtt.caCert == null) getCaCert()
                                    }
                                    .padding(top = 15.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (aps.dashboard.mqtt.caCert == null) getCaCert()
                                            else {
                                                ca = ""
                                                aps.dashboard.mqtt.caFileName = ""
                                                aps.dashboard.mqtt.caCertStr = null
                                                aps.dashboard.daemon?.notifyConfigChanged()
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
                            Text(
                                "Custom CA certificate that signed the server certificate. " +
                                        "Supply if your server certificate is self-signed. " +
                                        "Server address supplied in configuration must be included in SAN of server certificate.",
                                fontSize = 11.sp,
                                color = colors.b,
                                modifier = Modifier.padding(top = 3.dp)
                            )

                            EditText(
                                enabled = false,
                                label = { Text("Client certificate") },
                                value = client,
                                onValueChange = {},
                                modifier = Modifier
                                    .nrClickable {
                                        if (aps.dashboard.mqtt.clientCert == null) getClientCert()
                                    }
                                    .padding(top = 5.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (aps.dashboard.mqtt.clientCert == null) getClientCert()
                                            else {
                                                client = ""
                                                aps.dashboard.mqtt.clientFileName = ""
                                                aps.dashboard.mqtt.clientCertStr = null
                                                aps.dashboard.daemon?.notifyConfigChanged()
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
                                        if (aps.dashboard.mqtt.clientKey == null) getClientKey()
                                    }
                                    .padding(top = 5.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (aps.dashboard.mqtt.clientKey == null) getClientKey()
                                            else {
                                                key = ""
                                                aps.dashboard.mqtt.keyFileName = ""
                                                aps.dashboard.mqtt.clientKeyStr = null
                                                aps.dashboard.daemon?.notifyConfigChanged()
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
                        }
                    }
                }

                var cred by remember { mutableStateOf(aps.dashboard.mqtt.includeCred) }
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
                            aps.dashboard.mqtt.includeCred = it
                            aps.dashboard.daemon?.notifyConfigChanged()
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
                        var userHidden by remember { mutableStateOf(aps.dashboard.mqtt.username.isNotEmpty()) }
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
                                    if (aps.dashboard.mqtt.username != it) {
                                        aps.dashboard.mqtt.username = it
                                        aps.dashboard.daemon?.notifyConfigChanged()
                                    }
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    user = ""
                                    userHidden = false
                                    aps.dashboard.mqtt.username = ""
                                    aps.dashboard.daemon?.notifyConfigChanged()
                                }) {
                                    Icon(
                                        painterResource(R.drawable.il_interface_multiply),
                                        "",
                                        tint = colors.b
                                    )
                                }
                            }
                        )

                        var passHidden by remember { mutableStateOf(aps.dashboard.mqtt.pass.isNotEmpty()) }
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
                                    if (aps.dashboard.mqtt.pass != it) {
                                        aps.dashboard.mqtt.pass = it
                                        aps.dashboard.daemon?.notifyConfigChanged()
                                    }
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    pass = ""
                                    passHidden = false
                                    aps.dashboard.mqtt.pass = ""
                                    aps.dashboard.daemon?.notifyConfigChanged()
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