package dev.def.lab.droid.securecommunication.crypto

import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString

object Base64Codec {
    fun encode(bytes: ByteArray): String = bytes.toByteString().base64()

    fun decode(text: String): ByteArray? = text.decodeBase64()?.toByteArray()
}
