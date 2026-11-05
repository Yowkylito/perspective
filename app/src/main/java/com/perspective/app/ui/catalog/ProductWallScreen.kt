package com.perspective.app.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import com.perspective.app.data.Category
import kotlinx.coroutines.launch
import com.perspective.app.data.MockImages
import com.perspective.app.data.Product
import com.perspective.app.data.SampleCatalogue
import com.perspective.app.ui.brand.Wordmark
import com.perspective.app.ui.components.ProductTile
import com.perspective.app.ui.theme.Space

/** Products shown before the editorial block. Must stay even — see the call site. */
private const val CollageAfter = 4

/**
 * The product wall, and the chrome that sits over it.
 *
 * Two fixed columns with NO horizontal gutter and a 40dp row gap — the ratio
 * between those two numbers is what makes rows read as discrete objects rather
 * than a continuous mass. Images run full-bleed to the screen edge; only the
 * label is inset. Do not add a density switcher: column count is the same on
 * every storefront measured, premium or not, but the switcher only appears on
 * the mainstream ones.
 *
 * The header is an overlay rather than a row above the grid, and the grid pads
 * its content by exactly the header's height. That is what lets plates travel
 * under the bar instead of stopping at it, which is the thing the glass is
 * there to show.
 */
@Composable
fun ProductWallScreen(
    onProductClick: (Product) -> Unit,
    onLicences: () -> Unit,
    onAccount: () -> Unit,
    onBag: () -> Unit,
    modifier: Modifier = Modifier,
    initialCampaignFrame: Int = 0,
) {
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    var tab by rememberSaveable { mutableStateOf(0) }
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    // Narrowing the catalogue puts you at the top of the result.
    //
    // Done on the press rather than in an effect keyed on the filter, because
    // an effect also fires when this screen is rebuilt on the way back from a
    // product -- which would throw away the scroll position the grid just
    // restored, every time.
    //
    // Without it, choosing a category removes the banner, the mosaic, the
    // featured cards and the collage, roughly 2,500dp of editorial, while the
    // grid keeps its index and offset: you ask to see Trousers and land near
    // the footer of a list you have never seen the top of.
    val showFromTop: () -> Unit = { scope.launch { grid.scrollToItem(0) } }

    val tabs = remember { listOf(ALL_TAB) + Category.entries.map { it.label } }

    val items = remember(tab, query.text) {
        val byCategory =
            if (tab == 0) SampleCatalogue.items
            else SampleCatalogue.items.filter { it.category.label == tabs[tab] }
        val q = query.text.trim()
        if (q.isEmpty()) byCategory
        else byCategory.filter {
            it.name.contains(q, ignoreCase = true) || it.colourway.contains(q, ignoreCase = true)
        }
    }

    // Editorial runs only on the unfiltered wall. Once someone has narrowed the
    // catalogue they are looking for a thing, not reading a campaign, and a
    // banner between them and the results is the storefront talking over them.
    val editorial = tab == 0 && query.text.isBlank()

    val scrolled by remember {
        derivedStateOf { grid.firstVisibleItemIndex > 0 || grid.firstVisibleItemScrollOffset > 0 }
    }

    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = grid,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusBar + HeaderBarHeight + HeaderTabsHeight,
                bottom = Space.Xxl,
            ),
            horizontalArrangement = Arrangement.spacedBy(Space.GridGapHorizontal),
            verticalArrangement = Arrangement.spacedBy(Space.GridGapVertical),
        ) {
            if (editorial) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "banner") {
                    CollectionBanner(
                        frames = MockImages.banners,
                        captions = MockImages.bannerCaptions,
                        initialFrame = initialCampaignFrame,
                    )
                }
            }

            if (editorial) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "mosaic") {
                    Column {
                        SectionHeading("SHOP BY CATEGORY", top = Space.Xl)
                        CategoryMosaic(
                            tiles = Category.entries.mapIndexed { i, c ->
                                c to MockImages.categoryTiles[i % MockImages.categoryTiles.size]
                            },
                            onSelect = { chosen ->
                                tab = tabs.indexOf(chosen.label).coerceAtLeast(0)
                                showFromTop()
                            },
                        )
                    }
                }
            }

            if (editorial) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "featured") {
                    Column {
                        SectionHeading("FEATURED")
                        EditorialCards(
                            cards = MockImages.featured,
                            onCard = null,
                        )
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }, key = "title") {
                Column(
                    modifier = Modifier.padding(
                        start = Space.Gutter,
                        end = Space.Gutter,
                        top = if (editorial) Space.Lg else Space.Sm,
                        bottom = Space.Xl,
                    ),
                ) {
                    Text(
                        text = if (tab == 0) "ALL PIECES" else tabs[tab],
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

            if (editorial && items.size > CollageAfter) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "collage") {
                    EditorialCollage(frames = MockImages.collage)
                }
            }

            items(items.drop(CollageAfter), key = { it.id }) { product ->
                ProductTile(product = product, onClick = { onProductClick(product) })
            }

            // The storefront's only non-catalogue destination. A footer is
            // Before the footer, not after it. Declared last, this rendered
            // BELOW the wordmark and the licences link -- the message telling
            // you there is nothing here sat past the end of the page.
            if (items.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    Text(
                        text = "Nothing here under that name.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Space.Gutter),
                    )
                }
            }

            // where the reference storefronts put this sort of thing, and it
            // keeps the header free -- the bar already carries brand, search
            // and bag, and a fourth control there would be one too many.
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
                    // The account lives here rather than in the bar, and
                    // that is the bar's own rule being kept: it already
                    // carries brand, search and bag, and a fourth control
                    // there would be one too many. The footer is where the
                    // reference storefronts put the same thing.
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.Lg)) {
                        FooterWord(text = "ACCOUNT", onClick = onAccount)
                        FooterWord(text = "LICENCES", onClick = onLicences)
                    }
                }
            }

        }

        StorefrontHeader(
            query = query,
            onQuery = { query = it; showFromTop() },
            tabs = tabs,
            selected = tab,
            onSelect = { tab = it; showFromTop() },
            scrolled = scrolled,
            onBag = onBag,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

private const val ALL_TAB = "ALL"

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
