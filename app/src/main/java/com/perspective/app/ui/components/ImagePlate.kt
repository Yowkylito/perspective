package com.perspective.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.perspective.app.ui.theme.Ratio

/**
 * A single product image, held to [Ratio.Product].
 *
 * The ratio is fixed HERE, not inferred from the bitmap, so the grid reserves
 * the final geometry before the decode finishes and nothing shifts when it
 * lands. That is also why there is no shimmer and no crossfade: the space is
 * already correct, so a loading animation would be describing a change the
 * layout never makes - and in a lazy grid it would re-fire on every recycle,
 * which is the same objection that keeps staggered tile entrance out.
 *
 * The flat [MaterialTheme.colorScheme.surfaceVariant] fill behind the image is
 * what shows while the decode is in flight.
 */
@Composable
fun ImagePlate(
    model: Any?,
    modifier: Modifier = Modifier,
    ratio: Float = Ratio.Product,
    contentDescription: String? = null,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
