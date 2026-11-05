package com.perspective.app.ui.bag

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.perspective.app.data.Bag
import com.perspective.app.data.BagLine
import com.perspective.app.data.Session
import com.perspective.app.data.asPrice
import com.perspective.app.ui.components.ImagePlate
import com.perspective.app.ui.components.PerspectiveButton
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Ratio
import com.perspective.app.ui.theme.Space

/** Thumbnail width. Held to [Ratio.Product] so a line echoes a tile. */
private val ThumbWidth = 88.dp

/**
 * The ruled square of one step control.
 *
 * The touch target is a [Space.Sm] ring wider all round — 44dp against a 28dp
 * mark — which is the only reason the padding in [Step] sits inside the
 * clickable rather than outside it. A rule big enough to be a comfortable
 * target would be a box the size of the price next to it.
 */
private val StepRule = 28.dp

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
 * commits a purchase is not the place to introduce a second kind. It takes no
 * money: there is no payment step, and CLAUDE.md records why.
 *
 * **It reads and writes [Bag] directly rather than being handed its lines.**
 * [Bag] is a singleton over snapshot state, so reading it in composition is
 * already the subscription; lifting it into a parameter would add a hop without
 * adding a seam, since there is no second source of lines and no other screen
 * that owns them. What IS hoisted is where the controls lead: this screen knows
 * that something happens next, never what.
 */
@Composable
fun BagScreen(
    onCheckout: () -> Unit,
    onBrowse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lines = Bag.lines
    val pieces = Bag.count

    // Lines already out of the bag but still leaving the screen.
    //
    // The removal is committed to the model on the PRESS, not when the
    // transition settles. Deferring it left the totals, the piece count and the
    // checkout control describing a line that had been taken out, and a
    // checkout inside that window placed an order containing it and then
    // emptied the bag over the evidence. A resurrected line is at least
    // visible; a silently bought one is not.
    //
    // So the row goes on rendering from this copy for as long as the exit
    // needs it, and nothing anyone can act on is reading from here.
    // Each holds the row it left from, so it shrinks away where it sat rather
    // than jumping to the end of the list first.
    val departing = remember { mutableStateListOf<Pair<Int, BagLine>>() }
    // Computed, not remembered. Bag.lines is a snapshot list whose identity
    // never changes, so keying a remember on it would hold the first value
    // forever: the totals read Bag directly and stayed right while every row
    // froze at the quantity it was first composed with.
    val rows = buildList {
        addAll(lines)
        departing
            .filterNot { (_, gone) -> lines.any { it.key == gone.key } }
            .sortedBy { it.first }
            .forEach { (at, gone) -> add(at.coerceIn(0, size), gone) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(Space.Lg))
        Column(modifier = Modifier.padding(horizontal = Space.Gutter)) {
            Text(text = "BAG", style = MaterialTheme.typography.titleLarge)
            // Pieces, not lines. Two of one shirt is two items, and a bag that
            // said "1 item" while holding two would be counting its own rows
            // rather than the things in it. Nothing to count is said by the
            // empty state in words, so the figure stays away from zero.
            if (pieces > 0) {
                Text(
                    // Pieces, not items: the wall counts products and calls
                    // those items, and a bag holding two of one shirt is not
                    // holding two products. The account history says the same
                    // word for the same reason.
                    text = if (pieces == 1) "1 piece" else "$pieces pieces",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Space.Xs),
                )
            }
        }
        Spacer(Modifier.height(Space.Xl))

        if (rows.isEmpty()) {
            EmptyBag(onBrowse = onBrowse)
        } else {
            rows.forEachIndexed { index, line ->
                // Keyed on the line rather than on its position, so the state
                // below follows the line. Without it a removal hands a
                // departing row's half-finished transition to whichever line
                // slid up into its slot.
                key(line.key) {
                    // Composed visible and only ever leaving, so the list is
                    // seen to close over the line rather than jumping — which
                    // is the whole of what this animation explains. Waiting on
                    // isIdle rather than on a duration leaves the system
                    // animation scale authoritative: with animations off the
                    // transition settles at once and the row is gone at once.
                    val presence = remember { MutableTransitionState(true) }
                    val gone = departing.any { it.second.key == line.key }
                    LaunchedEffect(gone) { if (gone) presence.targetState = false }
                    LaunchedEffect(presence.isIdle, presence.currentState) {
                        if (presence.isIdle && !presence.currentState) {
                            departing.removeAll { it.second.key == line.key }
                        }
                    }
                    // Only ever touches the local copy. Nothing here can put a
                    // line back or take one out, so navigating away mid-exit
                    // leaves the bag exactly as the press left it.
                    DisposableEffect(Unit) {
                        onDispose { departing.removeAll { it.second.key == line.key } }
                    }

                    AnimatedVisibility(
                        visibleState = presence,
                        // Nothing to explain on arrival: a line gets into the
                        // bag from another screen, so here it has simply always
                        // been there.
                        enter = EnterTransition.None,
                        exit = shrinkVertically(
                            animationSpec = Motion.leaving(),
                            shrinkTowards = Alignment.Top,
                        ) + fadeOut(animationSpec = Motion.leaving()),
                    ) {
                        // Row and rule leave together, or the screen keeps a
                        // divider belonging to nothing.
                        Column {
                            BagRow(
                                line = line,
                                onNudge = { Bag.nudge(line.key, it) },
                                onRemove = {
                                    departing.add(index to line)
                                    Bag.remove(line.key)
                                },
                            )
                            HorizontalDivider(
                                thickness = Space.Hairline,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(horizontal = Space.Gutter),
                            )
                        }
                    }
                }
            }

        }

        // Bound to the bag rather than to what is on screen, so it leaves the
        // moment the last line does rather than quoting a figure for a bag that
        // no longer holds anything.
        if (lines.isNotEmpty()) {
            Spacer(Modifier.height(Space.Xl))
            Column(modifier = Modifier.padding(horizontal = Space.Gutter)) {
                // One figure, because there is only one. A "Shipping —
                // calculated at checkout" line was carried over from the
                // version where this button did nothing; there is no checkout
                // step to calculate anything at, nothing is added, and the
                // total sitting equal to the subtotal said so on the same
                // screen. A subtotal is only a subtotal of something.
                TotalRow("Total", Bag.subtotal.asPrice(), emphasis = true)
                Spacer(Modifier.height(Space.Xl))
                // The label says where the press actually goes. Signed out, the
                // next screen is a sign-in, and a button reading CHECKOUT would
                // be naming the screen after that one. Which screen that is
                // remains the app's business; this only has to be honest about
                // it.
                PerspectiveButton(
                    label = if (Session.signedIn) "CHECKOUT" else "SIGN IN TO CHECKOUT",
                    onClick = onCheckout,
                )
            }
        }
        Spacer(Modifier.height(Space.Xxl))
    }
}

/**
 * The empty bag.
 *
 * One line that says what is true, and a control that goes where the clothes
 * are. No illustration, no second sentence talking the person back into the
 * shop, and no recently-viewed rail dressed up as a recommendation — an empty
 * bag is a fact about the bag, not an opening to sell into. The copy sits in
 * the same register as the wall's own empty result.
 *
 * **The checkout control is absent here rather than disabled.** There is
 * nothing to check out, so a greyed button would be describing an action that
 * does not exist yet; the one control on screen is the one that can be taken.
 * THE SHOP is the onboarding screen's own name for where it leads.
 */
@Composable
private fun EmptyBag(onBrowse: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = Space.Gutter)) {
        Text(
            text = "Nothing in the bag.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Space.Xl))
        PerspectiveButton(
            label = "THE SHOP",
            onClick = onBrowse,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BagRow(
    line: BagLine,
    onNudge: (Int) -> Unit,
    onRemove: () -> Unit,
) {
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
            // Name at the start and price at the end of one row — the product
            // detail's own arrangement, so a line in the bag reads like the
            // page it came from. The price carries the quantity with it, since
            // that is the figure the subtotal underneath is built from.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = line.product.name,
                    style = MaterialTheme.typography.titleSmall,
                    // Weighted so a long name wraps instead of pushing the
                    // price off the edge: this column is a thumbnail narrower
                    // than the detail page's full measure.
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(Space.Md))
                Text(
                    text = (line.product.price * line.quantity).asPrice(),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(Space.Xs))
            Text(
                text = line.product.colourway,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.Sm))
            // Size stays a statement and quantity became a control, which is
            // the real difference between the two facts: the size was settled
            // on the product page and cannot be changed from here, while the
            // quantity is one of the two things this screen exists to let
            // someone change.
            Text(
                text = "Size ${line.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.Sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuantityStepper(quantity = line.quantity, onNudge = onNudge)
                // The word, not a bin glyph — the same construction as the
                // footer's own text controls, and it keeps the app free of icon
                // assets. Vertical padding only, so its right edge still lines
                // up with the price above it.
                Text(
                    text = "REMOVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onRemove)
                        .padding(vertical = Space.Sm),
                )
            }
        }
    }
}

/**
 * Quantity, built from type.
 *
 * The app ships no icon assets, so the control is a minus and a plus set in the
 * display face — the same pair, at the same style, the accordion uses for open
 * and closed. A stepper is the place a storefront most reliably reaches for two
 * glyph buttons; this one says the same thing in the brand's own alphabet, and
 * both characters are in Courier Prime's charset, which is the check anything
 * non-alphabetic has to pass here.
 *
 * **`−` stops at one instead of deleting the line.** `Bag.setQuantity` would
 * remove it below one, but a decrement that silently becomes a deletion is a
 * second action wearing the first one's control. REMOVE is what removes, and it
 * is the only thing that does.
 */
@Composable
private fun QuantityStepper(quantity: Int, onNudge: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Step(
            glyph = "−",
            // Not "Remove one": REMOVE is a different control on the same row,
            // and a screen reader hearing two removes that do different things
            // has been told less than nothing.
            label = "One fewer",
            enabled = quantity > 1,
            onClick = { onNudge(-1) },
        )
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            // A bare figure between two glyphs carries its meaning entirely in
            // the layout, and the layout is the one thing a screen reader does
            // not get. The number says the word the arrangement leaves out.
            modifier = Modifier
                .widthIn(min = StepRule)
                .semantics { contentDescription = "Quantity $quantity" },
        )
        Step(
            glyph = "+",
            label = "One more",
            enabled = quantity < Bag.MaxQuantity,
            onClick = { onNudge(+1) },
        )
    }
}

@Composable
private fun Step(
    glyph: String,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            // Padding inside the clickable: the target is the 44dp square, the
            // rule is the 28dp one. See [StepRule].
            .clickable(
                enabled = enabled,
                // Spoken, the glyph alone is punctuation. The action carries
                // the words, the way the button's uppercase label stays an
                // ordinary string for the same reason.
                onClickLabel = label,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(Space.Sm)
            // A minimum rather than a fixed size: the glyph is set in sp and
            // grows with the system font scale, and a hard 28dp clips it from
            // about 1.3x — which is stock Android's largest setting, before
            // the accessibility sizes are reached at all.
            .defaultMinSize(minWidth = StepRule, minHeight = StepRule)
            // The rule holds whatever the state; only the glyph greys. A
            // control that disappears when it is inactive makes the row
            // rearrange under the thumb, which is the same objection that keeps
            // unavailable sizes on the product page rather than removing them.
            .border(Space.Hairline, scheme.outlineVariant)
            // clickable(enabled = false) takes the click semantics AND the
            // label with it, leaving a lone U+2212 that reads as punctuation.
            // Unavailable sizes on the product page say this; so does this.
            .semantics { if (!enabled) disabled() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) scheme.onBackground else scheme.outline,
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
        // The figure stays at body size and regular weight even on the
        // emphasised row. The price rule holds for the total as it does for a
        // line and for the wall, so what carries the emphasis is the word and
        // the ink — never the number, which is the one place a cart is most
        // tempted to shout.
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (emphasis) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
