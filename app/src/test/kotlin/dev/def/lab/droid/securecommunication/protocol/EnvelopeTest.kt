package dev.def.lab.droid.securecommunication.protocol

import dev.def.lab.droid.securecommunication.crypto.Base64Codec
import dev.def.lab.droid.securecommunication.crypto.ClassroomMaterial
import dev.def.lab.droid.securecommunication.crypto.MessageCrypto
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnvelopeTest {
    @Test
    fun theRequestCarriesThreeBase64Fields() {
        val xml = Envelope.request(
            byteArrayOf(1, 2, 3),
            byteArrayOf(4, 5, 6),
            byteArrayOf(7, 8, 9),
        )
        assertTrue(xml.startsWith("<request>"))
        assertTrue(xml.endsWith("</request>"))
        assertEquals("AQID", field(xml, "enckey"))
        assertEquals("BAUG", field(xml, "message"))
        assertEquals("BwgJ", field(xml, "signature"))
    }

    @Test
    fun aResponseBodyRoundTripsThroughEncryption() {
        val acknowledgment = "<code>0</code><id>0123456789abcdef0123456789abcdef</id>"
        val sealed = MessageCrypto.aesCbcEncrypt(
            acknowledgment.toByteArray(Charsets.UTF_8),
            ClassroomMaterial.keyBytes,
            ClassroomMaterial.ivBytes,
        )
        val body = "<response>" + Base64Codec.encode(sealed) + "</response>"
        val payload = Envelope.responseBody(body)
        assertArrayEquals(
            acknowledgment.toByteArray(Charsets.UTF_8),
            MessageCrypto.aesCbcDecrypt(payload!!, ClassroomMaterial.keyBytes, ClassroomMaterial.ivBytes),
        )
    }

    @Test
    fun aResponseWithoutTheElementIsRejected() {
        assertNull(Envelope.responseBody("not a response"))
    }

    @Test
    fun aResponseWithNonBase64ContentIsRejected() {
        assertNull(Envelope.responseBody("<response>!!!!</response>"))
    }

    private fun field(xml: String, name: String): String {
        val opening = "<$name>"
        val closing = "</$name>"
        val start = xml.indexOf(opening) + opening.length
        val end = xml.indexOf(closing)
        return xml.substring(start, end)
    }
}
