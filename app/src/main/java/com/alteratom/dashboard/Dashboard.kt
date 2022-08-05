package com.alteratom.dashboard

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alteratom.R
import com.alteratom.dashboard.foreground_service.demons.Daemon
import com.alteratom.dashboard.icon.Icons
import com.alteratom.dashboard.log.Log
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.alteratom.dashboard.tile.Tile
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.InputStreamReader
import java.security.KeyPair
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import kotlin.random.Random

open class Dashboard(var name: String = "", var type: Daemon.Type = Daemon.Type.MQTTD) :
    RecyclerViewItem() {

    override val layout
        get() = R.layout.item_dashboard


    var iconKey = "il_interface_plus_circle"
    val iconRes: Int
        get() = Icons.icons[iconKey]?.res ?: R.drawable.il_interface_plus_circle

    var hsv = G.theme.a.hsv.let {
        floatArrayOf(it[0], it[1], it[2])
    }

    val pallet: Theme.ColorPallet
        get() = G.theme.a.getColorPallet(hsv, true)

    var log = Log()

    @JsonAlias("mqtt")
    var mqttData = MqttBrokerData()

    @JsonIgnore
    lateinit var daemon: Daemon

    var tiles: MutableList<Tile> = mutableListOf()
        set(value) {
            for (t in value) t.dashboard = this
            field = value
        }

    override fun onBindViewHolder(
        holder: RecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = ((screenWidth - view.paddingLeft * 2) * 1 / 3.236).toInt()
        view.layoutParams = params

        holder.itemView.findViewById<View>(R.id.id_icon)?.setBackgroundResource(iconRes)

        holder.itemView.findViewById<TextView>(R.id.id_tag).text =
            name.uppercase(Locale.getDefault())
    }

    override fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        G.theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = pallet
        )
    }

    data class MqttBrokerData(
        var isEnabled: Boolean = true,
        var ssl: Boolean = false,
        var sslTrustAll: Boolean = false,
        @JsonIgnore
        var caCert: X509Certificate? = null,
        var caFileName: String = "",
        @JsonIgnore
        var clientCert: X509Certificate? = null,
        var clientFileName: String = "",
        @JsonIgnore
        var clientKey: KeyPair? = null,
        var keyFileName: String = "",
        var clientKeyPassword: String = "",
        var address: String = "tcp://",
        var port: Int = 1883,
        var includeCred: Boolean = false,
        var username: String = "",
        var pass: String = "",
        var clientId: String = kotlin.math.abs(Random.nextInt()).toString()
    ) {
        val uri
            get() = "$address:$port"

        var caCertStr: String? = null
            set(value) {
                field = value
                caCert = try {
                    val cf = CertificateFactory.getInstance("X.509")
                    cf.generateCertificate(value?.byteInputStream()) as X509Certificate
                } catch (e: Exception) {
                    field = null
                    null
                }
            }

        var clientCertStr: String? = null
            set(value) {
                field = value
                clientCert = try {
                    val cf = CertificateFactory.getInstance("X.509")
                    cf.generateCertificate(value?.byteInputStream()) as X509Certificate
                } catch (e: Exception) {
                    field = null
                    null
                }
            }

        var clientKeyStr: String? = null
            set(value) {
                field = value
                clientKey = try {
                    JcaPEMKeyConverter().getKeyPair(PEMParser(InputStreamReader(value?.byteInputStream())).readObject() as PEMKeyPair)
                } catch (e: Exception) {
                    field = null
                    null
                }
            }
    }
}