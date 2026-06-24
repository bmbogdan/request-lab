package eu.mihaibadea.requestlab.feature.docs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import eu.mihaibadea.requestlab.core.designsystem.theme.spacing

// ─── Block model ─────────────────────────────────────────────────────────────────

private sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Code(val code: String) : MdBlock
    data class BulletItem(val text: String) : MdBlock
    data class TableRow(val cells: List<String>, val isHeader: Boolean) : MdBlock
    data object Rule : MdBlock
    data object Blank : MdBlock
}

// ─── Parser ──────────────────────────────────────────────────────────────────────

private fun parseMarkdown(markdown: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = markdown.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Fenced code block
        if (line.trimStart().startsWith("```")) {
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MdBlock.Code(codeLines.joinToString("\n")))
            i++
            continue
        }

        // Heading
        val headingMatch = Regex("^(#{1,6})\\s+(.+)$").find(line)
        if (headingMatch != null) {
            blocks.add(MdBlock.Heading(headingMatch.groupValues[1].length, headingMatch.groupValues[2]))
            i++
            continue
        }

        // Horizontal rule
        if (line.matches(Regex("^[-*_]{3,}\\s*$"))) {
            blocks.add(MdBlock.Rule)
            i++
            continue
        }

        // Table row
        if (line.trimStart().startsWith("|") && line.trimEnd().endsWith("|")) {
            val cells = line.split("|").drop(1).dropLast(1).map { it.trim() }
            val isHeader = i + 1 < lines.size && lines[i + 1].matches(Regex("^[|\\-: ]+$"))
            if (!cells.all { it.matches(Regex("^[-: ]+$")) }) {
                blocks.add(MdBlock.TableRow(cells, isHeader))
            }
            i++
            continue
        }

        // Bullet item
        val bulletMatch = Regex("^[-*+]\\s+(.+)$").find(line)
        if (bulletMatch != null) {
            blocks.add(MdBlock.BulletItem(bulletMatch.groupValues[1]))
            i++
            continue
        }

        // Blank line
        if (line.isBlank()) {
            blocks.add(MdBlock.Blank)
            i++
            continue
        }

        // Paragraph — accumulate consecutive non-blank, non-special lines
        val paragraphLines = mutableListOf<String>()
        while (i < lines.size) {
            val l = lines[i]
            if (l.isBlank() || l.trimStart().startsWith("```")
                || l.startsWith("#") || l.trimStart().startsWith("- ")
                || l.trimStart().startsWith("* ") || l.trimStart().startsWith("+ ")
                || l.trimStart().startsWith("|")
                || l.matches(Regex("^[-*_]{3,}\\s*$"))
            ) break
            paragraphLines.add(l)
            i++
        }
        if (paragraphLines.isNotEmpty()) {
            blocks.add(MdBlock.Paragraph(paragraphLines.joinToString(" ")))
        }
    }
    return blocks
}

// ─── Inline span renderer (bold + inline code) ───────────────────────────────────

@Composable
private fun inlineAnnotated(text: String, baseStyle: SpanStyle) = buildAnnotatedString {
    val boldCode = Regex("""\*\*(.+?)\*\*|`([^`]+)`""")
    var last = 0
    for (match in boldCode.findAll(text)) {
        if (match.range.first > last) {
            withStyle(baseStyle) { append(text.substring(last, match.range.first)) }
        }
        when {
            match.groupValues[1].isNotEmpty() -> withStyle(
                baseStyle.copy(fontWeight = FontWeight.Bold)
            ) { append(match.groupValues[1]) }
            match.groupValues[2].isNotEmpty() -> withStyle(
                baseStyle.copy(
                    fontFamily = FontFamily.Monospace,
                    background = MaterialTheme.colorScheme.surfaceVariant,
                )
            ) { append(match.groupValues[2]) }
        }
        last = match.range.last + 1
    }
    if (last < text.length) withStyle(baseStyle) { append(text.substring(last)) }
}

// ─── Block renderers ─────────────────────────────────────────────────────────────

@Composable
private fun HeadingBlock(level: Int, text: String) {
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineMedium
        2 -> MaterialTheme.typography.titleLarge
        else -> MaterialTheme.typography.titleMedium
    }
    if (level == 1) Spacer(Modifier.height(MaterialTheme.spacing.sm))
    Text(
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = if (level == 1) 0.dp else MaterialTheme.spacing.lg),
    )
}

@Composable
private fun ParagraphBlock(text: String) {
    val base = SpanStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
    )
    Text(
        text = inlineAnnotated(text, base),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
    )
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .padding(top = MaterialTheme.spacing.sm)
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.shapes.small,
            )
            .padding(MaterialTheme.spacing.md)
            .horizontalScroll(rememberScrollState()),
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BulletItemBlock(text: String) {
    val base = SpanStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
    )
    Row(modifier = Modifier.padding(top = MaterialTheme.spacing.xs)) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 1.dp),
        )
        Spacer(Modifier.width(MaterialTheme.spacing.sm))
        Text(
            text = inlineAnnotated(text, base),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TableRowBlock(cells: List<String>, isHeader: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isHeader) MaterialTheme.spacing.sm else 0.dp),
    ) {
        cells.forEachIndexed { index, cell ->
            Text(
                text = cell,
                style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
                fontWeight = if (isHeader) FontWeight.Bold else null,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = if (index == 0) 0.dp else MaterialTheme.spacing.sm,
                        bottom = MaterialTheme.spacing.xs,
                    ),
            )
        }
    }
    if (isHeader) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

// ─── Public entry point ──────────────────────────────────────────────────────────

@Composable
fun MarkdownContent(markdown: String, modifier: Modifier = Modifier) {
    val blocks = parseMarkdown(markdown)
    Column(modifier = modifier) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Heading -> HeadingBlock(block.level, block.text)
                is MdBlock.Paragraph -> ParagraphBlock(block.text)
                is MdBlock.Code -> CodeBlock(block.code)
                is MdBlock.BulletItem -> BulletItemBlock(block.text)
                is MdBlock.TableRow -> TableRowBlock(block.cells, block.isHeader)
                MdBlock.Rule -> HorizontalDivider(
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.md),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                MdBlock.Blank -> Spacer(Modifier.height(MaterialTheme.spacing.sm))
            }
        }
    }
}
