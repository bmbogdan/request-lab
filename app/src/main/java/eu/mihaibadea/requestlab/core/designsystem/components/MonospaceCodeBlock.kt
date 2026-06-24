package eu.mihaibadea.requestlab.core.designsystem.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import eu.mihaibadea.requestlab.core.designsystem.theme.MonospaceFontFamily
import eu.mihaibadea.requestlab.core.designsystem.theme.spacing

@Composable
fun MonospaceCodeBlock(
    text: String,
    modifier: Modifier = Modifier,
    contentDescription: String = "Code block",
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        SelectionContainer {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonospaceFontFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(MaterialTheme.spacing.md)
                    .semantics { this.contentDescription = contentDescription },
            )
        }
    }
}
