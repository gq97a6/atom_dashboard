package com.alteratom.dashboard.daemon.daemons.mqttd

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
    var isEnabled: Boolean = Pro.status,
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

    fun deepCopy(): MqttConfig? = Storage.parseSave(this.prepareSave())
}