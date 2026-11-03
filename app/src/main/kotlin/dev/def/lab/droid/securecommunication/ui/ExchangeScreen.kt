package dev.def.lab.droid.securecommunication.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.def.lab.droid.securecommunication.ExchangeStatus
import dev.def.lab.droid.securecommunication.R
import dev.def.lab.droid.securecommunication.SecureCommunicationViewModel
import dev.def.lab.droid.securecommunication.error.LabError

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExchangeScreen(viewModel: SecureCommunicationViewModel) {
    val focusManager = LocalFocusManager.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val emptyMessage = state.error == LabError.EmptyMessage
    val failure = state.configurationError ?: state.error?.takeUnless { emptyMessage }
    val hasError = (failure != null) || emptyMessage

    Scaffold(topBar = { LabHeader() }) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .imePadding()
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
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = stringResource(R.string.exchange_supporting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                TextField(
                    value = state.message,
                    onValueChange = viewModel::onMessageChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPreviewKeyEvent { event ->
                            if (event.key == Key.Tab) {
                                if (event.type == KeyEventType.KeyDown) {
                                    focusManager.moveFocus(
                                        if (event.isShiftPressed) FocusDirection.Previous else FocusDirection.Next,
                                    )
                                }
                                true
                            } else {
                                false
                            }
                        },
                    enabled = !state.isRunning,
                    label = {
                        Text(
                            text = stringResource(R.string.section_message),
                            fontWeight = MaterialTheme.typography.labelLargeEmphasized.fontWeight,
                        )
                    },
                    minLines = 3,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    shape = MaterialTheme.shapes.large,
                    isError = emptyMessage,
                    supportingText = if (emptyMessage) {
                        { Text(text = stringResource(R.string.error_empty_message)) }
                    } else {
                        null
                    },
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PrimaryAction(
                        label = stringResource(
                            if (state.isRunning) R.string.action_sending else R.string.action_send,
                        ),
                        onClick = viewModel::send,
                        enabled = state.isConfigured,
                        isRunning = state.isRunning,
                        modifier = Modifier.weight(1f).widthIn(min = 200.dp),
                    )
                    if (state.isRunning) {
                        OutlinedButton(
                            onClick = viewModel::cancel,
                            modifier = Modifier.heightIn(min = 56.dp),
                        ) {
                            Text(text = stringResource(R.string.action_cancel))
                        }
                    }
                }

                ResultGroup {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (state.isRunning) {
                            LoadingIndicator()
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        ) {
                            Text(
                                text = stringResource(R.string.section_status),
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                modifier = Modifier.semantics { heading() },
                            )
                            Text(
                                text = stringResource(
                                    when {
                                        state.configurationError != null -> R.string.status_configuration
                                        failure != null -> R.string.status_failed
                                        emptyMessage -> R.string.status_validation
                                        else -> state.status.labelRes()
                                    },
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (hasError) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                            failure?.let { error ->
                                Text(
                                    text = stringResource(error.messageRes(), error.statusCode()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    OutputSection(
                        title = stringResource(R.string.section_raw_response),
                        text = state.rawResponse,
                        placeholder = stringResource(R.string.placeholder_raw_response),
                    )
                    OutputSection(
                        title = stringResource(R.string.section_acknowledgment),
                        text = state.acknowledgment,
                        placeholder = stringResource(R.string.placeholder_acknowledgment),
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
