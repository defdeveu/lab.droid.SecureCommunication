package dev.def.lab.droid.securecommunication

import dev.def.lab.droid.securecommunication.error.LabError
import dev.def.lab.droid.securecommunication.error.LabFailure
import dev.def.lab.droid.securecommunication.service.ExchangeResult
import dev.def.lab.droid.securecommunication.service.SecureCommunicationService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SecureCommunicationViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sendPublishesBothPanes() = runTest(dispatcher) {
        val viewModel = SecureCommunicationViewModel(
            StubService(ExchangeResult("raw response", "acknowledgment")),
            dispatcher,
        )
        viewModel.onMessageChange("hello")

        viewModel.send()
        advanceUntilIdle()

        assertEquals("raw response", viewModel.uiState.value.rawResponse)
        assertEquals("acknowledgment", viewModel.uiState.value.acknowledgment)
        assertEquals(ExchangeStatus.RECEIVED, viewModel.uiState.value.status)
    }

    @Test
    fun sendWithoutAMessageReportsAnError() = runTest(dispatcher) {
        val viewModel = SecureCommunicationViewModel(StubService(), dispatcher)

        viewModel.send()
        advanceUntilIdle()

        assertEquals(LabError.EmptyMessage, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isRunning)
    }

    @Test
    fun aSecondSendWhileRunningIsIgnored() = runTest(dispatcher) {
        val service = StubService(ExchangeResult("raw", "ack"))
        val viewModel = SecureCommunicationViewModel(service, dispatcher)
        viewModel.onMessageChange("hello")

        viewModel.send()
        viewModel.send()
        advanceUntilIdle()

        assertEquals(1, service.calls)
    }

    @Test
    fun aServiceFailureIsReportedAndClearsThePanes() = runTest(dispatcher) {
        val service = StubService(ExchangeResult("raw", "ack"))
        val viewModel = SecureCommunicationViewModel(service, dispatcher)
        viewModel.onMessageChange("hello")
        viewModel.send()
        advanceUntilIdle()

        service.failure = LabError.ServerStatus(400)
        viewModel.send()
        advanceUntilIdle()

        assertEquals(LabError.ServerStatus(400), viewModel.uiState.value.error)
        assertEquals("", viewModel.uiState.value.rawResponse)
        assertEquals("", viewModel.uiState.value.acknowledgment)
        assertEquals(ExchangeStatus.READY, viewModel.uiState.value.status)
    }

    @Test
    fun cancelStopsARunningSend() = runTest(dispatcher) {
        val viewModel = SecureCommunicationViewModel(SuspendingService(), dispatcher)
        viewModel.onMessageChange("hello")

        viewModel.send()
        advanceUntilIdle()
        assertEquals(ExchangeStatus.SENDING, viewModel.uiState.value.status)

        viewModel.cancel()

        assertEquals(ExchangeStatus.CANCELLED, viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.isRunning)
    }

    @Test
    fun cancelBeforeTheSendRunsKeepsTheCancelledStatus() = runTest(dispatcher) {
        val viewModel = SecureCommunicationViewModel(
            StubService(ExchangeResult("raw", "ack")),
            dispatcher,
        )
        viewModel.onMessageChange("hello")

        viewModel.send()
        viewModel.cancel()
        advanceUntilIdle()

        assertEquals(ExchangeStatus.CANCELLED, viewModel.uiState.value.status)
        assertEquals("", viewModel.uiState.value.rawResponse)
        assertEquals("", viewModel.uiState.value.acknowledgment)
    }

    @Test
    fun anInvalidConfigurationDisablesSending() = runTest(dispatcher) {
        val service = StubService(ExchangeResult("raw", "ack"))
        val viewModel = SecureCommunicationViewModel(service, dispatcher, LabError.InvalidEndpoint)
        viewModel.onMessageChange("hello")

        viewModel.send()
        advanceUntilIdle()

        assertEquals(0, service.calls)
        assertEquals(LabError.InvalidEndpoint, viewModel.uiState.value.configurationError)
        assertFalse(viewModel.uiState.value.isConfigured)
    }

    @Test
    fun aCompletionThatArrivesAfterACancelIsIgnored() = runTest(dispatcher) {
        val viewModel = SecureCommunicationViewModel(
            UncooperativeService(ExchangeResult("late", "late-ack")),
            dispatcher,
        )
        viewModel.onMessageChange("hello")

        viewModel.send()
        advanceUntilIdle()
        assertEquals(ExchangeStatus.SENDING, viewModel.uiState.value.status)

        viewModel.cancel()
        assertEquals(ExchangeStatus.CANCELLED, viewModel.uiState.value.status)

        advanceUntilIdle()

        assertEquals(ExchangeStatus.CANCELLED, viewModel.uiState.value.status)
        assertEquals("", viewModel.uiState.value.rawResponse)
        assertEquals("", viewModel.uiState.value.acknowledgment)
    }

    @Test
    fun aCompletionFromAnAbandonedRequestIsIgnoredAfterANewerOneStarts() = runTest(dispatcher) {
        val service = GatedService(swallowCancellation = true)
        val viewModel = SecureCommunicationViewModel(service, dispatcher)
        viewModel.onMessageChange("hello")

        viewModel.send()
        advanceUntilIdle()
        assertEquals(ExchangeStatus.SENDING, viewModel.uiState.value.status)

        viewModel.cancel()
        viewModel.send()
        advanceUntilIdle()
        assertEquals(ExchangeStatus.SENDING, viewModel.uiState.value.status)
        assertEquals(2, service.calls)

        service.completeNewest(ExchangeResult("newer", "newer-ack"))
        advanceUntilIdle()

        assertEquals(ExchangeStatus.RECEIVED, viewModel.uiState.value.status)
        assertEquals("newer", viewModel.uiState.value.rawResponse)
        assertEquals("newer-ack", viewModel.uiState.value.acknowledgment)
    }

    private class GatedService(private val swallowCancellation: Boolean) : SecureCommunicationService {
        private val gates = mutableListOf<CompletableDeferred<ExchangeResult>>()

        var calls = 0
            private set

        override suspend fun send(message: String): ExchangeResult {
            calls++
            val gate = CompletableDeferred<ExchangeResult>()
            gates.add(gate)
            return try {
                gate.await()
            } catch (cancelled: CancellationException) {
                if (swallowCancellation) ExchangeResult("abandoned", "abandoned-ack") else throw cancelled
            }
        }

        fun completeNewest(result: ExchangeResult) {
            gates.removeAt(gates.lastIndex).complete(result)
        }
    }

    private class UncooperativeService(private val result: ExchangeResult) : SecureCommunicationService {
        override suspend fun send(message: String): ExchangeResult =
            try {
                awaitCancellation()
            } catch (cancelled: CancellationException) {
                result
            }
    }

    private class StubService(
        private val result: ExchangeResult = ExchangeResult("", ""),
        var failure: LabError? = null,
    ) : SecureCommunicationService {
        var calls = 0
            private set

        override suspend fun send(message: String): ExchangeResult {
            calls++
            failure?.let { throw LabFailure(it) }
            return result
        }
    }

    private class SuspendingService : SecureCommunicationService {
        override suspend fun send(message: String): ExchangeResult = awaitCancellation()
    }
}
