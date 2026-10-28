package dev.def.lab.droid.securecommunication.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.def.lab.droid.securecommunication.ExchangeStatus
import dev.def.lab.droid.securecommunication.R
import dev.def.lab.droid.securecommunication.SecureCommunicationViewModel
import dev.def.lab.droid.securecommunication.error.LabError

@Composable
fun ExchangeScreen(viewModel: SecureCommunicationViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = stringResource(R.string.app_name)) }) },
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.exchange_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.exchange_supporting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SectionCard(title = stringResource(R.string.section_message)) {
                    OutlinedTextField(
                        value = state.message,
                        onValueChange = viewModel::onMessageChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isRunning,
                    )
                }

                PrimaryAction(
                    label = stringResource(
                        if (state.isRunning) R.string.action_sending else R.string.action_send,
                    ),
                    onClick = viewModel::send,
                    enabled = !state.isRunning && state.isConfigured,
                )

                if (state.isRunning) {
                    OutlinedButton(
                        onClick = viewModel::cancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                    ) {
                        Text(text = stringResource(R.string.action_cancel))
                    }
                }

                SectionCard(title = stringResource(R.string.section_status)) {
                    Text(
                        text = stringResource(state.status.labelRes()),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                SectionCard(title = stringResource(R.string.section_raw_response)) {
                    OutputPane(
                        text = state.rawResponse,
                        placeholder = stringResource(R.string.placeholder_raw_response),
                    )
                }

                SectionCard(title = stringResource(R.string.section_acknowledgment)) {
                    OutputPane(
                        text = state.acknowledgment,
                        placeholder = stringResource(R.string.placeholder_acknowledgment),
                    )
                }

                state.configurationError?.let { error ->
                    Text(
                        text = stringResource(
                            R.string.configuration_error,
                            stringResource(error.messageRes(), error.statusCode()),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                state.error?.let { error ->
                    Text(
                        text = stringResource(error.messageRes(), error.statusCode()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@StringRes
private fun ExchangeStatus.labelRes(): Int = when (this) {
    ExchangeStatus.READY -> R.string.status_ready
    ExchangeStatus.SENDING -> R.string.status_sending
    ExchangeStatus.RECEIVED -> R.string.status_received
    ExchangeStatus.CANCELLED -> R.string.status_cancelled
}

@StringRes
private fun LabError.messageRes(): Int = when (this) {
    LabError.EmptyMessage -> R.string.error_empty_message
    LabError.InvalidEndpoint -> R.string.error_invalid_endpoint
    LabError.KeysUnavailable -> R.string.error_keys_unavailable
    LabError.EncryptionFailed -> R.string.error_encryption_failed
    LabError.SigningFailed -> R.string.error_signing_failed
    LabError.TransportFailed -> R.string.error_transport_failed
    is LabError.ServerStatus -> R.string.error_server_status
    LabError.ResponseContentType -> R.string.error_response_content_type
    LabError.ResponseShape -> R.string.error_response_shape
    LabError.DecryptionFailed -> R.string.error_decryption_failed
}

private fun LabError.statusCode(): Int = (this as? LabError.ServerStatus)?.status ?: 0
