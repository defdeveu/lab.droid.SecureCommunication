package dev.def.lab.droid.securecommunication.crypto

object PemCodec {
    fun decode(pem: String): ByteArray? {
        val body = pem.lineSequence()
            .filterNot { it.startsWith("-----") }
            .joinToString(separator = "")
            .trim()
        if (body.isEmpty()) {
            return null
        }
        return Base64Codec.decode(body)
    }
}
