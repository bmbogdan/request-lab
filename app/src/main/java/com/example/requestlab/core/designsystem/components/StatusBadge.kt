package com.example.requestlab.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.requestlab.core.designsystem.theme.spacing

sealed interface BadgeStatus {
    data class Http(val code: Int, val message: String = "") : BadgeStatus
    data class Failed(val reason: String) : BadgeStatus
}

@Composable
fun StatusBadge(status: BadgeStatus, modifier: Modifier = Modifier) {
    val (label, containerColor, labelColor, description) = when (status) {
        is BadgeStatus.Http -> {
            val (bg, fg) = httpColors(status.code)
            val msg = if (status.message.isNotEmpty()) "${status.code} ${status.message}" else "${status.code}"
            StatusBadgeData(msg, bg, fg, "HTTP status $msg")
        }
        is BadgeStatus.Failed -> StatusBadgeData(
            "FAILED",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurface,
            "Failed: ${status.reason}",
        )
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = labelColor,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(containerColor)
            .padding(
                horizontal = MaterialTheme.spacing.sm,
                vertical = MaterialTheme.spacing.xs,
            )
            .semantics { contentDescription = description },
    )
}

@Composable
private fun httpColors(code: Int): Pair<Color, Color> = when (code / 100) {
    2    -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    3    -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    4    -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    5    -> MaterialTheme.colorScheme.inverseSurface to MaterialTheme.colorScheme.inverseOnSurface
    else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
}

private data class StatusBadgeData(
    val label: String,
    val containerColor: Color,
    val labelColor: Color,
    val description: String,
)
