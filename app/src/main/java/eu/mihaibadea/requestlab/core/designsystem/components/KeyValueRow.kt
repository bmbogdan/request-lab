package eu.mihaibadea.requestlab.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import eu.mihaibadea.requestlab.core.designsystem.theme.spacing

@Composable
fun KeyValueRow(
    key: String,
    value: String,
    onKeyChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onEnabledChange: ((Boolean) -> Unit)? = null,
    keyLabel: String = "Key",
    valueLabel: String = "Value",
    keyTrailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = key,
            onValueChange = onKeyChange,
            label = { Text(keyLabel, style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            trailingIcon = keyTrailingContent,
        )
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(valueLabel, style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f),
        )
        if (onEnabledChange != null) {
            Spacer(Modifier.width(MaterialTheme.spacing.xs))
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .semantics { contentDescription = "Delete $key row" },
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
