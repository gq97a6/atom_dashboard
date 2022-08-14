package com.alteratom.dashboard.activities.fragments.dashboard_properties

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import com.alteratom.dashboard.DialogBuilder.buildConfirm
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboardIndex
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.Pro
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.compose.*
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose
import com.alteratom.dashboard.foreground_service.demons.Mqttd
import com.alteratom.dashboard.proAlert
import java.util.*
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
                    var enabled by remember { mutableStateOf(dashboard.mqttData.isEnabled) }
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
                                dashboard.mqttData.isEnabled = it
                                dashboard.daemon.notifyOptionsChanged()
                            } else {
                                with(fragment) { requireContext().proAlert(requireActivity()) }
                            }
                        }
                    )

                    var conStatus by remember { mutableStateOf("") }

                    dashboard.daemon.let {
                        it.isDone.observe(fragment.viewLifecycleOwner) { _ ->
                            when (it) {
                                is Mqttd -> {
                                    conStatus = when (it.status) {
                                        Mqttd.Status.DISCONNECTED -> "DISCONNECTED"
                                        Mqttd.Status.FAILED -> "FAILED"
                                        Mqttd.Status.ATTEMPTING -> "ATTEMPTING"
                                        Mqttd.Status.CONNECTED -> "CONNECTED"
                                        Mqttd.Status.CONNECTED_SSL -> "CONNECTED"
                                    }
                                }
                            }
                        }
                    }

                    val alpha = rememberInfiniteTransition().animateFloat(
                        initialValue = 1f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(300, 200),
                            repeatMode = RepeatMode.Reverse,
                        )
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
                                    BorderStroke(0.dp, colors.color),
                                    RoundedCornerShape(6.dp)
                                )
                                .background(colors.background.copy(.9f))
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
                                                dashboard.mqttData = it.mqttData.copy()
                                                dashboard.mqttData.clientId =
                                                    abs(Random.nextInt()).toString()
                                                dashboard.daemon.notifyOptionsChanged()

                                                copyShow = false
                                                MainActivity.fm.replaceWith(
                                                    DashboardPropertiesFragment(),
                                                    false
                                                )
                                            }
                                            .border(
                                                BorderStroke(0.dp, colors.color),
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

                var address by remember { mutableStateOf(dashboard.mqttData.address) }
                EditText(
                    label = { Text("Address") },
                    value = address,
                    onValueChange = {
                        address = it
                        it.trim().let {
                            if (dashboard.mqttData.address != it) {
                                dashboard.mqttData.address = it
                                dashboard.daemon.notifyOptionsChanged()
                            }
                        }
                    }
                )

                var port by remember {
                    mutableStateOf(dashboard.mqttData.port.let {
                        if (it != -1) it.toString() else ""
                    })
                }
                EditText(
                    label = { Text("Port") },
                    value = port,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        port = it
                        (it.trim().toIntOrNull() ?: (-1)).let {
                            if (dashboard.mqttData.port != it) {
                                dashboard.mqttData.port = it
                                dashboard.daemon.notifyOptionsChanged()
                            }
                        }
                    }
                )

                var id by remember { mutableStateOf(dashboard.mqttData.clientId) }
                EditText(
                    label = { Text("Unique client ID") },
                    value = id,
                    onValueChange = {
                        id = it
                        it.trim().let {
                            when {
                                it.isBlank() -> {
                                    dashboard.mqttData.clientId =
                                        abs(Random.nextInt()).toString()
                                    id = dashboard.mqttData.clientId
                                    dashboard.daemon.notifyOptionsChanged()
                                }
                                dashboard.mqttData.clientId != it -> {
                                    dashboard.mqttData.clientId = it
                                    dashboard.daemon.notifyOptionsChanged()
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
                                    BorderStroke(0.dp, colors.color),
                                    RoundedCornerShape(6.dp)
                                )
                                .background(colors.background.copy(.9f))
                                .padding(15.dp),
                        ) {
                            var enable by remember { mutableStateOf(dashboard.mqttData.ssl) }
                            var trust by remember { mutableStateOf(dashboard.mqttData.sslTrustAll) }

                            var ca by remember { mutableStateOf(dashboard.mqttData.caFileName) }
                            var client by remember { mutableStateOf(dashboard.mqttData.clientFileName) }
                            var key by remember { mutableStateOf(dashboard.mqttData.keyFileName) }

                            fun getCaCert() =
                                fragment.openFile { str, file ->
                                    dashboard.mqttData.let { m ->
                                        m.caCertStr = str
                                        if (m.caCert != null) {
                                            m.caFileName = file ?: "ca.crt"
                                            ca = dashboard.mqttData.caFileName
                                            dashboard.daemon.notifyOptionsChanged()
                                        } else createToast(
                                            fragment.requireContext(),
                                            "Certificate error"
                                        )
                                    }
                                }

                            fun getClientCert() =
                                fragment.openFile { str, file ->
                                    dashboard.mqttData.let { m ->
                                        m.clientCertStr = str
                                        if (m.clientCert != null) {
                                            m.clientFileName = file ?: "client.crt"
                                            client = dashboard.mqttData.clientFileName
                                            dashboard.daemon.notifyOptionsChanged()
                                        } else createToast(
                                            fragment.requireContext(),
                                            "Certificate error"
                                        )
                                    }
                                }

                            fun getClientKey() =
                                fragment.openFile { str, file ->
                                    dashboard.mqttData.let { m ->
                                        m.clientKeyStr = str
                                        if (m.clientKey != null) {
                                            m.keyFileName = file ?: "client.key"
                                            key = dashboard.mqttData.keyFileName
                                            dashboard.daemon.notifyOptionsChanged()
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
                                    dashboard.mqttData.ssl = it
                                    dashboard.daemon.notifyOptionsChanged()
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
                                                    trust = it
                                                    dashboard.mqttData.sslTrustAll = it
                                                    dashboard.daemon.notifyOptionsChanged()
                                                }
                                        } else {
                                            trust = it
                                            dashboard.mqttData.sslTrustAll = it
                                            dashboard.daemon.notifyOptionsChanged()
                                        }
                                    }
                                )

                                val alpha = rememberInfiniteTransition().animateFloat(
                                    initialValue = 1f,
                                    targetValue = 0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(300, 200),
                                        repeatMode = RepeatMode.Reverse,
                                    )
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
                                        if (dashboard.mqttData.caCert == null) getCaCert()
                                    }
                                    .padding(top = 10.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (dashboard.mqttData.caCert == null) getCaCert()
                                            else {
                                                ca = ""
                                                dashboard.mqttData.caFileName = ""
                                                dashboard.mqttData.caCertStr = null
                                                dashboard.daemon.notifyOptionsChanged()
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
                                        if (dashboard.mqttData.clientCert == null) getClientCert()
                                    }
                                    .padding(top = 5.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (dashboard.mqttData.clientCert == null) getClientCert()
                                            else {
                                                client = ""
                                                dashboard.mqttData.clientFileName = ""
                                                dashboard.mqttData.clientCertStr = null
                                                dashboard.daemon.notifyOptionsChanged()
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
                                        if (dashboard.mqttData.clientKey == null) getClientKey()
                                    }
                                    .padding(top = 5.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (dashboard.mqttData.clientKey == null) getClientKey()
                                            else {
                                                key = ""
                                                dashboard.mqttData.keyFileName = ""
                                                dashboard.mqttData.clientKeyStr = null
                                                dashboard.daemon.notifyOptionsChanged()
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

                            var passwordHidden by remember { mutableStateOf(dashboard.mqttData.clientKeyPassword.isNotEmpty()) }
                            var password by remember { mutableStateOf(if (passwordHidden) "hidden" else "") }
                            EditText(
                                label = { Text("Key password") },
                                modifier = Modifier.padding(top = 5.dp),
                                value = password,
                                textStyle = TextStyle(fontStyle = if (passwordHidden) Italic else Normal),
                                onValueChange = {
                                    if (passwordHidden) {
                                        password = ""
                                        passwordHidden = false
                                    } else password = it

                                    password.trim().let {
                                        if (dashboard.mqttData.clientKeyPassword != it) {
                                            dashboard.mqttData.clientKeyPassword = it
                                            dashboard.daemon.notifyOptionsChanged()
                                        }
                                    }
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        password = ""
                                        passwordHidden = false
                                        dashboard.mqttData.clientKeyPassword = ""
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

                var cred by remember { mutableStateOf(dashboard.mqttData.includeCred) }
                var show by remember { mutableStateOf(false) }

                val rotation = if (show) 0f else 180f
                val angle: Float by animateFloatAsState(
                    targetValue = if (rotation > 360 - rotation) {
                        -(360 - rotation)
                    } else rotation,
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
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
                            dashboard.mqttData.includeCred = it
                            dashboard.daemon.notifyOptionsChanged()
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
                        var userHidden by remember { mutableStateOf(dashboard.mqttData.username.isNotEmpty()) }
                        var user by remember { mutableStateOf(if (userHidden) "hidden" else "") }
                        EditText(
                            label = { Text("User name") },
                            value = user,
                            textStyle = TextStyle(fontStyle = if (userHidden) Italic else Normal),
                            onValueChange = {
                                if (userHidden) {
                                    user = ""
                                    userHidden = false
                                } else user = it

                                user.trim().let {
                                    if (dashboard.mqttData.username != it) {
                                        dashboard.mqttData.username = it
                                        dashboard.daemon.notifyOptionsChanged()
                                    }
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    user = ""
                                    userHidden = false
                                    dashboard.mqttData.username = ""
                                }) {
                                    Icon(
                                        painterResource(R.drawable.il_interface_multiply),
                                        "",
                                        tint = colors.b
                                    )
                                }
                            }
                        )

                        var passHidden by remember { mutableStateOf(dashboard.mqttData.pass.isNotEmpty()) }
                        var pass by remember { mutableStateOf(if (passHidden) "hidden" else "") }
                        EditText(
                            label = { Text("Password") },
                            value = pass,
                            textStyle = TextStyle(fontStyle = if (passHidden) Italic else Normal),
                            onValueChange = {
                                if (passHidden) {
                                    pass = ""
                                    passHidden = false
                                } else pass = it

                                pass.trim().let {
                                    if (dashboard.mqttData.pass != it) {
                                        dashboard.mqttData.pass = it
                                        dashboard.daemon.notifyOptionsChanged()
                                    }
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    pass = ""
                                    passHidden = false
                                    dashboard.mqttData.pass = ""
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