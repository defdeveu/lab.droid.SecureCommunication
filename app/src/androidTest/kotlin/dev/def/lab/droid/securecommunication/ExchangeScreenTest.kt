package dev.def.lab.droid.securecommunication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class ExchangeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun text(resourceId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resourceId)

    @Test
    fun theScreenShowsTheMessageSection() {
        composeRule
            .onNodeWithText(text(R.string.section_message))
            .assertIsDisplayed()
    }

    @Test
    fun theStatusStartsAtReady() {
        composeRule
            .onNodeWithText(text(R.string.status_ready))
            .assertIsDisplayed()
    }
}
