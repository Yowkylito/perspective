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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.perspective.app.data.Bag
import com.perspective.app.data.SampleCatalogue
import com.perspective.app.data.Session
import com.perspective.app.ui.brand.Wordmark
import com.perspective.app.ui.catalog.ProductWallScreen
import com.perspective.app.ui.account.AccountScreen
import com.perspective.app.ui.account.OrderPlacedScreen
import com.perspective.app.ui.account.SignInScreen
import com.perspective.app.ui.bag.BagScreen
import com.perspective.app.ui.legal.LicencesScreen
import com.perspective.app.ui.onboarding.OnboardingScreen
import com.perspective.app.ui.product.ProductDetailScreen
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Space

/**
 * How far the wall's own content trails the photograph that is carrying it.
 *
 * The crossing is one element moving and everything else getting out of its
 * way, so the shop must not arrive alongside the picture — it arrives in its
 * wake. The delay is what removes the competition for attention; 80 is the
 * smallest value that does it and still lands the two together, since
 * 80 + Motion.Standard is Motion.Considered exactly. No claim is made about
 * what the eye does inside those 80ms.
 */
private const val WallTrailMs = 80

private const val RouteOnboarding = "onboarding"
private const val RouteWall = "wall"
private const val RouteProduct = "product/{id}"
private const val RouteLicences = "licences"
private const val RouteBag = "bag"
private const val RouteAccount = "account"
private const val RouteSignIn = "signin"
private const val RouteOrder = "order/{reference}"

@Composable
fun PerspectiveApp() {
    val nav = rememberNavController()

    // Which campaign photograph is on screen. Hoisted here because it belongs
    // to neither screen on its own: the onboarding hero and the banner at the
    // head of the wall draw from one set, and the picture crosses from one to
    // the other when the shop opens. Two private counters would let it change
    // identity in mid-air, which is the one thing that crossing cannot survive.
    var campaignFrame by rememberSaveable { mutableIntStateOf(0) }

    // SharedTransitionLayout must wrap the NavHost: it has to be a common
    // ancestor of both the tile and the detail hero for them to match.
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            // The house curves, declared once for the whole graph.
            //
            // Without these, navigation-compose supplies its own: a 700ms
            // fade, which is 1.94x the top of the band this project holds
            // itself to and exactly the slow, languid register Motion.kt names
            // as a different brand. Eight edges were running at it -- product
            // to bag, bag to sign-in, account to order and their returns --
            // because a transition nobody writes is not a transition nobody
            // sees. It also read as an inconsistency rather than a mistake:
            // the wall faded to the bag in 200ms and the product faded to the
            // same bag in 700.
            //
            // Set here, every destination inherits them and only a screen that
            // genuinely differs declares its own. Exactly one does.
            NavHost(
                navController = nav,
                startDestination = RouteOnboarding,
                enterTransition = { fadeIn(tween(Motion.Standard, easing = Motion.Decelerate)) },
                exitTransition = { fadeOut(tween(Motion.Quick, easing = Motion.Accelerate)) },
                popEnterTransition = { fadeIn(tween(Motion.Standard, easing = Motion.Decelerate)) },
                popExitTransition = { fadeOut(tween(Motion.Quick, easing = Motion.Accelerate)) },
            ) {
                // First, and without chrome -- the onboarding screen IS the
                // brand presenting itself, so a bar over it would be the app
                // introducing itself twice.
                composable(
                    RouteOnboarding,
                ) {
                    // The scope has to be provided here too, not only on the
                    // destinations that already had it: the photograph and the
                    // wordmark both cross FROM this screen, and a shared
                    // element with only one scope degrades silently to nothing.
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        OnboardingScreen(
                            frame = campaignFrame,
                            onFrame = { campaignFrame = it },
                            onEnter = {
                                nav.navigate(RouteWall) {
                                    popUpTo(RouteOnboarding) { inclusive = true }
                                }
                            },
                        )
                    }
                }
                composable(
                    RouteWall,
                    // The non-shared content fades while the image travels. That
                    // is the whole shape of the transition: one element moves and
                    // everything else gets out of its way.
                    //
                    // Arriving from onboarding the wall is what gets out of the
                    // way, so it fades up behind the photograph rather than with
                    // it. Nothing is staggered per tile: the grid recycles, so a
                    // stagger would re-fire every time a row scrolled back.
                    enterTransition = {
                        fadeIn(
                            tween(
                                Motion.Standard,
                                delayMillis = WallTrailMs,
                                easing = Motion.Decelerate,
                            ),
                        )
                    },
                ) {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        // No Chrome wrapper here: the wall draws its own header
                        // OVER the grid so plates pass underneath it. Wrapping
                        // it in a bar again would put the grid below the chrome
                        // and there would be nothing for the glass to reveal.
                        ProductWallScreen(
                            onProductClick = { nav.navigate("product/${it.id}") { launchSingleTop = true } },
                            onLicences = { nav.navigate(RouteLicences) { launchSingleTop = true } },
                            onAccount = { nav.navigate(RouteAccount) { launchSingleTop = true } },
                            onBag = { nav.navigate(RouteBag) { launchSingleTop = true } },
                            initialCampaignFrame = campaignFrame,
                        )
                    }
                }
                composable(
                    RouteProduct,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val product = SampleCatalogue.byId(entry.arguments?.getString("id").orEmpty())
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        Chrome(
                            leading = "BACK",
                            onLeading = { nav.popBackStack() },
                            onBag = { nav.navigate(RouteBag) { launchSingleTop = true } },
                        ) {
                            if (product != null) {
                                ProductDetailScreen(product = product)
                            }
                        }
                    }
                }
                composable(
                    RouteBag,
                ) {
                    Chrome(leading = "BACK", onLeading = { nav.popBackStack() }) {
                        BagScreen(
                            onCheckout = {
                                if (!Session.signedIn) {
                                    // The sign-in returns here rather than
                                    // going on by itself: checkout is the
                                    // customer's decision to make twice, and
                                    // being thrown into an order by the act of
                                    // signing in is the shop taking it for them.
                                    nav.navigate(RouteSignIn) { launchSingleTop = true }
                                } else {
                                    Session.placeOrder()?.let { order ->
                                        nav.navigate("order/${order.reference}") {
                                            // Back to the shop -- not to the
                                            // bag, which is empty now, and not
                                            // to the piece that was just
                                            // bought, which is where the stack
                                            // would otherwise land you. A
                                            // placed order is a finished
                                            // journey and the end of one is the
                                            // storefront. Reached from the
                                            // order history instead, this
                                            // destination is not navigated to
                                            // this way and Back still returns
                                            // to the account.
                                            popUpTo(RouteWall) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                            onBrowse = { nav.popBackStack(RouteWall, inclusive = false) },
                        )
                    }
                }
                // One route, two faces. Signing in or out flips which one it
                // draws rather than navigating, because the account and the way
                // into it are the same destination as far as anyone using it is
                // concerned -- and a sign-out that navigated would leave the
                // signed-in screen sitting on the back stack.
                composable(
                    RouteAccount,
                ) {
                    Chrome(
                        leading = "BACK",
                        onLeading = { nav.popBackStack() },
                        onBag = { nav.navigate(RouteBag) { launchSingleTop = true } },
                    ) {
                        if (Session.signedIn) {
                            AccountScreen(
                                onOrder = { reference -> nav.navigate("order/$reference") { launchSingleTop = true } },
                                onSignedOut = {},
                            )
                        } else {
                            SignInScreen(onSignedIn = {})
                        }
                    }
                }
                composable(
                    RouteSignIn,
                ) {
                    Chrome(leading = "BACK", onLeading = { nav.popBackStack() }) {
                        SignInScreen(onSignedIn = { nav.popBackStack() })
                    }
                }
                composable(
                    RouteOrder,
                    arguments = listOf(navArgument("reference") { type = NavType.StringType }),
                ) { entry ->
                    val order = Session.order(entry.arguments?.getString("reference").orEmpty())
                    Chrome(
                        leading = "BACK",
                        onLeading = { nav.popBackStack() },
                        // Reached from the history the bag may well hold
                        // something, and this is not a screen reached from it.
                        onBag = { nav.navigate(RouteBag) { launchSingleTop = true } },
                    ) {
                        if (order == null) {
                            // Nothing persists, so a reference can outlive the
                            // order it names -- signing out while this is still
                            // composed behind its own exit, or a cold start
                            // into a restored back stack. Leaving rather than
                            // drawing an empty screen about it.
                            //
                            // Named and inclusive, not bare: a bare
                            // popBackStack takes whatever is on top, and during
                            // this screen's own pop-exit that is the
                            // destination underneath it -- so signing out from
                            // the account with this still leaving would take
                            // the account with it.
                            LaunchedEffect(Unit) { nav.popBackStack(RouteOrder, inclusive = true) }
                        } else {
                            OrderPlacedScreen(
                                order = order,
                                onContinue = { nav.popBackStack() },
                            )
                        }
                    }
                }
                composable(
                    RouteLicences,
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
    onBag: (() -> Unit)? = null,
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
                ChromeWord(text = leading, onClick = onLeading)
                // Absent rather than dead where there is nowhere for it to
                // go -- on the bag itself, and on the screens reached from it.
                // It carried the word with no destination before there was a
                // bag to carry; a label that does nothing is worse than a gap.
                if (onBag != null) {
                    ChromeWord(
                        text = if (Bag.count > 0) "BAG (${Bag.count})" else "BAG",
                        onClick = onBag,
                    )
                }
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

/**
 * One word of chrome, pressable.
 *
 * A bare clickable Text takes its hit area from its own glyphs — about 16dp
 * tall at labelSmall, against the 48dp Android asks for — and announces with no
 * role, so a screen reader offers to activate something it cannot name as a
 * control. Material components apply both for you; nothing in this bar is one.
 */
@Composable
private fun ChromeWord(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = Space.TouchTarget)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}
