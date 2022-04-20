package com.alteratom.dashboard.activities.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.DashboardSwitcher
import com.alteratom.dashboard.DialogBuilder.buildConfirm
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Transfer.showTransferPopup
import com.alteratom.dashboard.blink
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.recycler_view.GenericAdapter
import com.alteratom.dashboard.recycler_view.GenericItem
import com.alteratom.dashboard.toPem
import com.alteratom.databinding.DialogCopyBrokerBinding
import com.alteratom.databinding.DialogSslBinding
import com.alteratom.databinding.FragmentDashboardPropertiesBinding
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import kotlin.random.Random

class DashboardPropertiesFragment : Fragment(R.layout.fragment_dashboard_properties) {
    private lateinit var b: FragmentDashboardPropertiesBinding
    private lateinit var openCert: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentDashboardPropertiesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        theme.apply(b.root, requireContext())
        viewConfig()

        dashboard.dg?.mqttd?.let {
            it.conHandler.isDone.observe(viewLifecycleOwner) { isDone ->
                val v = b.dpMqttStatus
                v.text = if (dashboard.mqtt.isEnabled) {
                    if (it.client.isConnected) {
                        v.clearAnimation()
                        "CONNECTED"
                    } else if (!isDone) {
                        if (v.animation == null) v.blink(-1, 400)
                        "ATTEMPTING"
                    } else {
                        v.clearAnimation()
                        "FAILED"
                    }
                } else {
                    v.clearAnimation()
                    "DISCONNECTED"
                }
            }
        }

        b.dpMqttSwitch.setOnCheckedChangeListener { _, state ->
            dashboard.mqtt.isEnabled = state
            dashboard.dg?.mqttd?.notifyOptionsChanged()
        }

        b.dpName.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                dashboard.name =
                    it.ifBlank { kotlin.math.abs(Random.nextInt()).toString() }
            }
        }

        b.dpMqttAddress.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (dashboard.mqtt.address != it) {
                    dashboard.mqtt.address = it
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttPort.addTextChangedListener {
            val port = (it ?: "").toString().trim().toIntOrNull() ?: (-1)
            if (dashboard.mqtt.port != port) {
                dashboard.mqtt.port = port
                dashboard.dg?.mqttd?.notifyOptionsChanged()
            }
        }

        b.dpMqttCred.setOnCheckedChangeListener { _, state ->
            dashboard.mqtt.includeCred = state
            dashboard.dg?.mqttd?.notifyOptionsChanged()
            switchMqttCred(!state)
        }

        b.dpMqttCredArrow.setOnClickListener {
            switchMqttCred()
        }

        b.dpMqttLogin.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (dashboard.mqtt.username != it) {
                    dashboard.mqtt.username = it
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttPass.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (dashboard.mqtt.pass != it) {
                    dashboard.mqtt.pass = it
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttClientId.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                when {
                    it.isBlank() -> {
                        dashboard.mqtt.clientId = kotlin.math.abs(Random.nextInt()).toString()
                        b.dpMqttClientId.setText(dashboard.mqtt.clientId)
                        dashboard.dg?.mqttd?.notifyOptionsChanged()
                    }
                    dashboard.mqtt.clientId != it -> {
                        dashboard.mqtt.clientId = it
                        dashboard.dg?.mqttd?.notifyOptionsChanged()
                    }
                }
            }
        }

        b.dpMqttCopy.setOnClickListener {
            if (dashboards.size <= 1) {
                createToast(requireContext(), "No dashboards to copy from")
            } else {
                val dialog = Dialog(requireContext())
                val adapter = GenericAdapter(requireContext())

                val list = MutableList(dashboards.size) {
                    GenericItem(
                        R.layout.item_copy_broker
                    )
                }
                list.removeAt(dashboards.indexOf(dashboard))

                dialog.setContentView(R.layout.dialog_copy_broker)
                val binding = DialogCopyBrokerBinding.bind(dialog.findViewById(R.id.root))

                adapter.setHasStableIds(true)
                adapter.onBindViewHolder = { _, holder, pos ->
                    val p = pos + if (pos >= dashboards.indexOf(dashboard)) 1 else 0
                    val text = holder.itemView.findViewById<TextView>(R.id.icb_text)
                    text.text = dashboards[p].name.uppercase(Locale.getDefault())
                }

                adapter.onItemClick = {
                    val pos = adapter.list.indexOf(it)
                    val p = pos + if (pos >= dashboards.indexOf(dashboard)) 1 else 0

                    dashboard.mqtt.address = dashboards[p].mqtt.address
                    dashboard.mqtt.port = dashboards[p].mqtt.port
                    dashboard.mqtt.username = dashboards[p].mqtt.username
                    dashboard.mqtt.pass = dashboards[p].mqtt.pass

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
            if (dashboard.dg?.mqttd?.client?.isConnected == true) showTransferPopup(this)
            else createToast(requireContext(), "Connection required", 1000)
        }

        b.dpRoot.onInterceptTouch = { e ->
            DashboardSwitcher.handle(e, requireActivity(), DashboardPropertiesFragment())
        }

        var onOpenCertSuccess: () -> Unit = {}

        openCert =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.also { uri ->
                        try {
                            requireContext().contentResolver.openInputStream(uri)
                                ?.use { inputStream ->
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

            binding.dsSsl.isChecked = dashboard.mqtt.ssl
            binding.dsTrustAll.isChecked = dashboard.mqtt.sslTrustAll
            binding.dsCaCert.setText(dashboard.mqtt.sslFileName)
            binding.dsTrustAlert.visibility = if (dashboard.mqtt.sslTrustAll) VISIBLE else GONE
            if (dashboard.mqtt.sslTrustAll) binding.dsTrustAlert.blink(-1, 400, 300)

            if (dashboard.mqtt.sslCert != null) binding.dsCaCertInsert.foreground =
                requireContext().getDrawable(R.drawable.button_remove)

            binding.dsSsl.setOnCheckedChangeListener { _, state ->
                dashboard.mqtt.ssl = state
                dashboard.dg?.mqttd?.notifyOptionsChanged()
            }

            binding.dsTrustAll.setOnTouchListener { v, event ->
                if (event.action != 0) return@setOnTouchListener true

                fun validate() {
                    binding.dsTrustAll.isChecked = dashboard.mqtt.sslTrustAll
                    binding.dsTrustAlert.visibility =
                        if (dashboard.mqtt.sslTrustAll) VISIBLE else GONE

                    if (dashboard.mqtt.sslTrustAll) binding.dsTrustAlert.blink(-1, 400, 300)
                    else binding.dsTrustAlert.clearAnimation()

                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }

                if (!dashboard.mqtt.sslTrustAll) {
                    requireContext().buildConfirm("Confirm override", "CONFIRM",
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

            onOpenCertSuccess = {
                binding.dsCaCert.setText(dashboard.mqtt.sslFileName)
                binding.dsCaCertInsert.foreground =
                    requireContext().getDrawable(R.drawable.button_remove)

                dashboard.dg?.mqttd?.notifyOptionsChanged()
            }

            fun openCert() {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                openCert.launch(intent)
            }

            binding.dsCaCert.setOnClickListener {
                if (dashboard.mqtt.sslCert == null) openCert()
            }

            binding.dsCaCertInsert.setOnClickListener {
                if (dashboard.mqtt.sslCert == null) openCert()
                else {
                    dashboard.mqtt.sslFileName = ""
                    dashboard.mqtt.sslCertStr = null

                    binding.dsCaCert.setText("")
                    binding.dsCaCertInsert.foreground =
                        requireContext().getDrawable(R.drawable.button_include)

                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }

            dialog.dialogSetup()
            theme.apply(binding.root)
            dialog.show()
        }
    }

    private fun viewConfig() {
        b.dpName.setText(dashboard.name.lowercase(Locale.getDefault()))

        b.dpMqttSwitch.isChecked = dashboard.mqtt.isEnabled

        b.dpMqttAddress.setText(dashboard.mqtt.address)
        dashboard.mqtt.port.let {
            b.dpMqttPort.setText(if (it != -1) it.toString() else "")
        }

        b.dpMqttCred.isChecked = dashboard.mqtt.includeCred
        b.dpMqttLogin.setText(dashboard.mqtt.username)
        b.dpMqttPass.setText(dashboard.mqtt.pass)

        b.dpMqttCredArrow.rotation = 180f
        b.dpMqttCredBox.visibility = View.GONE

        b.dpMqttClientId.setText(dashboard.mqtt.clientId)
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