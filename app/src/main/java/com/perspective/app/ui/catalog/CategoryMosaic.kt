package com.perspective.app.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.perspective.app.data.Category
import com.perspective.app.ui.components.ImagePlate
import com.perspective.app.ui.theme.Ink
import com.perspective.app.ui.theme.Ivory
import com.perspective.app.ui.theme.Space

/**
 * Landscape, 3:2 — the reference's proportion, and the point of a mosaic.
 *
 * Deliberately not [com.perspective.app.ui.theme.Ratio.Product]: the product
 * ratio belongs to products, and giving a category the same shape would say
 * these tiles are more merchandise when the first thing they have to say is
 * that they are doors. Landscape also puts the whole category set on screen at
 * once, which is what a map is for.
 */
private const val TileRatio = 3f / 2f

/**
 * A way into each category, as pictures rather than a list of words.
 *
 * The tabs in the header already filter the wall; this does the same job for
 * someone who arrived without a destination and is looking rather than
 * searching. The two are not redundant — a tab is for someone who knows the
 * word, a tile is for someone who does not yet.
 *
 * **The label sits on the image here, and under it on a product tile.** That is
 * not an inconsistency. On a tile the label is data about a thing for sale —
 * name, price — and it belongs on the ground where it can be read at a glance
 * down a column. Here the label IS the thing; the picture is only its surface,
 * so putting a word anywhere else would leave the tile meaningless. It gets the
 * same scrim the pager counter gets, and for the same reason: photography
 * carries no contrast guarantee.
 */
@Composable
fun CategoryMosaic(
    tiles: List<Pair<Category, String>>,
    onSelect: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        tiles.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { (category, image) ->
                    CategoryTile(
                        category = category,
                        image = image,
                        onClick = { onSelect(category) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: Category,
    image: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        ImagePlate(model = image, ratio = TileRatio)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        1f to Ink.copy(alpha = 0.45f),
                    ),
                )
                .padding(top = 56.dp),
        ) {
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall,
                color = Ivory,
                modifier = Modifier.padding(Space.BleedLabelInset),
            )
        }
    }
}
