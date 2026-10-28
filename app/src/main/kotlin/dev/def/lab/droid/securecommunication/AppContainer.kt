package dev.def.lab.droid.securecommunication

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.def.lab.droid.securecommunication.crypto.AssetKeyRepository
import dev.def.lab.droid.securecommunication.crypto.KeyRepository
import dev.def.lab.droid.securecommunication.error.LabError
import dev.def.lab.droid.securecommunication.service.OkHttpSecureCommunicationService
import dev.def.lab.droid.securecommunication.service.SecureCommunicationService
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class AppContainer(context: Context) {
    private val keys: KeyRepository = AssetKeyRepository(context.assets)
    private val endpoint: String? = BuildConfig.LAB_ENDPOINT
        .takeIf { it.isNotBlank() }
        ?.takeIf { candidate -> candidate.toHttpUrlOrNull() != null }
    private val service: SecureCommunicationService =
        OkHttpSecureCommunicationService(endpoint.orEmpty(), keys)

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            SecureCommunicationViewModel(
                service = service,
                configurationError = if (endpoint == null) LabError.InvalidEndpoint else null,
            )
        }
    }
}
