package com.alteratom.dashboard.activities.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.DialogBuilder.buildConfirm
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.foreground_service.demons.Mqttd
import com.alteratom.dashboard.recycler_view.GenericAdapter
import com.alteratom.dashboard.recycler_view.GenericItem
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.databinding.DialogCopyBrokerBinding
import com.alteratom.databinding.DialogSslBinding
import com.alteratom.databinding.FragmentDaemonPropertiesBinding
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import kotlin.random.Random

class DaemonPropertiesFragment : Fragment(R.layout.fragment_daemon_properties) {
    private lateinit var b: FragmentDaemonPropertiesBinding
    private lateinit var openCert: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentDaemonPropertiesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        theme.apply(b.root, requireContext())

        G.dashboard.daemon?.let {
            it.isDone.observe(viewLifecycleOwner) { isDone ->
                val v = b.dpMqttStatus
                when (it) {
                    is Mqttd -> {
                        v.text = when (it.status) {
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
                    }
                }
            }
        }

        b.dpMqttSwitch.setOnCheckedChangeListener { _, state ->
            G.dashboard.mqtt.isEnabled = state
            G.dashboard.dg.mqttd?.notifyOptionsChanged()
        }

        b.dpName.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                G.dashboard.name =
                    it.ifBlank { kotlin.math.abs(Random.nextInt()).toString() }
            }
        }

        b.dpMqttAddress.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (G.dashboard.mqtt.address != it) {
                    G.dashboard.mqtt.address = it
                    G.dashboard.dg.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttPort.addTextChangedListener {
            val port = (it ?: "").toString().trim().toIntOrNull() ?: (-1)
            if (G.dashboard.mqtt.port != port) {
                G.dashboard.mqtt.port = port
                G.dashboard.dg.mqttd?.notifyOptionsChanged()
            }
        }

        b.dpMqttCred.setOnCheckedChangeListener { _, state ->
            G.dashboard.mqtt.includeCred = state
            G.dashboard.dg.mqttd?.notifyOptionsChanged()
            switchMqttCred(!state)
        }

        b.dpMqttCredArrow.setOnClickListener {
            switchMqttCred()
        }

        b.dpMqttLogin.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (G.dashboard.mqtt.username != it) {
                    G.dashboard.mqtt.username = it
                    G.dashboard.dg.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttPass.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (G.dashboard.mqtt.pass != it) {
                    G.dashboard.mqtt.pass = it
                    G.dashboard.dg.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttClientId.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                when {
                    it.isBlank() -> {
                        G.dashboard.mqtt.clientId = kotlin.math.abs(Random.nextInt()).toString()
                        b.dpMqttClientId.setText(G.dashboard.mqtt.clientId)
                        G.dashboard.dg.mqttd?.notifyOptionsChanged()
                    }
                    G.dashboard.mqtt.clientId != it -> {
                        G.dashboard.mqtt.clientId = it
                        G.dashboard.dg.mqttd?.notifyOptionsChanged()
                    }
                }
            }
        }

        b.dpMqttCopy.setOnClickListener {
            if (G.dashboards.size <= 1) {
                createToast(requireContext(), "No dashboards to copy from")
            } else {
                val dialog = Dialog(requireContext())
                val adapter = GenericAdapter(requireContext())

                val list = MutableList(G.dashboards.size) {
                    GenericItem(
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
                    G.dashboard.mqtt.clientId = kotlin.math.abs(Random.nextInt()).toString()
                    G.dashboard.dg.mqttd?.notifyOptionsChanged()

                    viewConfig()
                    dialog.dismiss()
                }

                binding.dcbRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.dcbRecyclerView.adapter = adapter

                adapter.submitList(list)

                dialog.dialogSetup()
                theme.apply(binding.root)
                dialog.show()
            }
        }

        b.dpTransfer.setOnClickListener {
            if (G.dashboard.dg.mqttd?.client?.isConnected == true) Transfer.showTransferPopup(this)
            else createToast(requireContext(), "Connection required", 1000)
        }

        b.dpLeft.setOnClickListener {
            FragmentSwitcher.switch(true, DashboardPropertiesFragment())
        }

        b.dpRight.setOnClickListener {
            FragmentSwitcher.switch(false, DashboardPropertiesFragment())
        }

        b.dpRoot.onInterceptTouch = { e ->
            FragmentSwitcher.handle(e, DashboardPropertiesFragment())
        }

        var onOpenCertSuccess: () -> Unit = {}

        openCert =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
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



        b.dpMqttSsl.setOnClickListener {
            val dialog = Dialog(requireContext())

            dialog.setContentView(R.layout.dialog_ssl)
            val binding = DialogSslBinding.bind(dialog.findViewById(R.id.root))

            binding.dsSsl.isChecked = G.dashboard.mqtt.ssl
            binding.dsTrustAll.isChecked = G.dashboard.mqtt.sslTrustAll
            binding.dsCaCert.text = G.dashboard.mqtt.sslFileName
            binding.dsTrustAlert.visibility = if (G.dashboard.mqtt.sslTrustAll) View.VISIBLE else View.GONE
            if (G.dashboard.mqtt.sslTrustAll) binding.dsTrustAlert.blink(-1, 400, 300)

            if (G.dashboard.mqtt.sslCert != null) binding.dsCaCertInsert.foreground =
                requireContext().getDrawable(R.drawable.bt_remove)

            binding.dsSsl.setOnCheckedChangeListener { _, state ->
                G.dashboard.mqtt.ssl = state
                G.dashboard.dg.mqttd?.notifyOptionsChanged()
            }

            binding.dsTrustAll.setOnTouchListener { v, event ->
                if (event.action != 0) return@setOnTouchListener true

                fun validate() {
                    binding.dsTrustAll.isChecked = G.dashboard.mqtt.sslTrustAll
                    binding.dsTrustAlert.visibility =
                        if (G.dashboard.mqtt.sslTrustAll) View.VISIBLE else View.GONE

                    if (G.dashboard.mqtt.sslTrustAll) binding.dsTrustAlert.blink(-1, 400, 300)
                    else binding.dsTrustAlert.clearAnimation()

                    G.dashboard.dg.mqttd?.notifyOptionsChanged()
                }

                if (!G.dashboard.mqtt.sslTrustAll) {
                    requireContext().buildConfirm("Confirm override", "CONFIRM",
                        {
                            G.dashboard.mqtt.sslTrustAll = true
                            validate()
                        }
                    )
                } else {
                    G.dashboard.mqtt.sslTrustAll = false
                    validate()
                }

                return@setOnTouchListener true
            }

            onOpenCertSuccess = {
                binding.dsCaCert.text = G.dashboard.mqtt.sslFileName
                binding.dsCaCertInsert.foreground =
                    requireContext().getDrawable(R.drawable.bt_remove)

                G.dashboard.dg.mqttd?.notifyOptionsChanged()
            }

            fun openCert() {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                openCert.launch(intent)
            }

            binding.dsCaCert.setOnClickListener {
                if (G.dashboard.mqtt.sslCert == null) openCert()
            }

            binding.dsCaCertInsert.setOnClickListener {
                if (G.dashboard.mqtt.sslCert == null) openCert()
                else {
                    G.dashboard.mqtt.sslFileName = ""
                    G.dashboard.mqtt.sslCertStr = null

                    binding.dsCaCert.text = ""
                    binding.dsCaCertInsert.foreground =
                        requireContext().getDrawable(R.drawable.bt_include)

                    G.dashboard.dg.mqttd?.notifyOptionsChanged()
                }
            }

            dialog.dialogSetup()
            theme.apply(binding.root)
            dialog.show()
        }
    }

    private fun viewConfig() {
        b.dpName.setText(G.dashboard.name.lowercase(Locale.getDefault()))

        b.dpMqttSwitch.isChecked = G.dashboard.mqtt.isEnabled

        b.dpMqttAddress.setText(G.dashboard.mqtt.address)
        G.dashboard.mqtt.port.let {
            b.dpMqttPort.setText(if (it != -1) it.toString() else "")
        }

        b.dpMqttCred.isChecked = G.dashboard.mqtt.includeCred
        b.dpMqttLogin.setText(G.dashboard.mqtt.username)
        b.dpMqttPass.setText(G.dashboard.mqtt.pass)

        b.dpMqttCredArrow.rotation = 180f
        b.dpMqttCredBox.visibility = View.GONE

        b.dpMqttClientId.setText(G.dashboard.mqtt.clientId)
    }

    private fun switchMqttCred(state: Boolean? = null) {
        b.dpMqttCredBox.let {
            b.dpMqttCredArrow.animate()
                .rotation(if (state ?: it.isVisible) 180f else 0f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 250

            it.visibility = if (state ?: it.isVisible) View.GONE else View.VISIBLE
            b.dpMqttPass.requestFocus()
            b.dpMqttPass.clearFocus()
        }
    }
}