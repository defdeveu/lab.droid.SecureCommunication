package dev.def.lab.droid.securecommunication.crypto

object ClassroomMaterial {
    const val KEY = "00112233445566778899aabbccddeeff"
    const val IV = "1111111111111111"
    const val KEY_MATERIAL = "$KEY|$IV"

    val keyBytes: ByteArray = KEY.toByteArray(Charsets.UTF_8)
    val ivBytes: ByteArray = IV.toByteArray(Charsets.UTF_8)
    val keyMaterialBytes: ByteArray = KEY_MATERIAL.toByteArray(Charsets.UTF_8)
}
