package com.alteratom.dashboard

import android.app.Dialog
import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.dashboard.Settings.Companion.saveToFile
import com.alteratom.dashboard.Theme.Companion.saveToFile
import com.alteratom.dashboard.activities.SplashScreenActivity
import com.alteratom.dashboard.Dashboard.Companion.saveToFile
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.databinding.DialogTransferBinding
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.random.Random

object Transfer {
    fun showTransferPopup(fragment: Fragment) {
        fragment.apply {

            val dialog = Dialog(requireContext())
            var observer: (Pair<String?, MqttMessage?>) -> Unit = {}

            dialog.setContentView(R.layout.dialog_transfer)
            val binding = DialogTransferBinding.bind(dialog.findViewById(R.id.root))

            var transferCaptured = false
            val connectionObserver: (Boolean) -> Unit = {
                dashboard.dg?.mqttd?.let {
                    if (!it.client.isConnected && !transferCaptured) {
                        dialog.dismiss()
                        createToast(requireContext(), "Connection required", 1000)
                    }
                }
            }

            dashboard.dg?.mqttd?.conHandler?.isDone?.observe(viewLifecycleOwner, connectionObserver)

            binding.dtReceive.setOnClickListener {

                fun receiveMode(enable: Boolean = binding.dtTransferBox.isVisible) {
                    binding.dtTransferTopic.isEnabled = !enable
                    if (!enable) {
                        dashboard.dg?.mqttd?.data?.removeObserver(observer)
                        dashboard.dg?.mqttd?.topicCheck()

                        binding.dtTransferBox.visibility = VISIBLE
                        binding.dtReceiveIcon.setBackgroundResource(R.drawable.il_arrow_import)
                        binding.dtReceiveFrame.clearAnimation()
                    } else {
                        binding.dtTransferBox.visibility = GONE
                        binding.dtReceiveIcon.setBackgroundResource(R.drawable.il_multimedia_pause)
                        binding.dtReceiveFrame.blink(-1, 200)

                        var ignoreFirst = false
                        observer = { data ->
                            if (data.first == binding.dtTransferTopic.text.toString() && ignoreFirst) {
                                dashboard.dg?.mqttd?.data?.removeObserver(observer)
                                transferCaptured = true

                                val save: List<String> = try {
                                    G.mapper.readerForListOf(String::class.java)
                                        .readValue(data.second.toString())
                                } catch (e: Exception) {
                                    listOf()
                                }

                                if (save.isNotEmpty()) {
                                    val d = Dashboard.parseSave(save[0])
                                    val s = Settings.parseSave(save[1])
                                    val t = Theme.parseSave(save[2])

                                    if (d != null) {
                                        for (dashboard in d) {
                                            dashboard.mqttClientId =
                                                kotlin.math.abs(Random.nextInt()).toString()
                                        }
                                    }

                                    if (d != null) dashboards = d
                                    if (s != null) settings = s
                                    if (t != null) theme = t

                                    dashboards.saveToFile()
                                    settings.saveToFile()
                                    theme.saveToFile()

                                    activity?.startActivity(
                                        Intent(
                                            context,
                                            SplashScreenActivity::class.java
                                        )
                                    )

                                    activity?.finish()
                                    activity?.finishAffinity()
                                } else {
                                    createToast(requireContext(), "Transfer failed")
                                }

                                receiveMode(false)
                            }

                            ignoreFirst = true
                        }

                        dashboard.dg?.mqttd?.data?.removeObserver(observer)
                        dashboard.dg?.mqttd?.data?.observe(viewLifecycleOwner, observer)
                        dashboard.dg?.mqttd?.subscribe(binding.dtTransferTopic.text.toString(), 0)
                    }
                }

                receiveMode()
            }

            binding.dtTransfer.setOnClickListener {
                val save =
                    arrayOf(
                        (if (binding.dtTransferAll.isChecked) dashboards else listOf(dashboard)).prepareSave(),
                        if (binding.dtTransferSettings.isChecked) settings.prepareSave() else "",
                        if (binding.dtTransferTheme.isChecked) theme.prepareSave() else ""
                    )

                val saveString = G.mapper.writeValueAsString(save)

                dashboard.dg?.mqttd?.publish(
                    binding.dtTransferTopic.text.toString(),
                    saveString,
                    2
                )
                createToast(requireContext(), "Transferred")
            }

            dialog.setOnDismissListener {
                dashboard.dg?.mqttd?.data?.removeObserver(observer)
                dashboard.dg?.mqttd?.conHandler?.isDone?.removeObserver(connectionObserver)
                dashboard.dg?.mqttd?.notifyOptionsChanged()
            }

            dialog.dialogSetup()
            theme.apply(binding.root)
            dialog.show()
        }
    }
}