package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.DriverProfile
import com.example.ui.screens.EarningsScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun driver_earnings_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                EarningsScreen(
                    profile = DriverProfile(),
                    earningsList = emptyList(),
                    showPayoutSuccessDialog = false,
                    lastPayoutAmount = 0.0,
                    onInstantPayout = {},
                    onDismissPayoutDialog = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/driver_screen.png")
    }
}
