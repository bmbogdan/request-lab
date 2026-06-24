package com.example.requestlab.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun TokenHighlightField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "URL",
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val highlightColor = MaterialTheme.colorScheme.secondaryContainer
    val highlightTextColor = MaterialTheme.colorScheme.onSecondaryContainer

    val visualTransformation = remember(highlightColor, highlightTextColor) {
        VisualTransformation { text ->
            TransformedText(
                text = buildAnnotatedString {
                    append(text)
                    val raw = text.toString()
                    val regex = Regex("""\{\{[^}]*\}\}""")
                    regex.findAll(raw).forEach { match ->
                        addStyle(
                            style = SpanStyle(
                                background = highlightColor,
                                color = highlightTextColor,
                            ),
                            start = match.range.first,
                            end = match.range.last + 1,
                        )
                    }
                },
                offsetMapping = OffsetMapping.Identity,
            )
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.semantics { contentDescription = label },
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyLarge,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() },
        ),
    )
}
