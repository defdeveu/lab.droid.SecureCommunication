package dev.def.lab.droid.securecommunication

import dev.def.lab.droid.securecommunication.crypto.KeyRepository
import dev.def.lab.droid.securecommunication.crypto.PemCodec
import dev.def.lab.droid.securecommunication.crypto.Pkcs1
import dev.def.lab.droid.securecommunication.service.OkHttpSecureCommunicationService
import java.io.File
import java.security.PrivateKey
import java.security.PublicKey
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

class ServerInteropTest {
    @Before
    fun requireAnExplicitRequest() {
        assumeTrue(System.getenv("SECURE_COMMUNICATION_INTEROP") != null)
    }

    @Test
    fun aRealEnvelopeIsAcceptedByALocallyRunningServer() = runBlocking {
        val service = OkHttpSecureCommunicationService(
            "http://localhost:8090/secure-communication/request",
            FileKeyRepository(),
        )

        val result = service.send("interop check from the Android client")

        println("raw: " + result.rawResponse)
        println("ack: " + result.acknowledgment)
        assertTrue(result.acknowledgment.contains("<code>0</code>"))
    }

    private class FileKeyRepository : KeyRepository {
        override fun clientPrivateKey(): PrivateKey =
            Pkcs1.privateKeyFromPkcs1(der("client-private.pem"))

        override fun serverPublicKey(): PublicKey =
            Pkcs1.publicKeyFromPkcs1(der("server-public.pem"))

        private fun der(name: String): ByteArray =
            PemCodec.decode(File("src/main/assets/certificates/$name").readText())!!
    }
}
