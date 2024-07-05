package com.alteratom.dashboard.daemon.daemons.mqttd

import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.Pro
import com.alteratom.dashboard.objects.Storage
import com.alteratom.dashboard.objects.Storage.prepareSave
import com.fasterxml.jackson.annotation.JsonIgnore
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.InputStreamReader
import java.security.KeyPair
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.random.Random

data class MqttConfig(
    var isEnabled: Boolean = G.isLicensed,
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
    var address: String = "",
    var protocol: Mqttd.Protocol = Mqttd.Protocol.TCP,
    var port: Int = 1883,
    var keepAlive: Int = 50,
    var includeCred: Boolean = false,
    var queryString: String = "",
    var serverPath: String = "mqtt",
    var username: String = "",
    var pass: String = "",
    var clientId: String = kotlin.math.abs(Random.nextInt()).toString()
) {
    val sslRequired
        get() = protocol == Mqttd.Protocol.SSL || protocol == Mqttd.Protocol.WSS

    var caCertStr: String? = null
        set(value) {
            field = value
            caCert = if (value == null) null
            else try {
                val cf = CertificateFactory.getInstance("X.509")
                cf.generateCertificate(value.byteInputStream()) as X509Certificate
            } catch (e: Exception) {
                field = null
                null
            }
        }

    var clientCertStr: String? = null
        set(value) {
            field = value
            clientCert = if (value == null) null
            else try {
                val cf = CertificateFactory.getInstance("X.509")
                cf.generateCertificate(value.byteInputStream()) as X509Certificate
            } catch (e: Exception) {
                field = null
                null
            }
        }

    var clientKeyStr: String? = null
        set(value) {
            field = value
            clientKey = if (value == null) null
            else try {
                JcaPEMKeyConverter().getKeyPair(PEMParser(InputStreamReader(value.byteInputStream())).readObject() as PEMKeyPair)
            } catch (e: Exception) {
                field = null
                null
            }
        }

    init {
        if (address.contains("://")) {
            val parts = address.split("://")
            address = parts[1]
            protocol = when (parts[0].lowercase()) {
                "tcp" -> Mqttd.Protocol.TCP
                "ssl" -> Mqttd.Protocol.SSL
                "tls" -> Mqttd.Protocol.SSL
                "ws" -> Mqttd.Protocol.WS
                "wss" -> Mqttd.Protocol.WSS
                "mqtt" -> Mqttd.Protocol.TCP
                else -> Mqttd.Protocol.TCP
            }
        }
    }

    fun deepCopy(): MqttConfig? = Storage.parseSave(this.prepareSave())
}