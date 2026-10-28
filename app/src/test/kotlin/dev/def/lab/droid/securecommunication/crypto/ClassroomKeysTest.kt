package dev.def.lab.droid.securecommunication.crypto

import java.io.File
import java.security.Signature
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassroomKeysTest {
    private val clientPrivate by lazy { Pkcs1.privateKeyFromPkcs1(pem("client-private.pem")) }
    private val serverPublic by lazy { Pkcs1.publicKeyFromPkcs1(pem("server-public.pem")) }

    @Test
    fun theBundledPemsAreTheClassroomPair() {
        assertEquals(2048, (clientPrivate as java.security.interfaces.RSAPrivateKey).modulus.bitLength())
        assertEquals(2048, (serverPublic as java.security.interfaces.RSAPublicKey).modulus.bitLength())
    }

    @Test
    fun theClientKeySignsAndItsOwnPublicKeyVerifies() {
        val payload = "classroom".toByteArray(Charsets.UTF_8)
        val signature = MessageCrypto.rsaSignatureSha1(payload, clientPrivate)
        val verifier = Signature.getInstance("SHA1withRSA").apply {
            initVerify(clientPublicKey())
        }
        assertNotNull(signature)
        assertEquals(256, signature.size)
        verifier.update(payload)
        assertTrue(verifier.verify(signature))
    }

    @Test
    fun theClientAndServerKeysAreDifferentPairs() {
        val clientModulus = (clientPrivate as java.security.interfaces.RSAPrivateKey).modulus
        val serverModulus = (serverPublic as java.security.interfaces.RSAPublicKey).modulus
        assertTrue(clientModulus != serverModulus)
    }

    private fun clientPublicKey(): java.security.PublicKey {
        val rsaPrivate = clientPrivate as java.security.interfaces.RSAPrivateCrtKey
        return java.security.KeyFactory.getInstance("RSA").generatePublic(
            java.security.spec.RSAPublicKeySpec(rsaPrivate.modulus, rsaPrivate.publicExponent),
        )
    }

    private fun pem(name: String): ByteArray {
        val file = File("src/main/assets/certificates/$name")
        assertTrue("missing ${file.path}", file.exists())
        return PemCodec.decode(file.readText()) ?: error("unreadable ${file.path}")
    }
}
