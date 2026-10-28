package dev.def.lab.droid.securecommunication.crypto

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

object Pkcs1 {
    private val RSA_ALGORITHM_IDENTIFIER = byteArrayOf(
        0x30, 0x0D, 0x06, 0x09, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(),
        0xF7.toByte(), 0x0D, 0x01, 0x01, 0x01, 0x05, 0x00,
    )
    private val INTEGER_ZERO = byteArrayOf(0x02, 0x01, 0x00)

    fun privateKeyFromPkcs1(der: ByteArray): PrivateKey {
        val pkcs8 = sequence(INTEGER_ZERO, RSA_ALGORITHM_IDENTIFIER, octetString(der))
        return KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(pkcs8))
    }

    fun publicKeyFromPkcs1(der: ByteArray): PublicKey {
        val spki = sequence(RSA_ALGORITHM_IDENTIFIER, bitString(der))
        return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(spki))
    }

    private fun sequence(vararg parts: ByteArray): ByteArray =
        tag(0x30, parts.fold(ByteArray(0)) { accumulated, part -> accumulated + part })

    private fun octetString(content: ByteArray): ByteArray = tag(0x04, content)

    private fun bitString(content: ByteArray): ByteArray = tag(0x03, byteArrayOf(0x00) + content)

    private fun tag(identifier: Int, content: ByteArray): ByteArray =
        byteArrayOf(identifier.toByte()) + length(content.size) + content

    private fun length(size: Int): ByteArray = when {
        size < 0x80 -> byteArrayOf(size.toByte())
        size < 0x100 -> byteArrayOf(0x81.toByte(), size.toByte())
        size < 0x10000 -> byteArrayOf(0x82.toByte(), (size ushr 8).toByte(), size.toByte())
        else -> throw IllegalArgumentException("value too large")
    }
}
