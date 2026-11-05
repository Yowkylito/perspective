package com.perspective.app.ui.bag

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.perspective.app.data.BagLine
import com.perspective.app.data.SampleBag
import com.perspective.app.data.asPrice
import com.perspective.app.ui.components.ImagePlate
import com.perspective.app.ui.components.PerspectiveButton
import com.perspective.app.ui.theme.Ratio
import com.perspective.app.ui.theme.Space

/** Thumbnail width. Held to [Ratio.Product] so a line echoes a tile. */
private val ThumbWidth = 88.dp

/**
 * The bag.
 *
 * Laid out after a mainstream storefront's cart — thumbnail, description, size
 * and quantity on the left, price on the right, totals and a call to action
 * beneath — because that arrangement is genuinely solved and nothing is gained
 * by inventing a new one for the screen where people are counting their money.
 *
 * **What was taken out rather than added.** The reference carries a sale notice
 * at the top, a struck-through original price on every line, and a stock
 * warning under each item. All three are the urgency copy the brand rules rule
 * out by name, and a cart is where that pressure is most obviously applied, so
 * it is where leaving it out says the most. Price is set at body size and
 * regular weight here as everywhere else, including the total.
 *
 * **The call to action is the outlined rectangle, not a filled pill.** The
 * reference fills it black and rounds it fully. The product detail already
 * established the square outlined control, and the one button in the app that
 * takes money is not the place to introduce a second kind.
 */
@Composable
fun BagScreen(modifier: Modifier = Modifier) {
    val lines = SampleBag.lines

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(Space.Lg))
        Column(modifier = Modifier.padding(horizontal = Space.Gutter)) {
            Text(text = "BAG", style = MaterialTheme.typography.titleLarge)
            Text(
                text = if (lines.size == 1) "1 item" else "${lines.size} items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Space.Xs),
            )
        }
        Spacer(Modifier.height(Space.Xl))

        lines.forEach { line ->
            BagRow(line)
            HorizontalDivider(
                thickness = Space.Hairline,
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = Space.Gutter),
            )
        }

        Spacer(Modifier.height(Space.Xl))
        Column(modifier = Modifier.padding(horizontal = Space.Gutter)) {
            TotalRow("Subtotal", SampleBag.subtotal.asPrice())
            Spacer(Modifier.height(Space.Sm))
            TotalRow("Shipping", "Calculated at checkout")
            Spacer(Modifier.height(Space.Lg))
            HorizontalDivider(
                thickness = Space.Hairline,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Spacer(Modifier.height(Space.Lg))
            TotalRow("Total", SampleBag.subtotal.asPrice(), emphasis = true)
            Spacer(Modifier.height(Space.Xl))
            PerspectiveButton(
                label = "CHECKOUT",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(Space.Xxl))
    }
}

@Composable
private fun BagRow(line: BagLine) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Space.Gutter, vertical = Space.Md),
    ) {
        Box(modifier = Modifier.width(ThumbWidth)) {
            ImagePlate(model = line.product.images.firstOrNull(), ratio = Ratio.Product)
        }
        Spacer(Modifier.width(Space.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = line.product.name, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(Space.Xs))
            Text(
                text = line.product.colourway,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.Sm))
            Text(
                // Size and quantity on one line: both are small facts about the
                // same line, and stacking them would give each the weight of a
                // heading.
                text = "Size ${line.size}    Qty ${line.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(Space.Md))
        Text(
            text = (line.product.price * line.quantity).asPrice(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TotalRow(label: String, value: String, emphasis: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = if (emphasis) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodyMedium,
            color = if (emphasis) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = if (emphasis) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodyMedium,
            color = if (emphasis) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
