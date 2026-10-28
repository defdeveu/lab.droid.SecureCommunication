package dev.def.lab.droid.securecommunication.crypto

import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object MessageCrypto {
    fun rsaEncrypt(plain: ByteArray, publicKey: PublicKey): ByteArray =
        Cipher.getInstance("RSA/ECB/PKCS1Padding")
            .apply { init(Cipher.ENCRYPT_MODE, publicKey) }
            .doFinal(plain)

    fun aesCbcEncrypt(plain: ByteArray, key: ByteArray, iv: ByteArray): ByteArray =
        Cipher.getInstance("AES/CBC/PKCS5Padding")
            .apply { init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv)) }
            .doFinal(plain)

    fun aesCbcDecrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray =
        Cipher.getInstance("AES/CBC/PKCS5Padding")
            .apply { init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv)) }
            .doFinal(ciphertext)

    fun rsaSignatureSha1(data: ByteArray, privateKey: PrivateKey): ByteArray =
        java.security.Signature.getInstance("SHA1withRSA")
            .apply {
                initSign(privateKey)
                update(data)
            }
            .sign()
}
