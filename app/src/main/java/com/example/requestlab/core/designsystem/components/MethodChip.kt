package com.example.requestlab.core.designsystem.components

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.requestlab.core.common.model.HttpMethod
import com.example.requestlab.core.designsystem.theme.spacing

@Composable
fun MethodChip(
    method: HttpMethod,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val (containerColor, labelColor) = methodColors(method)
    val description = "HTTP method: ${method.name}${if (onClick != null) ". Tap to change." else ""}"

    SuggestionChip(
        onClick = onClick ?: {},
        label = { Text(method.name, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .semantics { contentDescription = description },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = labelColor,
        ),
        border = if (method == HttpMethod.HEAD || method == HttpMethod.OPTIONS)
            SuggestionChipDefaults.suggestionChipBorder(
                enabled = true,
                borderColor = MaterialTheme.colorScheme.outline,
            )
        else null,
    )
}

@Composable
private fun methodColors(method: HttpMethod): Pair<Color, Color> = when (method) {
    HttpMethod.GET     -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    HttpMethod.POST    -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    HttpMethod.PUT,
    HttpMethod.PATCH   -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    HttpMethod.DELETE  -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    HttpMethod.HEAD,
    HttpMethod.OPTIONS -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
}
