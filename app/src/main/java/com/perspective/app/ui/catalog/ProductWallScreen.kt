package com.perspective.app.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.perspective.app.data.MockImages
import com.perspective.app.data.Product
import com.perspective.app.data.SampleCatalogue
import com.perspective.app.ui.BottomNavSpace
import com.perspective.app.ui.brand.Wordmark
import com.perspective.app.ui.components.ProductTile
import com.perspective.app.ui.theme.Space

/** Products shown before the editorial block. Must stay even — see the call site. */
private const val CollageAfter = 4

/**
 * Home: the editorial, then the whole catalogue, with nothing over it.
 *
 * **This is the screen without chrome, and that is the point of it.** The
 * storefront header and the campaign banner belong to [ShopScreen], which is
 * where the app lands and where the crossings from onboarding come to rest.
 * Home is the other half of that split: no bar, no tabs, no search field, no
 * filter — one page that opens on the campaign and keeps going until the
 * catalogue runs out. Giving it a header again would put two headers in the
 * app and a second category control on a tab that has nothing to narrow.
 *
 * Two fixed columns with NO horizontal gutter and a 40dp row gap — the ratio
 * between those two numbers is what makes rows read as discrete objects rather
 * than a continuous mass. Images run full-bleed to the screen edge; only the
 * label is inset. Do not add a density switcher: column count is the same on
 * every storefront measured, premium or not, but the switcher only appears on
 * the mainstream ones.
 *
 * **The status-bar inset is real padding here, not content padding.** The shop
 * lets plates travel under the clock because its own header is over them
 * holding the contrast. This page has no chrome at all, and a photograph
 * passing under the status bar is a legibility accident waiting for the wrong
 * frame. The two screens swapped these treatments together; they are the same
 * decision seen from each end.
 *
 * **[gridState] is hoisted, and that is not a style preference.** The bar pops
 * this screen's entry when you leave the tab, so anything remembered in here
 * goes with it — including the scroll position, on the longest page in the app.
 * The caller holds it above the NavHost instead; `PerspectiveApp` records why
 * that is where it has to live. Default it and this screen still works, which
 * is what a preview and a test want; the app does not default it.
 */
@Composable
fun ProductWallScreen(
    onProductClick: (Product) -> Unit,
    onLicences: () -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    val items = SampleCatalogue.items

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            // Clears the floating bar as well as the page's own end
            // margin, or the footer sits under the pill.
            bottom = Space.Xxl + BottomNavSpace,
        ),
        horizontalArrangement = Arrangement.spacedBy(Space.GridGapHorizontal),
        verticalArrangement = Arrangement.spacedBy(Space.GridGapVertical),
    ) {
        // The page's own title, and the only thing that names this tab now the
        // bar is gone from over it.
        item(span = { GridItemSpan(maxLineSpan) }, key = "featured") {
            Column {
                SectionHeading("FEATURED", top = Space.Lg)
                EditorialCards(
                    cards = MockImages.featured,
                    onCard = null,
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }, key = "title") {
            Column(
                modifier = Modifier.padding(
                    start = Space.Gutter,
                    end = Space.Gutter,
                    top = Space.Lg,
                    bottom = Space.Xl,
                ),
            ) {
                Text(
                    text = "ALL PIECES",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = if (items.size == 1) "1 item" else "${items.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Space.Xs),
                )
            }
        }

        items(items.take(CollageAfter), key = { it.id }) { product ->
            ProductTile(product = product, onClick = { onProductClick(product) })
        }

        if (items.size > CollageAfter) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "collage") {
                EditorialCollage(frames = MockImages.collage)
            }
        }

        items(items.drop(CollageAfter), key = { it.id }) { product ->
            ProductTile(product = product, onClick = { onProductClick(product) })
        }

        // The storefront's only non-catalogue destination, and it is here
        // because this is the page that ends. The shop narrows and re-widens;
        // this one runs out, which is where the reference storefronts put a
        // footer. The account used to sit beside it, back when the chrome was
        // full. It is in the bottom bar now, and two ways to the same screen
        // on one page is one too many.
        item(span = { GridItemSpan(maxLineSpan) }, key = "footer") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Space.Xxl, start = Space.Gutter, end = Space.Gutter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HorizontalDivider(
                    thickness = Space.Hairline,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                Spacer(Modifier.height(Space.Xl))
                Wordmark(style = MaterialTheme.typography.labelSmall, tracking = 2.dp)
                Spacer(Modifier.height(Space.Lg))
                FooterWord(text = "LICENCES", onClick = onLicences)
            }
        }
    }
}

/**
 * Section heading for the front page.
 *
 * Body size with no tracking, per the structure rules: screen titles here sit
 * level with the copy rather than above it, and letter-spaced small caps are
 * reserved for micro-labels like SELECT SIZE.
 */
@Composable
private fun SectionHeading(text: String, top: androidx.compose.ui.unit.Dp = Space.Xl) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(start = Space.Gutter, end = Space.Gutter, top = top, bottom = Space.Md),
    )
}

/**
 * A footer word, pressable.
 *
 * Same reason as the chrome's words: a bare clickable Text is about 16dp of hit
 * area against the 48dp Android asks for, and announces without a role.
 */
@Composable
private fun FooterWord(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = Space.TouchTarget)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
