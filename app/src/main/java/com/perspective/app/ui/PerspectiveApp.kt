package com.perspective.app.ui

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.perspective.app.data.SampleCatalogue
import com.perspective.app.ui.brand.Wordmark
import com.perspective.app.ui.catalog.ProductWallScreen
import com.perspective.app.ui.bag.BagScreen
import com.perspective.app.ui.legal.LicencesScreen
import com.perspective.app.ui.product.ProductDetailScreen
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Space

private const val RouteWall = "wall"
private const val RouteProduct = "product/{id}"
private const val RouteLicences = "licences"
private const val RouteBag = "bag"

@Composable
fun PerspectiveApp() {
    val nav = rememberNavController()

    // SharedTransitionLayout must wrap the NavHost: it has to be a common
    // ancestor of both the tile and the detail hero for them to match.
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(navController = nav, startDestination = RouteWall) {
                composable(
                    RouteWall,
                    // The non-shared content fades while the image travels. That
                    // is the whole shape of the transition: one element moves and
                    // everything else gets out of its way.
                    exitTransition = { fadeOut(tween(Motion.Quick, easing = Motion.Accelerate)) },
                    popEnterTransition = { fadeIn(tween(Motion.Standard, easing = Motion.Decelerate)) },
                ) {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        // No Chrome wrapper here: the wall draws its own header
                        // OVER the grid so plates pass underneath it. Wrapping
                        // it in a bar again would put the grid below the chrome
                        // and there would be nothing for the glass to reveal.
                        ProductWallScreen(
                            onProductClick = { nav.navigate("product/${it.id}") },
                            onLicences = { nav.navigate(RouteLicences) },
                            onBag = { nav.navigate(RouteBag) },
                        )
                    }
                }
                composable(
                    RouteProduct,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    enterTransition = { fadeIn(tween(Motion.Standard, easing = Motion.Decelerate)) },
                    popExitTransition = { fadeOut(tween(Motion.Quick, easing = Motion.Accelerate)) },
                ) { entry ->
                    val product = SampleCatalogue.byId(entry.arguments?.getString("id").orEmpty())
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        Chrome(leading = "BACK", onLeading = { nav.popBackStack() }) {
                            if (product != null) {
                                ProductDetailScreen(product = product)
                            }
                        }
                    }
                }
                composable(
                    RouteBag,
                    enterTransition = { fadeIn(tween(Motion.Standard, easing = Motion.Decelerate)) },
                    popExitTransition = { fadeOut(tween(Motion.Quick, easing = Motion.Accelerate)) },
                ) {
                    Chrome(leading = "BACK", onLeading = { nav.popBackStack() }) {
                        BagScreen()
                    }
                }
                composable(
                    RouteLicences,
                    enterTransition = { fadeIn(tween(Motion.Standard, easing = Motion.Decelerate)) },
                    popExitTransition = { fadeOut(tween(Motion.Quick, easing = Motion.Accelerate)) },
                ) {
                    Chrome(leading = "BACK", onLeading = { nav.popBackStack() }) {
                        LicencesScreen()
                    }
                }
            }
        }
    }
}

/**
 * App chrome: a 56dp bar carrying the wordmark, with one word on each side.
 *
 * Words, not icons. The brand is typographic, so a row of glyph icons would be
 * the one place the interface stops speaking its own language — and it keeps the
 * app free of icon assets entirely.
 *
 * There is no bottom tab bar. Every storefront this takes after navigates from a
 * drawer, but all of them are websites, which cannot have tabs — so this is a
 * decision taken on brand grounds, not on evidence, and it is the one worth
 * revisiting first if the app grows past a couple of destinations.
 */
@Composable
private fun Chrome(
    leading: String,
    onLeading: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = Space.Gutter),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = leading,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.clickable(onClick = onLeading),
                )
                Text(text = "BAG", style = MaterialTheme.typography.labelSmall)
            }
            Wordmark(
                style = MaterialTheme.typography.labelMedium,
                tracking = 2.dp,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        HorizontalDivider(
            thickness = Space.Hairline,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        content()
    }
}
