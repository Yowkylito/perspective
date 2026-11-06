package com.perspective.app.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.perspective.app.data.Category
import com.perspective.app.data.MockImages
import com.perspective.app.data.Product
import com.perspective.app.data.SampleCatalogue
import com.perspective.app.ui.BottomNavSpace
import com.perspective.app.ui.components.ProductTile
import com.perspective.app.ui.theme.Space
import kotlinx.coroutines.launch

/** The ALL tab's label, and the heading that stands in for a category when none is chosen. */
private const val AllTab = "ALL"
private const val AllTitle = "ALL PIECES"

/**
 * The shop: the storefront the app opens on, and the only screen with chrome
 * over it.
 *
 * **It is the landing, not the second tab.** Onboarding hands straight to this
 * screen, which is why the campaign banner and the storefront header live here
 * rather than on the wall. That is not a preference about where editorial looks
 * best: [CollectionBanner] carries `campaignFrame()` and [StorefrontHeader]
 * carries `brandWordmark()`, and both are the receiving end of a crossing that
 * starts on the onboarding screen. A shared element whose other end is on a
 * screen nobody navigated to degrades to nothing, silently — so the two
 * crossings and the landing screen are one decision, not three.
 *
 * Modelled on the reference app's Shop tab as measured off a recording — a
 * header with the mark and a search field, a row of category tabs under it,
 * full-bleed editorial, then a grid of labelled category tiles — and assembled
 * out of what this storefront already owns. Every piece below is a component
 * the app is already using; none of it is a second version of anything.
 *
 * **The header is an overlay, not a row above the grid**, and the grid pads its
 * content by exactly the header's height. That is what lets a plate travel
 * under the bar instead of stopping at it, which is the thing the glass is
 * there to show. The wall held this arrangement while the wall was the landing;
 * the two screens have swapped their top treatments entire, rather than growing
 * a second one each.
 *
 * **One category state, two controls that set it.** The tabs in the header and
 * the tiles in the mosaic narrow the same [Category]; the ALL tab widens it
 * again. An `ALL CATEGORIES` word used to do that widening, back when this page
 * had no chrome over it and there was nothing else that could. There is now,
 * and two ways to one place on one page is one too many.
 *
 * **Choosing a category narrows this page; it does not navigate.** A category
 * is a state of the shop, not somewhere you return to. Giving it a route would
 * put it on the back stack between the tab and the product, so Back out of a
 * piece would land on a filter rather than on the tab that was pressed — and it
 * would make this screen something the graph has to know the shape of.
 * Narrowing in place keeps [onProductClick] the only edge it has.
 *
 * The editorial leaves the moment the catalogue narrows, by tab or by query:
 * past that point someone is looking for a thing, not reading a campaign, and a
 * photograph between them and the results is the storefront talking over them.
 *
 * **Nothing animates, the narrowing included.** The grid has already reserved
 * its geometry, so there is no movement to explain, and a crossfade laid over a
 * lazy grid re-fires on every recycle — the same objection that keeps staggered
 * tile entrance out of the wall. What does explain the change is the jump back
 * to the top, and a position is not an animation.
 *
 * **The empty line is for the query, not for the tabs.** A tile exists because
 * a [Category] does and every entry has stock behind it, so narrowing by tab
 * cannot return nothing. A typed query can, because a query is arbitrary where
 * a category is not.
 */
@Composable
fun ShopScreen(
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
    initialCampaignFrame: Int = 0,
) {
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    // The entry itself rather than an index: it is what every call below wants,
    // and an enum is Serializable, which is the whole of what the saved-state
    // bundle asks of it. Saved rather than remembered because rotating out of a
    // category and back into the map is the page forgetting where you were.
    var chosen by rememberSaveable { mutableStateOf<Category?>(null) }
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    // Narrowing puts you at the top of the result. Without it, choosing a
    // category removes the banner and the whole mosaic from above the grid
    // while the grid keeps its index and offset: you ask to see Trousers and
    // land near the foot of a list you have never seen the top of.
    //
    // Done on the press rather than in an effect keyed on the filter, because
    // an effect also fires when this screen is rebuilt on the way back from a
    // product -- which would throw away the scroll position the grid just
    // restored, every time.
    val showFromTop: () -> Unit = { scope.launch { grid.scrollToItem(0) } }

    val tabs = remember { listOf(AllTab) + Category.entries.map { it.label } }

    // Read once into a plain local. The grid's item blocks need the nullable
    // narrowed, and a delegated property cannot be smart-cast.
    val category = chosen
    val selected = if (category == null) 0 else Category.entries.indexOf(category) + 1

    val items = remember(category, query.text) {
        val byCategory =
            if (category == null) SampleCatalogue.items
            else SampleCatalogue.items.filter { it.category == category }
        val q = query.text.trim()
        if (q.isEmpty()) byCategory
        else byCategory.filter {
            it.name.contains(q, ignoreCase = true) || it.colourway.contains(q, ignoreCase = true)
        }
    }

    val editorial = category == null && query.text.isBlank()

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
                // Clears the floating bar as well as the page's own end margin,
                // or the last row sits under the pill.
                bottom = Space.Xxl + BottomNavSpace,
            ),
            horizontalArrangement = Arrangement.spacedBy(Space.GridGapHorizontal),
            verticalArrangement = Arrangement.spacedBy(Space.GridGapVertical),
        ) {
            if (editorial) {
                // Where the onboarding photograph comes to rest. It opens on
                // the frame that screen was showing, so the picture lands as
                // itself rather than swapping mid-flight.
                item(span = { GridItemSpan(maxLineSpan) }, key = "banner") {
                    CollectionBanner(
                        frames = MockImages.banners,
                        captions = MockImages.bannerCaptions,
                        initialFrame = initialCampaignFrame,
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }, key = "mosaic") {
                    Column {
                        // Body size and no tracking, per the structure rules —
                        // the letter-spaced capitals are for micro-labels, not
                        // for the headings that name a section.
                        Text(
                            text = "SHOP BY CATEGORY",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(
                                start = Space.Gutter,
                                end = Space.Gutter,
                                top = Space.Xl,
                                bottom = Space.Md,
                            ),
                        )
                        CategoryMosaic(
                            // Plates by position, the same arithmetic the wall
                            // does, so the same category wears the same picture
                            // wherever it appears and the tile stays a
                            // recognisable object rather than a different door
                            // each time.
                            tiles = Category.entries.mapIndexed { i, c ->
                                c to MockImages.categoryTiles[i % MockImages.categoryTiles.size]
                            },
                            onSelect = { picked ->
                                chosen = picked
                                showFromTop()
                            },
                        )
                    }
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }, key = "heading") {
                    Column(
                        modifier = Modifier.padding(
                            start = Space.Gutter,
                            end = Space.Gutter,
                            top = Space.Sm,
                            bottom = Space.Xl,
                        ),
                    ) {
                        Text(
                            text = category?.label ?: AllTitle,
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

                items(items, key = { it.id }) { product ->
                    ProductTile(product = product, onClick = { onProductClick(product) })
                }
            }
        }

        StorefrontHeader(
            query = query,
            onQuery = { query = it; showFromTop() },
            tabs = tabs,
            selected = selected,
            onSelect = { index ->
                chosen = if (index == 0) null else Category.entries[index - 1]
                showFromTop()
            },
            scrolled = scrolled,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
