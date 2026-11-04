package com.perspective.app.ui.product

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import com.perspective.app.data.Product
import com.perspective.app.data.SizeOption
import com.perspective.app.data.asPrice
import com.perspective.app.ui.components.Accordion
import com.perspective.app.ui.components.ImagePlate
import com.perspective.app.ui.components.PerspectiveButton
import com.perspective.app.ui.theme.Ratio
import com.perspective.app.ui.theme.Space

/**
 * Product detail.
 *
 * Order is measured, not invented: full-bleed image pager first, then name and
 * price on ONE row — name at the start, price at the end — then colourway, then
 * size inline, then the CTA, then everything else folded into accordions.
 *
 * Note what is deliberately absent: no thumbnail strip under the pager, no
 * rating, no review count, no "only N left", no badge, no wishlist heart.
 */
@Composable
fun ProductDetailScreen(
    product: Product,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<String?>(null) }
    val pager = rememberPagerState { product.imageCount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth()) {
                ImagePlate(ratio = Ratio.Product)
            }
            // An "n/N" counter, not a dot row: it states the count exactly and
            // stays legible however many images a product has.
            Text(
                text = "${pager.currentPage + 1}/${product.imageCount}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Space.Md),
            )
        }

        Column(modifier = Modifier.padding(horizontal = Space.Gutter)) {
            Spacer(Modifier.height(Space.Lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(text = product.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = product.price.asPrice(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Space.Xs))
            Text(
                text = product.colourway,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Space.Xl))
            Text(text = "SELECT SIZE", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(Space.Md))
            SizeRow(
                sizes = product.sizes,
                selected = selected,
                onSelect = { selected = it },
            )

            Spacer(Modifier.height(Space.Lg))
            PerspectiveButton(
                label = if (selected == null) "SELECT A SIZE" else "ADD TO BAG",
                onClick = {},
                enabled = selected != null,
            )

            Spacer(Modifier.height(Space.Xl))
            Accordion(title = "DETAILS") {
                Text(text = product.description, style = MaterialTheme.typography.bodyMedium)
            }
            Accordion(title = "SIZE & FIT") {
                Text(
                    text = "Model is 183cm and wears a size M. Fits true to size.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Accordion(title = "SHIPPING & RETURNS") {
                Text(
                    text = "Dispatched within two working days. Returns accepted for 30 days from delivery.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(Space.Xxl))
        }
    }
}

/**
 * Sizes inline, never hidden behind a sheet.
 *
 * Unavailable sizes are struck through AND greyed — two signals, not one — and
 * they stay present rather than being removed, so the customer can see the piece
 * exists in that size and is simply gone. They remain in the semantics tree
 * marked disabled rather than being stripped out, so a screen-reader user learns
 * the same thing a sighted one does.
 */
@Composable
private fun SizeRow(
    sizes: List<SizeOption>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Space.Sm),
    ) {
        sizes.forEach { size ->
            val isSelected = size.label == selected
            val ink = MaterialTheme.colorScheme.onBackground
            Box(
                modifier = Modifier
                    .then(
                        if (isSelected) Modifier.border(Space.Hairline, ink) else Modifier,
                    )
                    .then(
                        if (size.available) {
                            Modifier.clickable { onSelect(size.label) }
                        } else {
                            Modifier.semantics { disabled() }
                        },
                    )
                    .padding(horizontal = Space.Sm, vertical = Space.Xs),
            ) {
                Text(
                    text = size.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (size.available) ink else MaterialTheme.colorScheme.outline,
                    textDecoration = if (size.available) null else TextDecoration.LineThrough,
                )
            }
        }
    }
}
