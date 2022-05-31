package com.alteratom.dashboard.activities.fragments.dashboard_properties

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesFragment
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.databinding.DialogCopyBrokerBinding
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

class DashboardPropertiesFragment : Fragment() {

    var onOpenCertSuccess: () -> Unit = {}
    lateinit var openCert: ActivityResultLauncher<Intent>

    val copyProperties: () -> Unit = {
        if (G.dashboards.size <= 1) {
            createToast(requireContext(), "No dashboards to copy from")
        } else {
            val dialog = Dialog(requireContext())
            val adapter = RecyclerViewAdapter<RecyclerViewItem>(requireContext())

            val list = MutableList(G.dashboards.size) {
                RecyclerViewItem(
                    R.layout.item_copy_broker
                )
            }
            list.removeAt(G.dashboards.indexOf(G.dashboard))

            dialog.setContentView(R.layout.dialog_copy_broker)
            val binding = DialogCopyBrokerBinding.bind(dialog.findViewById(R.id.root))

            adapter.setHasStableIds(true)
            adapter.onBindViewHolder = { _, holder, pos ->
                val p = pos + if (pos >= G.dashboards.indexOf(G.dashboard)) 1 else 0
                val text = holder.itemView.findViewById<TextView>(R.id.icb_text)
                text.text = G.dashboards[p].name.uppercase(Locale.getDefault())
            }

            adapter.onItemClick = {
                val pos = adapter.list.indexOf(it)
                val p = pos + if (pos >= G.dashboards.indexOf(G.dashboard)) 1 else 0

                G.dashboard.mqtt = G.dashboards[p].mqtt.copy()
                G.dashboard.mqtt.clientId = abs(Random.nextInt()).toString()
                G.dashboard.daemon.notifyOptionsChanged()

                fm.replaceWith(DashboardPropertiesFragment(), false)
                dialog.dismiss()
            }

            binding.dcbRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.dcbRecyclerView.adapter = adapter

            adapter.submitList(list)

            dialog.dialogSetup()
            G.theme.apply(binding.root)
            dialog.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        G.theme.apply(context = requireContext())

        openCert =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.also { uri ->
                        try {
                            requireContext().contentResolver.openInputStream(uri)
                                ?.use { inputStream ->
                                    G.dashboard.mqtt.let { m ->

                                        m.sslCertStr = try {
                                            val cf = CertificateFactory.getInstance("X.509")
                                            cf.generateCertificate(inputStream) as X509Certificate
                                        } catch (e: Exception) {
                                            null
                                        }?.toPem()

                                        m.sslFileName =
                                            if (m.sslCert != null) {
                                                runCatching {
                                                    requireContext().contentResolver.query(
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

                                        if (m.sslCert != null) onOpenCertSuccess()
                                    }
                                }

                        } catch (e: java.lang.Exception) {
                            createToast(requireContext(), "Certificate error")
                        }
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                //Background
                Box(modifier = Modifier.background(Theme.colors.background))

                ComposeTheme(Theme.isDark) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(modifier = Modifier.padding(16.dp)) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(
                                    text = "Dashboard",
                                    fontSize = 45.sp,
                                    color = Theme.colors.color

                                )
                                Text(
                                    modifier = Modifier.offset(y = -10.dp),
                                    text = "properties",
                                    fontSize = 35.sp,
                                    color = Theme.colors.a

                                )

                                var name by remember {
                                    mutableStateOf(G.dashboard.name.lowercase(Locale.getDefault()))
                                }
                                EditText(
                                    label = { Text("Dashboard name") },
                                    value = name,
                                    onValueChange = {
                                        name = it

                                        it.trim().let {
                                            G.dashboard.name =
                                                it.ifBlank {
                                                    abs(Random.nextInt(0, 100)).toString()
                                                }
                                        }
                                    }
                                )

                                DashboardPropertiesCompose.compose(
                                    G.dashboard.type,
                                    this@DashboardPropertiesFragment
                                )
                            }
                        }
                    }

                    if(!settings.hideNav) {
                        NavigationArrows(
                            { FragmentSwitcher.switch(true, DashboardPropertiesFragment()) },
                            { FragmentSwitcher.switch(false, DashboardPropertiesFragment()) }
                        )
                    }
                }
            }
        }
    }

    /*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Set dashboard status
        G.dashboard.daemon.let {
            it.isDone.observe(viewLifecycleOwner) { isDone ->
                val v = b.dpMqttStatus
                v.text = when (it) {
                    is Mqttd -> when (it.status) {
                        Mqttd.MqttdStatus.DISCONNECTED -> {
                            v.clearAnimation()
                            "DISCONNECTED"
                        }
                        Mqttd.MqttdStatus.FAILED -> {
                            v.clearAnimation()
                            "FAILED TO CONNECT"
                        }
                        Mqttd.MqttdStatus.ATTEMPTING -> {
                            if (v.animation == null) v.blink(-1, 400)
                            "ATTEMPTING"
                        }
                        Mqttd.MqttdStatus.CONNECTED -> {
                            v.clearAnimation()
                            "CONNECTED"
                        }
                        Mqttd.MqttdStatus.CONNECTED_SSL -> {
                            v.clearAnimation()
                            "CONNECTED"
                        }
                    }
                    else -> {
                        "err"
                    }
                }
            }
        }


    }
     */
}