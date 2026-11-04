package com.perspective.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import com.perspective.app.ui.theme.Ratio

/**
 * Stands in for product photography until real assets exist.
 *
 * It reserves the exact final geometry, so swapping in an AsyncImage later
 * causes no layout shift - the ratio is fixed here, not inferred from the
 * bitmap. Flat fill, no shimmer: a loading animation on every tile of a wall is
 * noise, and the grid already reserves its space.
 *
 * [tone] exists ONLY because the wall runs a zero horizontal gutter. Two
 * identical flat plates butted together read as one continuous block, which
 * makes the scaffold impossible to review. Real photography separates itself.
 * Delete this parameter the moment images land - it is a reviewing aid, not a
 * design decision, and alternating tones is not part of the system.
 */
@Composable
fun ImagePlate(
    modifier: Modifier = Modifier,
    ratio: Float = Ratio.Product,
    tone: Int = 0,
) {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val fill = if (tone % 2 == 0) {
        base
    } else {
        lerp(base, MaterialTheme.colorScheme.background, 0.45f)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .background(fill),
    )
}
