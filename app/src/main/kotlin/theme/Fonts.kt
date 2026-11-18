package defdev.labs.droid.securecommunication.theme

import androidx.annotation.FontRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import defdev.labs.droid.securecommunication.R

val LabSans = variableFontFamily(R.font.noto_sans_variable, 400, 500, 700)
val LabMono = variableFontFamily(R.font.noto_sans_mono_variable, 400)
val LabTitle = variableFontFamily(R.font.noto_serif_display_variable, 875)

private fun variableFontFamily(@FontRes resource: Int, vararg weights: Int): FontFamily =
    FontFamily(weights.map { weight ->
        Font(
            resId = resource,
            weight = FontWeight(weight),
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight),
                FontVariation.width(100f),
            ),
        )
    })
