package dev.def.lab.droid.securecommunication.protocol

import dev.def.lab.droid.securecommunication.crypto.Base64Codec

object Envelope {
    private const val OPENING = "<response>"
    private const val CLOSING = "</response>"

    fun request(wrappedKey: ByteArray, message: ByteArray, signature: ByteArray): String =
        "<request><enckey>" + Base64Codec.encode(wrappedKey) + "</enckey>" +
            "<message>" + Base64Codec.encode(message) + "</message>" +
            "<signature>" + Base64Codec.encode(signature) + "</signature></request>"

    fun responseBody(text: String): ByteArray? {
        val opening = text.indexOf(OPENING)
        val closing = text.indexOf(CLOSING)
        if (opening < 0 || closing < 0 || closing < opening) {
            return null
        }
        return Base64Codec.decode(text.substring(opening + OPENING.length, closing))
    }
}
