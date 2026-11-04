package com.perspective.app.ui.brand

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.perspective.app.ui.theme.Space

/**
 * The PERSPECT1VE wordmark, drawn in code rather than shipped as a bitmap.
 *
 * Code wins here on three counts: it stays sharp at any size, it takes the
 * theme's ink colour so light and dark need one mark instead of two, and the
 * mirrored glyphs survive a change of display face — which matters while that
 * face is still being confirmed.
 *
 * Mirroring flips whole glyphs on the X axis rather than using U+018E (Ǝ). That
 * codepoint is absent from every monospaced family checked, and it fails in the
 * worst way: Android's Noto Sans *does* carry it, so a literal Ǝ silently falls
 * back to a proportional Noto glyph 9% narrower than the surrounding monospace
 * — broken on device, perfect in Figma.
 *
 * Once the display face is confirmed, consider converting this to a
 * VectorDrawable with the letterforms outlined. A logo is artwork, not text, and
 * outlining removes the font dependency from the one element that must never
 * render wrong.
 *
 * @param mirrored indices into [text] to flip. Defaults to all three Es.
 */
@Composable
fun Wordmark(
    modifier: Modifier = Modifier,
    text: String = "PERSPECT1VE",
    mirrored: Set<Int> = setOf(1, 5, 10),
    style: TextStyle = MaterialTheme.typography.displaySmall,
    tracking: Dp = 5.dp,
    color: Color = MaterialTheme.colorScheme.onBackground,
    boxed: Boolean = true,
) {
    // Tracking is carried by the Row, so the style's own letterSpacing would
    // double-count it. Line height is collapsed to the glyph size: Space Mono's
    // 1.481em default line box would otherwise pad the rule box with dead air.
    val glyph = style.copy(
        color = color,
        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
        lineHeight = style.fontSize,
    )

    val frame = if (boxed) {
        Modifier
            .border(Space.Hairline, color)
            .padding(horizontal = Space.Md, vertical = Space.Sm + Space.Xs)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(frame)
            .clearAndSetSemantics { contentDescription = "Perspective" },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(tracking),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            text.forEachIndexed { index, ch ->
                Text(
                    text = ch.toString(),
                    style = glyph,
                    modifier = if (index in mirrored) {
                        Modifier.scale(scaleX = -1f, scaleY = 1f)
                    } else {
                        Modifier
                    },
                )
            }
        }
    }
}
