package com.alteratom.dashboard

import android.widget.TextView
import com.alteratom.R
import com.alteratom.dashboard.foreground_service.DaemonGroup
import com.alteratom.dashboard.log.Log
import com.alteratom.dashboard.tile.Tile
import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.File
import java.io.FileReader
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import kotlin.random.Random

@Suppress("UNUSED")
class Dashboard(var name: String = "", var isInvalid: Boolean = false) :
    com.alteratom.dashboard.recycler_view.RecyclerViewItem() {

    override val layout
        get() = R.layout.item_dashboard

    var log = Log()

    @JsonIgnore
    var dg: DaemonGroup? = null

    var tiles: MutableList<Tile> = mutableListOf()
        set(value) {
            for (t in value) t.dashboard = this
            field = value
        }

    var mqtt = MqttData()
       // set(value) {
       //     field = value
       //     value.initSSLCert()
       // }

    companion object {
        fun MutableList<Dashboard>.saveToFile(save: String = this.prepareSave()) {
            try {
                File(FolderTree.dashboardsFile).writeText(save)
            } catch (e: Exception) {
                run { }
            }
        }

        private fun getSaveFromFile() = try {
            FileReader(FolderTree.dashboardsFile).readText()
        } catch (e: Exception) {
            ""
        }

        fun parseSave(save: String = getSaveFromFile()): MutableList<Dashboard>? =
            try {
                G.mapper.readerForListOf(Dashboard::class.java).readValue(save)
            } catch (e: Exception) {
                null
            }
    }

    override fun onBindViewHolder(
        holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = ((screenWidth - view.paddingLeft * 2) * 1 / 3.236).toInt()
        view.layoutParams = params

        holder.itemView.findViewById<TextView>(R.id.id_tag).text =
            name.uppercase(Locale.getDefault())
    }

    data class MqttData(
        var isEnabled: Boolean = true, //il_interface_exclamation_triangle
        var ssl: Boolean = false,
        var sslTrustAll: Boolean = false,
        @JsonIgnore
        var sslCert: X509Certificate? = null,
        var sslFileName: String = "",
        var address: String = "tcp://",
        var port: Int = 1883,
        var includeCred: Boolean = false,
        var username: String = "",
        var pass: String = "",
        var clientId: String = kotlin.math.abs(Random.nextInt()).toString()
    ) {
        val URI
            get() = "$address:$port"

        var sslCertStr: String? = null
            set(value) {
                field = value
                sslCert = try {
                    val cf = CertificateFactory.getInstance("X.509")
                    cf.generateCertificate(sslCertStr?.byteInputStream()) as X509Certificate
                } catch (e: Exception) {
                    null
                }
            }
    }
}