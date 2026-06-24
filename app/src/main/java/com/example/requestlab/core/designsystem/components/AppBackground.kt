package com.example.requestlab.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.dotGridBackground(
    dotColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
    dotRadius: Float = 1.5f,
    spacing: Float = 28f,
): Modifier = drawBehind {
    val cols = (size.width / spacing).toInt() + 2
    val rows = (size.height / spacing).toInt() + 2
    for (col in 0..cols) {
        for (row in 0..rows) {
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(col * spacing, row * spacing),
            )
        }
    }
}
