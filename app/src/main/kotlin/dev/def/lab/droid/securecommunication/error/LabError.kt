package dev.def.lab.droid.securecommunication.error

sealed class LabError {
    data object EmptyMessage : LabError()

    data object InvalidEndpoint : LabError()

    data object KeysUnavailable : LabError()

    data object EncryptionFailed : LabError()

    data object SigningFailed : LabError()

    data object TransportFailed : LabError()

    data class ServerStatus(val status: Int) : LabError()

    data object ResponseContentType : LabError()

    data object ResponseShape : LabError()

    data object DecryptionFailed : LabError()
}
