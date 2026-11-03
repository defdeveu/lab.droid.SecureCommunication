package dev.def.lab.droid.securecommunication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.def.lab.droid.securecommunication.R

@Composable
fun LabHeader() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMediumEmphasized.copy(
                    fontSize = 19.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight(875),
                ),
                modifier = Modifier.semantics { heading() },
            )
        },
        navigationIcon = {
            Box(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
                Icon(
                    painter = painterResource(R.drawable.brand_stamp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(40.dp),
                )
            }
        },
    )
}

@Composable
fun ResultGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            content = content,
        )
    }
}

@Composable
fun OutputSection(title: String, text: String, placeholder: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmallEmphasized,
            modifier = Modifier.semantics { heading() },
        )
        SelectionContainer {
            Text(
                text = text.ifEmpty { placeholder },
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = if (text.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun PrimaryAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isRunning: Boolean = false,
) {
    val height = 56.dp
    Button(
        onClick = onClick,
        enabled = enabled && !isRunning,
        colors = if (isRunning) {
            ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            ButtonDefaults.buttonColors()
        },
        shapes = ButtonDefaults.shapes(
            shape = MaterialTheme.shapes.large,
            pressedShape = ButtonDefaults.mediumPressedShape,
        ),
        contentPadding = ButtonDefaults.contentPaddingFor(height),
        modifier = modifier.heightIn(min = height),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_shield_moon),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.MediumIconSize),
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.MediumIconSpacing))
        Text(text = label, style = ButtonDefaults.textStyleFor(height))
    }
}
