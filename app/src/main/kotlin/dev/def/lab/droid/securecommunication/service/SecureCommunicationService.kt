package dev.def.lab.droid.securecommunication.service

import android.util.Log
import dev.def.lab.droid.securecommunication.crypto.Base64Codec
import dev.def.lab.droid.securecommunication.crypto.ClassroomMaterial
import dev.def.lab.droid.securecommunication.crypto.KeyRepository
import dev.def.lab.droid.securecommunication.crypto.MessageCrypto
import dev.def.lab.droid.securecommunication.error.LabError
import dev.def.lab.droid.securecommunication.error.LabFailure
import dev.def.lab.droid.securecommunication.protocol.Envelope
import java.io.IOException
import java.security.GeneralSecurityException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

data class ExchangeResult(val rawResponse: String, val acknowledgment: String)

interface SecureCommunicationService {
    suspend fun send(message: String): ExchangeResult
}

class OkHttpSecureCommunicationService(
    private val endpoint: String,
    private val keys: KeyRepository,
    private val client: OkHttpClient = OkHttpClient(),
) : SecureCommunicationService {
    override suspend fun send(message: String): ExchangeResult {
        val wrappedKey = try {
            MessageCrypto.rsaEncrypt(ClassroomMaterial.keyMaterialBytes, keys.serverPublicKey())
        } catch (failure: GeneralSecurityException) {
            throw LabFailure(LabError.EncryptionFailed)
        }
        val ciphertext = try {
            MessageCrypto.aesCbcEncrypt(
                message.toByteArray(Charsets.UTF_8),
                ClassroomMaterial.keyBytes,
                ClassroomMaterial.ivBytes,
            )
        } catch (failure: GeneralSecurityException) {
            throw LabFailure(LabError.EncryptionFailed)
        }
        val signature = try {
            MessageCrypto.rsaSignatureSha1(ciphertext, keys.clientPrivateKey())
        } catch (failure: GeneralSecurityException) {
            throw LabFailure(LabError.SigningFailed)
        }

        Log.d(TAG, "wrapped key material: " + Base64Codec.encode(wrappedKey))
        Log.d(TAG, "message ciphertext: " + Base64Codec.encode(ciphertext))
        Log.d(TAG, "signature: " + Base64Codec.encode(signature))

        val request = try {
            Request.Builder()
                .url(endpoint)
                .post(Envelope.request(wrappedKey, ciphertext, signature).toRequestBody(XML))
                .build()
        } catch (failure: IllegalArgumentException) {
            throw LabFailure(LabError.InvalidEndpoint)
        }

        val received = try {
            client.newCall(request).awaitExchange()
        } catch (failure: IOException) {
            throw LabFailure(LabError.TransportFailed)
        }

        if (received.status !in 200..299) {
            throw LabFailure(LabError.ServerStatus(received.status))
        }
        if (!received.contentType.lowercase().startsWith("text/html")) {
            throw LabFailure(LabError.ResponseContentType)
        }
        val payload = Envelope.responseBody(received.body) ?: throw LabFailure(LabError.ResponseShape)
        val acknowledgment = try {
            String(
                MessageCrypto.aesCbcDecrypt(
                    payload,
                    ClassroomMaterial.keyBytes,
                    ClassroomMaterial.ivBytes,
                ),
                Charsets.UTF_8,
            )
        } catch (failure: GeneralSecurityException) {
            throw LabFailure(LabError.DecryptionFailed)
        }
        return ExchangeResult(received.body, acknowledgment)
    }

    private suspend fun Call.awaitExchange(): Received = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val received = try {
                        response.use {
                            Received(
                                status = it.code,
                                contentType = it.header("Content-Type").orEmpty(),
                                body = it.body?.string().orEmpty(),
                            )
                        }
                    } catch (failure: IOException) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(failure)
                        }
                        return
                    }
                    if (continuation.isActive) {
                        continuation.resume(received)
                    }
                }
            },
        )
    }

    private data class Received(val status: Int, val contentType: String, val body: String)

    private companion object {
        const val TAG = "diva-sc"
        val XML = "application/text".toMediaType()
    }
}
