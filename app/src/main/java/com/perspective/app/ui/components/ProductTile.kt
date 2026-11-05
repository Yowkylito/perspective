package com.perspective.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.perspective.app.data.Product
import com.perspective.app.ui.productImage
import com.perspective.app.data.asPrice
import com.perspective.app.ui.theme.Space

/**
 * One product in the wall.
 *
 * No card, no border, no shadow, no badge, no wishlist control. The tile is an
 * image and two lines of text on the ground — anything else is chrome, and
 * chrome is what separates a boutique wall from a big-box one.
 *
 * The price is the SAME size as the name and a lighter weight. Setting it larger
 * or bolder is a discount-retail reflex; the reference storefronts either match
 * the two exactly or make the price the quieter of the pair.
 */
@Composable
fun ProductTile(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick, role = Role.Button)
            .clearAndSetSemantics {
                contentDescription = "${product.name}, ${product.price.asPrice()}"
            },
    ) {
        ImagePlate(
            model = product.images.firstOrNull(),
            modifier = Modifier.productImage(product.id),
        )
        Spacer(Modifier.height(Space.LabelGap))
        Column(modifier = Modifier.padding(horizontal = Space.BleedLabelInset)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Space.LabelLineGap))
            Text(
                text = product.price.asPrice(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
