package com.perspective.app.ui.catalog

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.perspective.app.data.Bag
import com.perspective.app.ui.brand.Wordmark
import com.perspective.app.ui.brandWordmark
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Space

/** Bar heights. The wall pads its content by their sum so nothing starts underneath. */
val HeaderBarHeight = 56.dp
val HeaderTabsHeight = 44.dp

/**
 * The slot the bag label sits in, whatever it currently says.
 *
 * The search field takes the remaining width, so without a reserved slot
 * putting the first thing in the bag visibly shrinks the search box and taking
 * the last one out grows it back — the count is worth having, a search field
 * that twitches when you shop is not. Courier Prime advances 0.600em with
 * 0.10em of tracking at labelSmall's 12sp, so "BAG (9)" measures about 59dp.
 */
private val BagSlot = 60.dp


/**
 * How much of the ground the header keeps once content is passing beneath it.
 *
 * Translucency, NOT a blur. Compose 1.9.3 has no backdrop-blur API -- `blur()`
 * blurs a composable's own content, not what is behind it -- and the only way
 * to get a true frosted pane here would be to record the wall into a second
 * graphics layer and draw it again blurred under the bar, which means drawing a
 * photographic grid twice every frame. That is a real scroll cost for an effect
 * that, on a flat single-colour ground, is almost indistinguishable from this.
 *
 * 0.82 is low enough that a plate visibly passes under the bar and high enough
 * that tracked mono labels stay legible over photography.
 */
private const val GlassAlpha = 0.82f

/**
 * The storefront header: brand, search, bag, and the category tabs under them.
 *
 * It is drawn OVER the wall rather than above it, so the grid scrolls beneath —
 * which is the whole reason the glass earns its place. A solid bar explains
 * nothing; a translucent one states that the content did not stop at the bar,
 * it went under it. That is a spatial relationship, which is one of the
 * purposes the motion system accepts.
 *
 * At rest the bar is opaque: there is nothing behind it to show, so showing
 * nothing is correct. The change is tied to scroll position, not to a timer.
 *
 * **Words where the reference used icons.** The pattern this follows sets a
 * magnifier and a cart glyph here. This app does not: the chrome is typographic
 * by rule, so search is a ruled field with a tracked placeholder and the bag is
 * the word. The structure is the same; the vocabulary is this brand's.
 */
@Composable
fun StorefrontHeader(
    query: TextFieldValue,
    onQuery: (TextFieldValue) -> Unit,
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    scrolled: Boolean,
    onBag: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val ground by animateColorAsState(
        targetValue = if (scrolled) scheme.background.copy(alpha = GlassAlpha) else scheme.background,
        animationSpec = Motion.quick(),
        label = "headerGround",
    )
    val hairline by animateFloatAsState(
        targetValue = if (scrolled) 1f else 0f,
        animationSpec = Motion.quick(),
        label = "headerHairline",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ground)
            .statusBarsPadding()
            .drawBehind {
                if (hairline > 0f) {
                    val y = size.height - Space.Hairline.toPx() / 2f
                    drawLine(
                        color = scheme.outlineVariant.copy(alpha = hairline),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = Space.Hairline.toPx(),
                    )
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HeaderBarHeight)
                .padding(horizontal = Space.Gutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The other end of the wordmark's journey. It is the same mark
            // the launch sequence assembled and the onboarding block held; this
            // is simply where it works from once the shop is open.
            Wordmark(
                style = MaterialTheme.typography.labelMedium,
                tracking = 2.dp,
                modifier = Modifier.brandWordmark(),
            )
            Spacer(Modifier.width(Space.Sm + Space.Xs))
            SearchField(
                value = query,
                onValue = onQuery,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(Space.Sm + Space.Xs))
            // The word sits in a reserved slot and the slot is what you
            // press: a bare Text takes its hit area from its own glyphs, which
            // at labelSmall is about 16dp tall against a 48dp minimum.
            Box(
                modifier = Modifier
                    .widthIn(min = BagSlot)
                    .heightIn(min = Space.TouchTarget)
                    .clickable(onClick = onBag, role = Role.Button),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    // The count is the only place adding to the bag is visible
                    // from the catalogue, so without it the press has no
                    // consequence you can see. A parenthetical rather than a
                    // badge: a filled circle with a number in it is the one
                    // piece of mainstream-retail chrome this bar does not
                    // carry, and the brand is typographic -- the word already
                    // says what it is.
                    text = if (Bag.count > 0) "BAG (${Bag.count})" else "BAG",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        CategoryTabs(tabs = tabs, selected = selected, onSelect = onSelect)
    }
}

/**
 * A ruled field, not a pill with a magnifier in it.
 *
 * Square corners and a hairline rule, the same construction as the PDP's call
 * to action — a rounded search bar would be the one rounded thing in an
 * interface that has no radius anywhere else.
 */
@Composable
private fun SearchField(
    value: TextFieldValue,
    onValue: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .height(34.dp)
            .border(Space.Hairline, scheme.outlineVariant)
            .padding(horizontal = Space.Sm + Space.Xs),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.text.isEmpty()) {
            Text(
                text = "SEARCH",
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValue,
            singleLine = true,
            textStyle = LocalTextStyle.current.merge(
                MaterialTheme.typography.labelSmall.copy(color = scheme.onBackground),
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(scheme.onBackground),
            modifier = Modifier
                .fillMaxWidth()
                // The SEARCH placeholder is a sibling node and it disappears
                // the moment anything is typed, so without this the field has
                // no name at all. Same fix the sign-in field already carries.
                .semantics { contentDescription = "Search" },
        )
    }
}

/**
 * The tab row, with a rule under the active one.
 *
 * The indicator animates between tabs rather than cutting, because it is the
 * one thing on screen that says which of several things you are looking at —
 * moving it shows that the same marker went from there to here. It is a rule,
 * not a pill or a filled chip: the brand already has a rule, around the
 * wordmark, and reusing it costs the system nothing new.
 */
@Composable
private fun CategoryTabs(
    tabs: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderTabsHeight)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Space.Gutter),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.Lg),
    ) {
        tabs.forEachIndexed { index, label ->
            val active = index == selected
            val ink by animateColorAsState(
                targetValue = if (active) scheme.onBackground else scheme.onSurfaceVariant,
                animationSpec = Motion.quick(),
                label = "tabInk",
            )
            val underline by animateDpAsState(
                targetValue = if (active) Space.Hairline else 0.dp,
                animationSpec = Motion.quick(),
                label = "tabRule",
            )
            Box(
                modifier = Modifier
                    .height(HeaderTabsHeight)
                    // The underline says which tab is active, and an underline
                    // is not something a screen reader can read. selectable
                    // carries the same fact into the semantics tree.
                    .selectable(
                        selected = active,
                        role = Role.Tab,
                        onClick = { onSelect(index) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = ink)
                if (underline > 0.dp) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = Space.Sm + Space.Xs)
                            .fillMaxWidth()
                            .height(underline)
                            .background(scheme.onBackground),
                    )
                }
            }
        }
    }
}
