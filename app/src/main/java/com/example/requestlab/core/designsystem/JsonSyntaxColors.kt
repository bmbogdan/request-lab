package com.example.requestlab.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class JsonSyntaxColors(
    val key: Color,
    val string: Color,
    val number: Color,
    val boolean: Color,
    val null_: Color,
)

val LocalJsonSyntaxColors = staticCompositionLocalOf<JsonSyntaxColors> {
    error("JsonSyntaxColors not provided — wrap content in AppTheme")
}

val MaterialTheme.jsonSyntax: JsonSyntaxColors
    @Composable get() = LocalJsonSyntaxColors.current

@Composable
fun lightJsonSyntaxColors() = JsonSyntaxColors(
    key     = Color(0xFF8B5E3C),  // warm amber/orange tonal
    string  = Color(0xFF3A7D44),  // muted green
    number  = Color(0xFF2858A8),  // warm blue (on-brand)
    boolean = Color(0xFF7B3FA0),  // muted purple
    null_   = Color(0xFF7B766C),  // matches colorScheme.outline
)

@Composable
fun darkJsonSyntaxColors() = JsonSyntaxColors(
    key     = Color(0xFFE8A87C),
    string  = Color(0xFF8FD09A),
    number  = Color(0xFF82AADC),
    boolean = Color(0xFFCB9BDF),
    null_   = Color(0xFF959087),  // matches dark colorScheme.outline
)
