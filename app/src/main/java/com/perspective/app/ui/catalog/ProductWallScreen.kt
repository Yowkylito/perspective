package com.perspective.app.ui.catalog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.perspective.app.data.Product
import com.perspective.app.data.SampleCatalogue
import com.perspective.app.ui.components.ProductTile
import com.perspective.app.ui.theme.Space

/**
 * The product wall.
 *
 * Two fixed columns with NO horizontal gutter and a 40dp row gap — the ratio
 * between those two numbers is what makes rows read as discrete objects rather
 * than a continuous mass. Images run full-bleed to the screen edge; only the
 * label is inset. Do not add a density switcher: column count is the same on
 * every storefront measured, premium or not, but the switcher only appears on
 * the mainstream ones.
 */
@Composable
fun ProductWallScreen(
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "ALL PIECES",
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Space.Xxl),
        horizontalArrangement = Arrangement.spacedBy(Space.GridGapHorizontal),
        verticalArrangement = Arrangement.spacedBy(Space.GridGapVertical),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier.padding(
                    start = Space.Gutter,
                    end = Space.Gutter,
                    top = Space.Lg,
                    bottom = Space.Xl,
                ),
            ) {
                // Screen title sits at body size, not above it.
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${SampleCatalogue.items.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Space.Xs),
                )
            }
        }

        itemsIndexed(SampleCatalogue.items, key = { _, it -> it.id }) { index, product ->
            ProductTile(
                product = product,
                onClick = { onProductClick(product) },
                tone = index,
            )
        }
    }
}
