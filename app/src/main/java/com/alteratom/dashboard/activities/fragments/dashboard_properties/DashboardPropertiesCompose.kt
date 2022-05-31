package com.alteratom.dashboard.activities.fragments.dashboard_properties

import android.app.Dialog
import android.content.Intent
import android.view.View
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontStyle.Companion.Normal
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.DialogBuilder.buildConfirm
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose
import com.alteratom.dashboard.foreground_service.demons.Mqttd
import com.alteratom.databinding.DialogSslBinding
import kotlin.random.Random

object DashboardPropertiesCompose : DaemonBasedCompose {

    fun editSsl(fragment: Fragment) {

        val context = fragment.context
        if (context == null) return

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.dialog_ssl)
        val binding = DialogSslBinding.bind(dialog.findViewById(R.id.root))

        binding.dsSsl.isChecked = dashboard.mqtt.ssl
        binding.dsTrustAll.isChecked = dashboard.mqtt.sslTrustAll
        binding.dsCaCert.text = dashboard.mqtt.sslFileName
        binding.dsTrustAlert.visibility =
            if (dashboard.mqtt.sslTrustAll) View.VISIBLE else View.GONE
        if (dashboard.mqtt.sslTrustAll) binding.dsTrustAlert.blink(-1, 400, 300)

        if (dashboard.mqtt.sslCert != null) binding.dsCaCertInsert.foreground =
            context.getDrawable(R.drawable.bt_remove)

        binding.dsSsl.setOnCheckedChangeListener { _, state ->
            dashboard.mqtt.ssl = state
            dashboard.daemon.notifyOptionsChanged()
        }

        binding.dsTrustAll.setOnTouchListener { v, event ->
            if (event.action != 0) return@setOnTouchListener true

            fun validate() {
                binding.dsTrustAll.isChecked = dashboard.mqtt.sslTrustAll
                binding.dsTrustAlert.visibility =
                    if (dashboard.mqtt.sslTrustAll) View.VISIBLE else View.GONE

                if (dashboard.mqtt.sslTrustAll) binding.dsTrustAlert.blink(-1, 400, 300)
                else binding.dsTrustAlert.clearAnimation()

                dashboard.daemon.notifyOptionsChanged()
            }

            if (!dashboard.mqtt.sslTrustAll) {
                context.buildConfirm("Confirm override", "CONFIRM",
                    {
                        dashboard.mqtt.sslTrustAll = true
                        validate()
                    }
                )
            } else {
                dashboard.mqtt.sslTrustAll = false
                validate()
            }

            return@setOnTouchListener true
        }

        (fragment as DashboardPropertiesFragment).onOpenCertSuccess = {
            binding.dsCaCert.text = dashboard.mqtt.sslFileName
            binding.dsCaCertInsert.foreground =
                context.getDrawable(R.drawable.bt_remove)

            dashboard.daemon.notifyOptionsChanged()
        }

        fun openCert() {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            fragment.openCert.launch(intent)
        }

        binding.dsCaCert.setOnClickListener {
            if (dashboard.mqtt.sslCert == null) openCert()
        }

        binding.dsCaCertInsert.setOnClickListener {
            if (dashboard.mqtt.sslCert == null) openCert()
            else {
                dashboard.mqtt.sslFileName = ""
                dashboard.mqtt.sslCertStr = null

                binding.dsCaCert.text = ""
                binding.dsCaCertInsert.foreground =
                    context.getDrawable(R.drawable.bt_include)

                dashboard.daemon.notifyOptionsChanged()
            }
        }

        dialog.dialogSetup()
        G.theme.apply(binding.root)
        dialog.show()
    }

    @Composable
    override fun Mqttd(fragment: Fragment) {
        FrameBox("Communication:", "MQTT") {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    Arrangement.SpaceBetween
                ) {
                    var enabled by remember { mutableStateOf(dashboard.mqtt.isEnabled) }
                    LabeledSwitch(
                        modifier = Modifier.align(Alignment.CenterVertically),
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
                        it.isDone.observe(fragment.viewLifecycleOwner) { isDone ->
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
                            .align(Alignment.CenterVertically)
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

                OutlinedButton(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(.8f),
                    contentPadding = PaddingValues(13.dp),
                    border = BorderStroke(2.dp, Theme.colors.b),
                    onClick = { (fragment as DashboardPropertiesFragment).copyProperties() }
                ) {
                    Text("COPY PROPERTIES", fontSize = 10.sp, color = Theme.colors.a)
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

                OutlinedButton(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(.8f)
                        .padding(top = 10.dp),
                    contentPadding = PaddingValues(13.dp),
                    border = BorderStroke(2.dp, Theme.colors.b),
                    onClick = { editSsl(fragment) }
                ) {
                    Text("CONFIGURE SSL", fontSize = 10.sp, color = Theme.colors.a)
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
                        var userHidden by remember { mutableStateOf(!dashboard.mqtt.username.isEmpty()) }
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

                        var passHidden by remember { mutableStateOf(!dashboard.mqtt.pass.isEmpty()) }
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