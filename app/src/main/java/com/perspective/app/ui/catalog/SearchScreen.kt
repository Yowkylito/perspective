package com.perspective.app.ui.catalog

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.perspective.app.data.Category
import com.perspective.app.data.MockImages
import com.perspective.app.data.Product
import com.perspective.app.data.SampleCatalogue
import com.perspective.app.ui.BottomNavSpace
import com.perspective.app.ui.components.ProductTile
import com.perspective.app.ui.theme.Space
import kotlinx.coroutines.launch

/**
 * Field height, matched to the sign-in field rather than to the header's.
 *
 * The storefront's search box stands at 34dp, and that number comes entirely
 * from the 56dp bar it has to fit inside. Nothing here is inside anything, so
 * it takes the height the app uses for a field standing on its own — which is
 * also the height of the only button in the app, and two hairline rectangles
 * that disagree about their height read as two systems meeting.
 */
private val FieldHeight = 48.dp

/**
 * Search.
 *
 * **This screen is extrapolated, and it should be read that way.** The
 * recording the rest of the tab bar was measured off never opens its Search
 * tab, so unlike [ShopScreen] there is no measurement under the layout below
 * and no claim that the reference looks like this. What is here instead is this
 * app's own search field, this app's own wall and this app's own category
 * tiles, arranged the way this storefront already arranges things. If the
 * reference is later found to differ, the measurement wins — that is the rule
 * the Structure notes were written under, and it applies to this file first.
 *
 * **The field is the storefront header's, moved and resized.** Same hairline
 * rectangle, same square corners, same hand-placed tracked placeholder, same
 * [BasicTextField] underneath. This is the app's only search input and a second
 * construction for it would read as a second app — so the one thing that
 * changes is [FieldHeight], for the reason recorded there.
 *
 * It is pinned above the results rather than scrolling away with them. It is
 * the only control on the page, and a result list long enough to scroll is
 * exactly the list whose query most needs correcting.
 *
 * **No screen title.** The placeholder says SEARCH and the tab under it says so
 * again; a heading would be the third time on one page, which is the same count
 * that took the account out of the wall's footer.
 *
 * **Tiles before a query, not a cursor over an empty page.** An empty search
 * screen asks a question and offers nothing to answer it with, and the pieces
 * that answer it are already drawn — [CategoryMosaic] is the same block the
 * wall opens with, wearing the same plates, so arriving here with no words in
 * mind still puts four doors on screen.
 *
 * **A tile narrows the catalogue; it does not type into the field.** Spelling
 * the choice as text would be the obvious shortcut and it would be wrong:
 * the labels are plural and the names are singular, so TROUSERS matches neither
 * trouser in the catalogue and the tile would clear the page it was meant to
 * fill. What a tile means is a filter, so it sets one — and it composes with
 * the query exactly as the wall's tabs do, because having chosen a category and
 * then typed, the obvious reading is that both still hold.
 *
 * **The match is the wall's own**: name or colourway, trimmed, case-insensitive,
 * and nothing cleverer. Deliberately not here: no search over descriptions,
 * which would return a piece for a word that is nowhere on its tile; no fuzzy
 * or partial-word matching, which cannot be verified against eight products; no
 * suggestions, and no recent searches — that last one needs somewhere to keep a
 * history, and this app persists nothing on purpose. A list that emptied itself
 * every cold start would be a feature that looks broken rather than small.
 *
 * **The empty line is the wall's, word for word.** The fact is identical — a
 * query matched nothing — and two different sentences for one event would be
 * the app sounding like two people. It is one line, it states what is true, and
 * it does not apologise or suggest.
 *
 * The status-bar inset is real padding, as on [ShopScreen] and for the same
 * reason: there is no chrome over this page to keep the clock legible.
 */
@Composable
fun SearchScreen(
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TextFieldValue rather than a bare String, as the header's field holds it:
    // rotating with a half-typed query and getting the cursor back at the far
    // end of it is a small loss people blame themselves for.
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    var chosen by rememberSaveable { mutableStateOf<Category?>(null) }
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    val showFromTop: () -> Unit = { scope.launch { grid.scrollToItem(0) } }

    // Read once into plain locals: the item blocks below need the nullable
    // narrowed, and a delegated property cannot be smart-cast.
    val category = chosen
    val typed = query.text.trim()

    val results = remember(category, typed) {
        val byCategory =
            if (category == null) SampleCatalogue.items
            else SampleCatalogue.items.filter { it.category == category }
        if (typed.isEmpty()) byCategory
        else byCategory.filter {
            it.name.contains(typed, ignoreCase = true) ||
                it.colourway.contains(typed, ignoreCase = true)
        }
    }

    // Nothing asked for yet. Not the same as a search that found nothing, which
    // is why the tiles stand here and the line stands there.
    val browsing = typed.isEmpty() && category == null

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        SearchField(
            value = query,
            onValue = {
                query = it
                showFromTop()
            },
            // The key has nothing to submit — the list is already filtered on
            // every keystroke — so its one job is to put the keyboard away so
            // that the results it has produced can be seen.
            onSearch = { focus.clearFocus() },
            modifier = Modifier.padding(horizontal = Space.Gutter, vertical = Space.Md),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = grid,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                // Clears the floating bar as well as the page's own end margin,
                // or the last row sits under the pill.
                bottom = Space.Xxl + BottomNavSpace,
            ),
            horizontalArrangement = Arrangement.spacedBy(Space.GridGapHorizontal),
            verticalArrangement = Arrangement.spacedBy(Space.GridGapVertical),
        ) {
            if (browsing) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "mosaic") {
                    Column {
                        // The wall's own name for this block. One block, one
                        // name — a second word for the same four tiles would
                        // make them read as a different set.
                        Text(
                            text = "SHOP BY CATEGORY",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(
                                start = Space.Gutter,
                                end = Space.Gutter,
                                top = Space.Lg,
                                bottom = Space.Md,
                            ),
                        )
                        CategoryMosaic(
                            // Plates by position, the arithmetic the wall and
                            // the shop both do, so a category wears one picture
                            // everywhere and stays a recognisable door.
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
                        if (category != null) {
                            BackToCategories(
                                onClick = {
                                    chosen = null
                                    showFromTop()
                                },
                            )
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        // The figure stays away from zero: nothing found is
                        // said in words below, and a count of none beside that
                        // line would be the screen saying it twice. The bag's
                        // piece count holds itself to the same rule.
                        if (results.isNotEmpty()) {
                            Text(
                                text = if (results.size == 1) "1 item" else "${results.size} items",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    top = if (category != null) Space.Xs else 0.dp,
                                ),
                            )
                        }
                    }
                }

                if (results.isEmpty()) {
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

                items(results, key = { it.id }) { product ->
                    ProductTile(product = product, onClick = { onProductClick(product) })
                }
            }
        }
    }
}

/**
 * The app's third text input, built as the first two are.
 *
 * Square corners and a hairline rule; the placeholder is a sibling `Text`
 * rather than a decoration box, which is what lets it carry the tracked display
 * voice while the field keeps its own.
 *
 * **The typed text is set in that same tracked mono, where the sign-in field
 * deliberately is not.** The difference is what each one holds. An address is
 * somebody's lowercase property and tracked capitals fight it the whole way; a
 * query is a line of chrome, typed into the chrome's own box, and this is the
 * second copy of a field that already exists. Matching the header matters more
 * here than matching the form.
 */
@Composable
private fun SearchField(
    value: TextFieldValue,
    onValue: (TextFieldValue) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(FieldHeight)
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            textStyle = LocalTextStyle.current.merge(
                MaterialTheme.typography.labelSmall.copy(color = scheme.onBackground),
            ),
            cursorBrush = SolidColor(scheme.onBackground),
            modifier = Modifier
                .fillMaxWidth()
                // The SEARCH placeholder is a sibling node and it leaves the
                // moment anything is typed, so without this the field has no
                // name at all. The same fix the other two fields carry.
                .semantics { contentDescription = "Search" },
        )
    }
}

/**
 * The way back out of a category, as a word.
 *
 * There is no chrome over this page, so there is no BACK to lean on, and the
 * narrowing is not on the back stack by design — a hardware Back from a
 * filtered search correctly leaves the tab rather than widening it. So the undo
 * lives on the page, built the way every other text control in this app is: a
 * bare clickable Text takes its hit area from its own glyphs, about 16dp
 * against the 48dp Android asks for, and announces with no role.
 */
@Composable
private fun BackToCategories(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = Space.TouchTarget)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "ALL CATEGORIES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
