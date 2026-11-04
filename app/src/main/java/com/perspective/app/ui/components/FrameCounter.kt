package com.perspective.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.perspective.app.ui.theme.Ink
import com.perspective.app.ui.theme.Ivory
import com.perspective.app.ui.theme.Space

/** Covers the counter and its inset; short enough to stay unread as an element. */
private val ScrimHeight = 96.dp

/**
 * An "n/N" counter over a full-bleed image, with the scrim that makes it
 * readable.
 *
 * "n/N", not a dot row: it states the count exactly and stays legible however
 * many frames there are.
 *
 * The scrim is the one piece of decoration-looking chrome in the app, and it
 * earns its place on legibility — the named purpose, not "it looks expensive".
 * Against the old flat placeholder plate a theme colour was readable; against
 * photography there is no contrast guarantee at all, and a pale studio floor
 * swallowed it entirely on device.
 *
 * The colour is fixed [Ivory] in both modes rather than `onSurfaceVariant`: it
 * sits on an image, not on a themed surface, so tracking the theme would be
 * tracking the wrong background.
 */
@Composable
fun BoxScope.FrameCounter(current: Int, total: Int) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .height(ScrimHeight)
            .clearAndSetSemantics { }
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    1f to Ink.copy(alpha = 0.45f),
                ),
            ),
    )
    Text(
        text = "$current/$total",
        style = MaterialTheme.typography.labelSmall,
        color = Ivory,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(Space.Md),
    )
}
