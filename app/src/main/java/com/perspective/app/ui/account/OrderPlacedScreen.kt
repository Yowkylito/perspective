package com.perspective.app.ui.account

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
import com.perspective.app.data.Order
import com.perspective.app.data.asPrice
import com.perspective.app.ui.components.ImagePlate
import com.perspective.app.ui.components.PerspectiveButton
import com.perspective.app.ui.theme.Ratio
import com.perspective.app.ui.theme.Space

/**
 * Thumbnail width, held to [Ratio.Product] so a line still echoes a tile.
 *
 * Smaller than the bag's 88dp. A bag line is something still being decided
 * about and the picture is part of the deciding; a placed order is a record,
 * and the picture is only there to confirm which thing it was.
 */
private val ThumbWidth = 64.dp

/**
 * One order, read from either direction.
 *
 * This is the confirmation the checkout lands on, and it is also what the
 * account history opens — so every word on it has to be as true a month later
 * as it is ten seconds after the press. That rules out most of what a
 * confirmation page usually says: no thanks, no "on its way", no estimate that
 * ages into a lie, and above all no claim that a confirmation email has been
 * sent, because nothing sent one.
 *
 * **No tick and no entrance.** The mark would be an icon asset in an app that
 * has none and spells its chrome in words, and would be the first glyph in the
 * interface doing a job a word already does. The entrance would be motion whose
 * only purpose is to look pleased, which is precisely the test the motion
 * system applies and the thing it refuses. Nothing on this screen animates: the
 * order does not change while it is being looked at, so there is no state
 * change to explain.
 *
 * Lines echo the bag's row rather than repeating it — same anatomy, thumbnail
 * then description then price at the end, on a smaller plate and without the
 * colourway. A placed order records what was bought and in which size; the
 * catalogue detail belongs on the page where it was still a choice.
 *
 * **The total is emphasised by its label, not by its figure.** `TOTAL` is set
 * in the display voice as a micro-label, and the number stays at body size and
 * regular weight like every other price here. Setting the one figure that
 * matters most in something heavier is the discount-retail reflex the type
 * rules name by name.
 *
 * The control says `DONE` rather than anything about shopping on: arriving from
 * the history, this is not the start of anything, and a label that is only
 * right half the time is a label that is wrong.
 */
@Composable
fun OrderPlacedScreen(
    order: Order,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rule = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.Gutter),
    ) {
        Spacer(Modifier.height(Space.Lg))
        // The fact first, then the thing to quote if anyone has to be asked
        // about it, then when it happened. In that order because that is the
        // order they are wanted in, whichever door this was reached through.
        Text(text = "ORDER PLACED", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(Space.Lg))
        Text(text = order.reference, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(Space.Xs))
        Text(
            text = order.placed,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Space.Xl))
        order.lines.forEach { line ->
            HorizontalDivider(thickness = Space.Hairline, color = rule)
            OrderedLine(line)
        }
        HorizontalDivider(thickness = Space.Hairline, color = rule)

        Spacer(Modifier.height(Space.Lg))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // titleSmall, not the mono micro-label. Measured from the
            // shipped binaries: Courier Prime caps are 0.580em against
            // Instrument Sans at 0.720, so labelMedium at 13sp sets capitals
            // 7.5dp tall against the figure's 10.1 — the number rendering a
            // third taller than the word beside it, which is the one thing
            // structure rule 5 forbids. The bag's total row already does it
            // this way; these two should not disagree about one row.
            Text(text = "TOTAL", style = MaterialTheme.typography.titleSmall)
            Text(text = order.total.asPrice(), style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(Space.Xl))
        PerspectiveButton(label = "DONE", onClick = onContinue)

        Spacer(Modifier.height(Space.Xxl))
    }
}

/**
 * One line of a placed order.
 *
 * Size and quantity share a metadata line, the same idiom the bag uses, so the
 * two screens read as one account of the same purchase. The price shown is the
 * line price — unit price times quantity — because that is the figure the total
 * underneath is made of.
 */
@Composable
private fun OrderedLine(line: BagLine) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Space.Md),
    ) {
        Box(modifier = Modifier.width(ThumbWidth)) {
            ImagePlate(model = line.product.images.firstOrNull(), ratio = Ratio.Product)
        }
        Spacer(Modifier.width(Space.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = line.product.name, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(Space.Sm))
            Text(
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
