package dev.def.lab.droid.securecommunication

import android.graphics.Bitmap
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.ClipData
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.printToString
import androidx.test.platform.app.InstrumentationRegistry
import dev.def.lab.droid.securecommunication.error.LabError
import dev.def.lab.droid.securecommunication.error.LabFailure
import dev.def.lab.droid.securecommunication.service.ExchangeResult
import dev.def.lab.droid.securecommunication.service.SecureCommunicationService
import dev.def.lab.droid.securecommunication.theme.SecureCommunicationTheme
import dev.def.lab.droid.securecommunication.ui.ExchangeScreen
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExchangeInteractionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private class PendingService : SecureCommunicationService {
        val result = CompletableDeferred<ExchangeResult>()
        val calls = AtomicInteger()

        override suspend fun send(message: String): ExchangeResult {
            calls.incrementAndGet()
            return result.await()
        }
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val evidence get() = File(context.cacheDir, "ui-acceptance").apply { mkdirs() }
    private val progress = hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)

    private fun text(id: Int): String = context.getString(id)

    private fun show(service: SecureCommunicationService, keyboard: Boolean = false): SecureCommunicationViewModel {
        val model = SecureCommunicationViewModel(service)
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                val inputModeManager = LocalInputModeManager.current
                SideEffect { if (keyboard) inputModeManager.requestInputMode(InputMode.Keyboard) }
                SecureCommunicationTheme { ExchangeScreen(model) }
            }
        }
        composeRule.onNode(hasSetTextAction()).performTextInput("Test message")
        return model
    }

    private fun save(name: String, image: Bitmap) {
        File(evidence, "$name.png").outputStream().use {
            check(image.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }

    private fun capture(name: String) {
        val root = composeRule.onNode(
            isRoot() and hasAnyDescendant(hasText(text(R.string.app_name))),
            useUnmergedTree = true,
        )
        save(name, root.captureToImage().asAndroidBitmap())
        File(evidence, "$name.semantics.txt").writeText(root.printToString())
        if (name == "output-selection") {
            save("output-selection-system", requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()))
        }
    }

    @Test
    fun sendingKeepsItsColourAnimatesAndCanBeCancelled() {
        val service = PendingService()
        val model = show(service)
        val before = composeRule.onNodeWithText(text(R.string.action_send))
            .captureToImage().asAndroidBitmap()
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithText(text(R.string.action_send)).performClick()
        composeRule.mainClock.advanceTimeBy(64)
        composeRule.onNodeWithText(text(R.string.action_sending)).assertIsNotEnabled()
        composeRule.onNodeWithText("Test message").assertIsNotEnabled()
        composeRule.onNodeWithText(text(R.string.action_cancel)).assertIsEnabled().assertIsDisplayed()
        composeRule.onNode(progress).assertIsDisplayed()
        composeRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        composeRule.mainClock.advanceTimeBy(600)
        val during = composeRule.onNodeWithText(text(R.string.action_sending))
            .captureToImage().asAndroidBitmap()
        val beforeColour = before.getPixel(before.width / 2, before.height / 5)
        val duringColour = during.getPixel(during.width / 2, during.height / 5)
        for (shift in listOf(0, 8, 16)) {
            assertTrue(kotlin.math.abs(((beforeColour shr shift) and 255) - ((duringColour shr shift) and 255)) <= 8)
        }
        capture("sending")
        val first = composeRule.onNode(progress).captureToImage().asAndroidBitmap()
        composeRule.mainClock.advanceTimeBy(640)
        val second = composeRule.onNode(progress).captureToImage().asAndroidBitmap()
        save("motion-first", first)
        save("motion-second", second)
        assertFalse(first.sameAs(second))
        if (InstrumentationRegistry.getArguments().getString("captureMotion") == "true") {
            repeat(60) { frame ->
                composeRule.mainClock.advanceTimeBy(80)
                save("motion-${frame.toString().padStart(2, '0')}", composeRule.onNode(progress).captureToImage().asAndroidBitmap())
            }
        }
        composeRule.mainClock.autoAdvance = true
        composeRule.onNodeWithText(text(R.string.action_cancel)).performClick()
        composeRule.onNodeWithText(text(R.string.status_cancelled)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.action_send)).assertIsEnabled()
        composeRule.onNodeWithText(text(R.string.action_cancel)).assertDoesNotExist()
        composeRule.onNode(progress).assertDoesNotExist()
        assertEquals("Test message", model.uiState.value.message)
        capture("cancelled")
    }

    @Test
    fun aFailureIsPresentedBeforeOutputsAndCanBeRetried() {
        val service = PendingService()
        val model = show(service)
        composeRule.onNodeWithText(text(R.string.action_send)).performClick()
        composeRule.runOnIdle { service.result.completeExceptionally(LabFailure(LabError.TransportFailed)) }
        composeRule.waitUntil(10_000) { model.uiState.value.error == LabError.TransportFailed }
        composeRule.onNodeWithText(text(R.string.status_failed)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.error_transport_failed)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.status_ready)).assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.action_send)).assertIsEnabled()
        composeRule.onNode(progress).assertDoesNotExist()
        val failure = composeRule.onNodeWithText(text(R.string.error_transport_failed)).fetchSemanticsNode()
        val output = composeRule.onNodeWithText(text(R.string.section_raw_response)).fetchSemanticsNode()
        check(failure.boundsInRoot.bottom < output.boundsInRoot.top)
        assertEquals("Test message", model.uiState.value.message)
        capture("failure")
        composeRule.onNodeWithText(text(R.string.action_send)).performClick()
        composeRule.waitUntil(10_000) { service.calls.get() == 2 && model.uiState.value.error != null }
    }

    @Test
    fun longOutputsRemainReachableAndHaveSelectionSemantics() {
        val service = PendingService()
        val model = show(service)
        val raw = "Response content ".repeat(400)
        val acknowledgment = "Acknowledgment content"
        composeRule.onNodeWithText(text(R.string.action_send)).performClick()
        composeRule.runOnIdle { service.result.complete(ExchangeResult(raw, acknowledgment)) }
        composeRule.waitUntil(10_000) { model.uiState.value.status == ExchangeStatus.RECEIVED }
        composeRule.onNodeWithText(text(R.string.status_received)).assertIsDisplayed()
        capture("long-response-top")
        composeRule.onNodeWithText(acknowledgment).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(raw).assertExists()
        capture("long-response-bottom")
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        composeRule.onNodeWithText(acknowledgment).performTouchInput { longClick() }
        capture("output-selection")
        val copyLabel = context.getString(android.R.string.copy)
        val copyNodes = {
            automation.windows.flatMap { it.root?.findAccessibilityNodeInfosByText(copyLabel).orEmpty() }
        }
        composeRule.waitUntil(5_000) {
            copyNodes().isNotEmpty()
        }
        val copy = copyNodes().first()
        val clipboard = requireNotNull(context.getSystemService(ClipboardManager::class.java))
        val previousClip = clipboard.primaryClip
        try {
            check(copy.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            composeRule.waitUntil(5_000) {
                val copied = clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
                copied.isNotEmpty() && acknowledgment.contains(copied)
            }
        } finally {
            if (previousClip != null) {
                clipboard.setPrimaryClip(previousClip)
            } else if (Build.VERSION.SDK_INT >= 28) {
                clipboard.clearPrimaryClip()
            } else {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        }
        composeRule.onNodeWithText(text(R.string.action_send)).performScrollTo().assertIsEnabled()
    }

    @Test
    fun thePrimaryAndCancelActionsCanBeOperatedFromTheKeyboard() {
        val service = PendingService()
        val model = show(service, keyboard = true)
        composeRule.onNode(hasSetTextAction()).assertIsFocused().performKeyInput { pressKey(Key.Tab) }
        composeRule.onNodeWithText(text(R.string.action_send)).assertIsFocused()
            .performKeyInput {
                keyDown(Key.ShiftLeft)
                pressKey(Key.Tab)
                keyUp(Key.ShiftLeft)
            }
        composeRule.onNode(hasSetTextAction()).assertIsFocused()
            .performKeyInput { pressKey(Key.Tab) }
        composeRule.onNodeWithText(text(R.string.action_send)).assertIsFocused()
            .performKeyInput { pressKey(Key.Enter) }
        composeRule.waitUntil(10_000) { model.uiState.value.status == ExchangeStatus.SENDING }
        composeRule.onNodeWithText(text(R.string.action_cancel))
            .performSemanticsAction(SemanticsActions.RequestFocus) { it() }
            .performKeyInput { pressKey(Key.Enter) }
        composeRule.onNodeWithText(text(R.string.status_cancelled)).assertIsDisplayed()
        assertEquals("Test message", model.uiState.value.message)
        composeRule.onNodeWithText(text(R.string.app_name))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        capture("keyboard-cancelled")
    }
}
