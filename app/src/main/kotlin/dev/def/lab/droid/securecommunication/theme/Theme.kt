package dev.def.lab.droid.securecommunication.theme

import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

private val LabTypography = Typography(fontFamily = FontFamily.Default).let { scale ->
    scale.copy(
        displayLarge = scale.displayLargeEmphasized,
        displayMedium = scale.displayMediumEmphasized,
        displaySmall = scale.displaySmallEmphasized,
        headlineLarge = scale.headlineLargeEmphasized,
        headlineMedium = scale.headlineMediumEmphasized,
        headlineSmall = scale.headlineSmallEmphasized,
        titleLarge = scale.titleLargeEmphasized,
        titleMedium = scale.titleMediumEmphasized,
        titleSmall = scale.titleSmallEmphasized,
        labelLarge = scale.labelLargeEmphasized,
        labelMedium = scale.labelMediumEmphasized,
        labelSmall = scale.labelSmallEmphasized,
    )
}

@Composable
fun SecureCommunicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = LabLightColors,
        motionScheme = MotionScheme.expressive(),
        typography = LabTypography,
        content = content,
    )
}
