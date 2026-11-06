package com.perspective.app.ui.product

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import com.perspective.app.data.Bag
import com.perspective.app.data.Product
import com.perspective.app.data.SizeOption
import com.perspective.app.data.asPrice
import com.perspective.app.ui.components.Accordion
import com.perspective.app.ui.components.FrameCounter
import com.perspective.app.ui.productImage
import com.perspective.app.ui.components.ImagePlate
import com.perspective.app.ui.components.PerspectiveButton
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Ratio
import com.perspective.app.ui.theme.Space

/**
 * How long ADDED holds on the CTA before the control hands itself back.
 *
 * A dwell, not an animation: for most of its length nothing is moving, so the
 * 120-360ms band does not govern this figure — it governs the crossfade at
 * either end of it. That is why this is a bare number rather than a [Motion]
 * token, and `OnboardingScreen`'s frame hold draws the same line from the
 * other side.
 *
 * The arithmetic, from the press, with the label crossfade at [Motion.Quick]
 * (240ms) in both directions: 240 fading in, 760 at full opacity, 240 fading
 * out. The word is on the control for 1240ms and holding still for 760 of
 * them, and it is the 760 this figure is chosen for.
 *
 * At [Motion.Standard] that middle number was 80ms. The word reached full
 * opacity and immediately began leaving again — a flicker rather than anything
 * anyone read, and a confirmation nobody reads is not one. 760ms is also long
 * enough to survive the eye going to the bag count in the chrome, which
 * increments on the same frame, and coming back.
 *
 * Nothing is blocked while it runs, which is what lets it be this long: the
 * control stays pressable, and pressing it again restarts the dwell rather
 * than queueing behind it.
 */
private const val ConfirmationMs = 1000

/**
 * Product detail.
 *
 * Order is measured, not invented: full-bleed image pager first, then name and
 * price on ONE row — name at the start, price at the end — then colourway, then
 * size inline, then the CTA, then everything else folded into accordions.
 *
 * Note what is deliberately absent: no thumbnail strip under the pager, no
 * rating, no review count, no "only N left", no badge, no wishlist heart.
 *
 * The CTA is the one press on this screen that changes anything, and what it
 * changes is [Bag] — read and written here rather than hoisted into a callback.
 * The bag is a singleton of snapshot state, so a parameter would only be this
 * screen asking its caller to call the same object back, and every caller would
 * write the same line.
 */
@Composable
fun ProductDetailScreen(
    product: Product,
    modifier: Modifier = Modifier,
) {
    // Saveable now that the choice leads to a purchase: rotating used to put
    // the control back to SELECT A SIZE and quietly undo the decision.
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val pager = rememberPagerState { product.imageCount }

    // The size the confirmation belongs to, and a count of adds. The size is
    // held rather than a bare flag because the confirmation is about what went
    // into the bag, not about the button: choose a different size and ADDED is
    // no longer true of what is selected, so the label goes back without
    // waiting for the beat to end. The count is what lets a second add of the
    // SAME size restart the beat -- the size alone does not change, so an
    // effect keyed on it would ride out the first press's timer.
    var confirming by remember { mutableStateOf<String?>(null) }
    var adds by remember { mutableIntStateOf(0) }

    // Nothing reads [beat], and only its duration is load-bearing -- hence no
    // easing, which would be shaping a number no frame consumes. It is a pause
    // with the platform's animation scale applied to it, and that is the one
    // thing `delay` cannot be: the defect BrandMoment and OnboardingScreen both
    // document from the other side, where with animations off the tweens snap
    // while a schedule keeps its full length. A delayed reset would strand
    // ADDED on the button for the best part of a second after the crossfade
    // that gives it meaning had stopped happening -- and the longer the dwell,
    // the longer that stranding, so lengthening it is exactly the change that
    // would have made a `delay` worse. Driven this way the no-animation case
    // collapses to nothing, which is the right reading rather than a degraded
    // one: one frame of a changed word is a glitch, not a confirmation, and the
    // count in the chrome is what actually records that the bag took it.
    val beat = remember { Animatable(0f) }
    LaunchedEffect(adds) {
        if (adds == 0) return@LaunchedEffect
        beat.snapTo(0f)
        beat.animateTo(1f, tween(ConfirmationMs))
        confirming = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth()) { page ->
                // Only page 0 is the shared element -- it is the image the tile
                // showed. Marking every page would give the key several matches
                // and the transition would pick one arbitrarily.
                ImagePlate(
                    model = product.images[page],
                    ratio = Ratio.Product,
                    modifier = if (page == 0) Modifier.productImage(product.id) else Modifier,
                )
            }
            FrameCounter(current = pager.currentPage + 1, total = product.imageCount)
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
            // ADDED is a beat the control passes through, not a state it
            // settles into, and it does not disable the button on the way. The
            // bag takes another of the same size as a second piece, so the
            // control has to come back and say it can be pressed again; parking
            // on ADDED would make the CTA a readout of the bag, which is the
            // bag's own screen's job.
            PerspectiveButton(
                label = when {
                    selected == null -> "SELECT A SIZE"
                    confirming == selected -> "ADDED"
                    else -> "ADD TO BAG"
                },
                onClick = {
                    selected?.let { size ->
                        // Only confirms what actually went in. Bag.add stops at
                        // nine of one line and says so; saying ADDED anyway
                        // would be the button claiming a press it dropped, and
                        // the chrome count does not move to contradict it.
                        if (Bag.add(product, size)) {
                            confirming = size
                            adds++
                        }
                    }
                },
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
            // The rule fades in rather than snapping. It explains WHICH size the
            // tap landed on -- a purpose -- and 200ms is short enough to read as
            // responsiveness rather than as an effect.
            val rule by animateColorAsState(
                targetValue = when {
                    isSelected -> ink
                    else -> Color.Transparent
                },
                animationSpec = Motion.quick(),
                label = "sizeRule",
            )
            Box(
                modifier = Modifier
                    .border(Space.Hairline, rule)
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
