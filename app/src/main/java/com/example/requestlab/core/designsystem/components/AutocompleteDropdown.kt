package com.example.requestlab.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AutocompleteDropdown(
    suggestions: List<String>,
    onSelect: (String) -> Unit,
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded && suggestions.isNotEmpty(),
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        suggestions.forEach { suggestion ->
            DropdownMenuItem(
                text = { Text(suggestion, style = MaterialTheme.typography.bodyMedium) },
                onClick = { onSelect(suggestion) },
            )
        }
    }
}
