package dev.def.lab.droid.securecommunication.crypto

import android.content.res.AssetManager
import dev.def.lab.droid.securecommunication.error.LabError
import dev.def.lab.droid.securecommunication.error.LabFailure
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.PrivateKey
import java.security.PublicKey

interface KeyRepository {
    fun clientPrivateKey(): PrivateKey

    fun serverPublicKey(): PublicKey
}

class AssetKeyRepository(private val assets: AssetManager) : KeyRepository {
    override fun clientPrivateKey(): PrivateKey =
        load(CLIENT_PRIVATE_PATH) { der -> Pkcs1.privateKeyFromPkcs1(der) }

    override fun serverPublicKey(): PublicKey =
        load(SERVER_PUBLIC_PATH) { der -> Pkcs1.publicKeyFromPkcs1(der) }

    private fun <T> load(path: String, parse: (ByteArray) -> T): T =
        try {
            val pem = assets.open(path).use { stream -> String(stream.readBytes(), Charsets.UTF_8) }
            val der = PemCodec.decode(pem) ?: throw LabFailure(LabError.KeysUnavailable)
            parse(der)
        } catch (failure: IOException) {
            throw LabFailure(LabError.KeysUnavailable)
        } catch (failure: GeneralSecurityException) {
            throw LabFailure(LabError.KeysUnavailable)
        }

    private companion object {
        const val CLIENT_PRIVATE_PATH = "certificates/client-private.pem"
        const val SERVER_PUBLIC_PATH = "certificates/server-public.pem"
    }
}
