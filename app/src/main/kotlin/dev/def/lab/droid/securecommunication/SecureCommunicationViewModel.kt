package dev.def.lab.droid.securecommunication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.def.lab.droid.securecommunication.error.LabError
import dev.def.lab.droid.securecommunication.error.LabFailure
import dev.def.lab.droid.securecommunication.service.SecureCommunicationService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ExchangeStatus { READY, SENDING, RECEIVED, CANCELLED }

data class ExchangeUiState(
    val message: String = "",
    val rawResponse: String = "",
    val acknowledgment: String = "",
    val status: ExchangeStatus = ExchangeStatus.READY,
    val error: LabError? = null,
    val configurationError: LabError? = null,
    val operationId: Long = 0,
) {
    val isRunning: Boolean get() = status == ExchangeStatus.SENDING

    val isConfigured: Boolean get() = configurationError == null
}

class SecureCommunicationViewModel(
    private val service: SecureCommunicationService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    configurationError: LabError? = null,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(ExchangeUiState(configurationError = configurationError))
    private var operation: Job? = null

    val uiState: StateFlow<ExchangeUiState> = mutableUiState.asStateFlow()

    fun onMessageChange(value: String) {
        mutableUiState.update { it.copy(message = value) }
    }

    fun send() {
        val current = mutableUiState.value
        if (current.isRunning || !current.isConfigured) {
            return
        }
        val message = current.message
        if (message.isEmpty()) {
            mutableUiState.update {
                it.copy(error = LabError.EmptyMessage, rawResponse = "", acknowledgment = "")
            }
            return
        }
        val operationId = current.operationId + 1
        mutableUiState.update {
            it.copy(
                operationId = operationId,
                status = ExchangeStatus.SENDING,
                error = null,
                rawResponse = "",
                acknowledgment = "",
            )
        }
        operation = viewModelScope.launch(dispatcher) {
            try {
                val result = service.send(message)
                publish(operationId) {
                    it.copy(
                        status = ExchangeStatus.RECEIVED,
                        rawResponse = result.rawResponse,
                        acknowledgment = result.acknowledgment,
                    )
                }
            } catch (failure: LabFailure) {
                publish(operationId) {
                    it.copy(
                        status = ExchangeStatus.READY,
                        error = failure.reason,
                        rawResponse = "",
                        acknowledgment = "",
                    )
                }
            }
        }
    }

    fun cancel() {
        if (!mutableUiState.value.isRunning) {
            return
        }
        operation?.cancel()
        operation = null
        mutableUiState.update {
            it.copy(operationId = it.operationId + 1, status = ExchangeStatus.CANCELLED, error = null)
        }
    }

    private fun publish(operationId: Long, transform: (ExchangeUiState) -> ExchangeUiState) {
        mutableUiState.update { state ->
            if (state.operationId == operationId) transform(state) else state
        }
    }
}
