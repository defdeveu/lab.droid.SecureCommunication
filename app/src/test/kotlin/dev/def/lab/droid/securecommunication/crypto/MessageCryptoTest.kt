package dev.def.lab.droid.securecommunication.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageCryptoTest {
    @Test
    fun aesCbcDecryptReversesAesCbcEncrypt() {
        val plain = "development security".toByteArray(Charsets.UTF_8)
        val cipher = MessageCrypto.aesCbcEncrypt(
            plain,
            ClassroomMaterial.keyBytes,
            ClassroomMaterial.ivBytes,
        )
        assertArrayEquals(plain, MessageCrypto.aesCbcDecrypt(cipher, ClassroomMaterial.keyBytes, ClassroomMaterial.ivBytes))
    }

    @Test
    fun aesCbcProducesWholeBlocks() {
        val cipher = MessageCrypto.aesCbcEncrypt(
            "a".toByteArray(Charsets.UTF_8),
            ClassroomMaterial.keyBytes,
            ClassroomMaterial.ivBytes,
        )
        assertEquals(16, cipher.size)
    }

    @Test
    fun base64RoundTrips() {
        val bytes = byteArrayOf(0x00, 0x7F, 0x80.toByte(), 0xFF.toByte())
        assertArrayEquals(bytes, Base64Codec.decode(Base64Codec.encode(bytes)))
    }

    @Test
    fun base64RejectsNonBase64Text() {
        assertNull(Base64Codec.decode("!!!!"))
    }

    @Test
    fun pemDecodeStripsTheArmour() {
        val pem = """
            -----BEGIN TEST-----
            AAECAw==
            -----END TEST-----
        """.trimIndent()
        val decoded = PemCodec.decode(pem)
        assertArrayEquals(byteArrayOf(0x00, 0x01, 0x02, 0x03), decoded)
    }

    @Test
    fun pemDecodeRejectsAnEmptyBody() {
        assertNull(PemCodec.decode("-----BEGIN TEST-----\n-----END TEST-----"))
    }

    @Test
    fun pemDecodeReturnsNullWhenTheBodyIsNotBase64() {
        val decoded = PemCodec.decode("-----BEGIN TEST-----\n!!!!\n-----END TEST-----")
        assertNull(decoded)
    }
}
