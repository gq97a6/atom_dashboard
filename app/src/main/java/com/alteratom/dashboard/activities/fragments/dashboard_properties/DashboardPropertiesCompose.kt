package com.alteratom.dashboard.activities.fragments.dashboard_properties

import android.provider.OpenableColumns
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
import androidx.compose.material.OutlinedButton
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
import com.alteratom.dashboard.*
import com.alteratom.dashboard.DialogBuilder.buildConfirm
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose
import com.alteratom.dashboard.foreground_service.demons.Mqttd
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

object DashboardPropertiesCompose : DaemonBasedCompose {

    @Composable
    override fun Mqttd(fragment: Fragment) {
        fragment as DashboardPropertiesFragment

        FrameBox("Communication:", "MQTT") {
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
                                color = Theme.colors.a
                            )
                        },
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            dashboard.mqtt.isEnabled = it
                            dashboard.daemon.notifyOptionsChanged()
                        }
                    )

                    var conStatus by remember { mutableStateOf("") }

                    dashboard.daemon.let {
                        it.isDone.observe(fragment.viewLifecycleOwner) { _ ->
                            when (it) {
                                is Mqttd -> {
                                    conStatus = when (it.status) {
                                        Mqttd.MqttdStatus.DISCONNECTED -> "DISCONNECTED"
                                        Mqttd.MqttdStatus.FAILED -> "FAILED"
                                        Mqttd.MqttdStatus.ATTEMPTING -> "ATTEMPTING"
                                        Mqttd.MqttdStatus.CONNECTED -> "CONNECTED"
                                        Mqttd.MqttdStatus.CONNECTED_SSL -> "CONNECTED"
                                    }
                                }
                            }
                        }
                    }

                    val value = rememberInfiniteTransition()
                    val alpha = value.animateFloat(
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
                                BorderStroke(2.dp, Theme.colors.a),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Text(
                            conStatus,
                            modifier = Modifier.align(Alignment.Center),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Theme.colors.a
                        )
                    }
                }

                var copyShow by remember { mutableStateOf(false) }

                OutlinedButton(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(.8f),
                    contentPadding = PaddingValues(13.dp),
                    border = BorderStroke(2.dp, Theme.colors.b),
                    onClick = {
                        if (G.dashboards.size <= 1)
                            createToast(fragment.requireContext(), "No dashboards to copy from")
                        else copyShow = true
                    }
                ) {
                    Text("COPY PROPERTIES", fontSize = 10.sp, color = Theme.colors.a)
                }

                if (copyShow) {
                    Dialog({ copyShow = false }) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    BorderStroke(0.dp, Theme.colors.color),
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
                                            .border(
                                                BorderStroke(0.dp, colors.color),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                dashboard.mqtt = it.mqtt.copy()
                                                dashboard.mqtt.clientId =
                                                    abs(Random.nextInt()).toString()
                                                dashboard.daemon.notifyOptionsChanged()

                                                copyShow = false
                                                MainActivity.fm.replaceWith(
                                                    DashboardPropertiesFragment(),
                                                    false
                                                )
                                            }
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
                    onValueChange = {
                        address = it
                        it.trim().let {
                            if (dashboard.mqtt.address != it) {
                                dashboard.mqtt.address = it
                                dashboard.daemon.notifyOptionsChanged()
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
                    onValueChange = {
                        port = it
                        (it.trim().toIntOrNull() ?: (-1)).let {
                            if (dashboard.mqtt.port != it) {
                                dashboard.mqtt.port = it
                                dashboard.daemon.notifyOptionsChanged()
                            }
                        }
                    }
                )

                var id by remember { mutableStateOf(dashboard.mqtt.clientId) }
                EditText(
                    label = { Text("Unique client ID") },
                    value = id,
                    onValueChange = {
                        id = it
                        it.trim().let {
                            when {
                                it.isBlank() -> {
                                    dashboard.mqtt.clientId =
                                        kotlin.math.abs(Random.nextInt()).toString()
                                    id = dashboard.mqtt.clientId
                                    dashboard.daemon.notifyOptionsChanged()
                                }
                                dashboard.mqtt.clientId != it -> {
                                    dashboard.mqtt.clientId = it
                                    dashboard.daemon.notifyOptionsChanged()
                                }
                            }
                        }
                    }
                )

                var sshShow by remember { mutableStateOf(false) }

                OutlinedButton(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(.8f)
                        .padding(top = 10.dp),
                    contentPadding = PaddingValues(13.dp),
                    border = BorderStroke(2.dp, Theme.colors.b),
                    onClick = { sshShow = true } //editSsl(fragment) }
                ) {
                    Text("CONFIGURE SSL", fontSize = 10.sp, color = Theme.colors.a)
                }

                if (sshShow) {
                    Dialog({ sshShow = false }) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    BorderStroke(0.dp, Theme.colors.color),
                                    RoundedCornerShape(6.dp)
                                )
                                .background(colors.background.copy(.9f))
                                .padding(15.dp),
                        ) {
                            var enable by remember { mutableStateOf(dashboard.mqtt.ssl) }
                            var trust by remember { mutableStateOf(dashboard.mqtt.sslTrustAll) }

                            var ca by remember { mutableStateOf(dashboard.mqtt.sslFileName) }
                            val client by remember { mutableStateOf("") }

                            fun getCaCert() {
                                fragment.openCert { uri, inputStream ->
                                    dashboard.mqtt.let { m ->

                                        m.sslCertStr = try {
                                            val cf = CertificateFactory.getInstance("X.509")
                                            cf.generateCertificate(inputStream) as X509Certificate
                                        } catch (e: Exception) {
                                            null
                                        }?.toPem()

                                        m.sslFileName =
                                            if (m.sslCert != null) {
                                                runCatching {
                                                    fragment.requireContext().contentResolver.query(
                                                        uri,
                                                        null,
                                                        null,
                                                        null,
                                                        null
                                                    )?.use { cursor ->
                                                        cursor.moveToFirst()
                                                        return@use cursor.getColumnIndexOrThrow(
                                                            OpenableColumns.DISPLAY_NAME
                                                        ).let(cursor::getString)
                                                    }
                                                }.getOrNull() ?: "cert.crt"
                                            } else ""

                                        if (m.sslCert != null) {
                                            ca = dashboard.mqtt.sslFileName
                                            dashboard.daemon.notifyOptionsChanged()
                                        }
                                    }
                                }
                            }

                            LabeledCheckbox(
                                label = {
                                    Text(
                                        "Enable SSL",
                                        fontSize = 15.sp,
                                        color = Theme.colors.a
                                    )
                                },
                                checked = enable,
                                onCheckedChange = {
                                    enable = it
                                    dashboard.mqtt.ssl = it
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
                                            color = Theme.colors.a
                                        )
                                    },
                                    checked = trust,
                                    onCheckedChange = {
                                        if (it) {
                                            fragment.requireContext().buildConfirm("Confirm override", "CONFIRM",
                                                {
                                                    trust = it
                                                    dashboard.mqtt.sslTrustAll = it
                                                    dashboard.daemon.notifyOptionsChanged()
                                                }
                                            )
                                        } else {
                                            trust = it
                                            dashboard.mqtt.sslTrustAll = it
                                            dashboard.daemon.notifyOptionsChanged()
                                        }
                                    }
                                )

                                val value = rememberInfiniteTransition()
                                val alpha = value.animateFloat(
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
                                        //.fillMaxHeight(1f)
                                        //.aspectRatio(1f)
                                )
                            }

                            EditText(
                                enabled = false,
                                label = { Text("Custom CA certificate") },
                                value = ca,
                                onValueChange = {},
                                modifier = Modifier
                                    .nrClickable {
                                        if (dashboard.mqtt.sslCert == null) getCaCert()
                                    }
                                    .padding(top = 10.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (dashboard.mqtt.sslCert == null) getCaCert()
                                            else {
                                                ca = ""
                                                dashboard.mqtt.sslFileName = ""
                                                dashboard.mqtt.sslCertStr = null
                                                dashboard.daemon.notifyOptionsChanged()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painterResource(if (ca.isEmpty()) R.drawable.il_interface_paperclip else R.drawable.il_interface_multiply),
                                            "",
                                            tint = Theme.colors.b
                                        )
                                    }
                                }
                            )

                            EditText(
                                enabled = false,
                                label = { Text("Client certificate") },
                                value = client,
                                onValueChange = {},
                                modifier = Modifier.nrClickable {
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                        }
                                    ) {
                                        Icon(
                                            painterResource(if (client.isEmpty()) R.drawable.il_interface_paperclip else R.drawable.il_interface_multiply),
                                            "",
                                            tint = Theme.colors.b
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
                                color = Theme.colors.a
                            )
                        },
                        checked = cred,
                        onCheckedChange = {
                            cred = it
                            show = it
                            dashboard.mqtt.includeCred = it
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
                            tint = Theme.colors.a,
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
                            onValueChange = {
                                if (userHidden) {
                                    user = ""
                                    userHidden = false
                                } else user = it

                                user.trim().let {
                                    if (dashboard.mqtt.username != it) {
                                        dashboard.mqtt.username = it
                                        dashboard.daemon.notifyOptionsChanged()
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
                                        tint = Theme.colors.b
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
                            onValueChange = {
                                if (passHidden) {
                                    pass = ""
                                    passHidden = false
                                } else pass = it

                                pass.trim().let {
                                    if (dashboard.mqtt.pass != it) {
                                        dashboard.mqtt.pass = it
                                        dashboard.daemon.notifyOptionsChanged()
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
                                        tint = Theme.colors.b
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