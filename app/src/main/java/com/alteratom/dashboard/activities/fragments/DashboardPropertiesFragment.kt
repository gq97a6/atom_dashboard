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
import com.alteratom.dashboard.DialogBuilder.buildConfirm
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Transfer.showTransferPopup
import com.alteratom.dashboard.blink
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.foreground_service.demons.Mqttd
import com.alteratom.dashboard.recycler_view.GenericAdapter
import com.alteratom.dashboard.recycler_view.GenericItem
import com.alteratom.dashboard.switcher.FragmentSwitcher
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
        b.dpMqttCredBox.visibility = GONE

        b.dpMqttClientId.setText(dashboard.mqtt.clientId)
    }

    private fun switchMqttCred(state: Boolean? = null) {
        b.dpMqttCredBox.let {
            b.dpMqttCredArrow.animate()
                .rotation(if (state ?: it.isVisible) 180f else 0f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 250

            it.visibility = if (state ?: it.isVisible) GONE else VISIBLE
            b.dpMqttPass.requestFocus()
            b.dpMqttPass.clearFocus()
        }
    }
}