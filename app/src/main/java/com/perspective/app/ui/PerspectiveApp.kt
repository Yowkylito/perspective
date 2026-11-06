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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.perspective.app.ui.catalog.ShopScreen
import com.perspective.app.ui.catalog.SearchScreen
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
 * How far the landing page's own content trails the photograph carrying it.
 *
 * The crossing is one element moving and everything else getting out of its
 * way, so the shop must not arrive alongside the picture — it arrives in its
 * wake. The delay is what removes the competition for attention; 80 is the
 * smallest value that does it and still lands the two together, since
 * 80 + Motion.Standard is Motion.Considered exactly. No claim is made about
 * what the eye does inside those 80ms.
 *
 * It belongs to whichever destination onboarding hands to. That is [RouteShop]
 * — the banner the photograph lands in is on the shop, so the trail has to be
 * there too.
 */
private const val LandingTrailMs = 80

private const val RouteOnboarding = "onboarding"
private const val RouteWall = "wall"
private const val RouteProduct = "product/{id}"
private const val RouteLicences = "licences"
private const val RouteBag = "bag"
private const val RouteAccount = "account"
private const val RouteSignIn = "signin"
private const val RouteOrder = "order/{reference}"
private const val RouteShop = "shop"
private const val RouteSearch = "search"

/**
 * The destinations the bar carries, in order.
 *
 * Five, in the reference's own order, Home first. Product, sign-in and order
 * are reached FROM one of these and returned from; putting them in a bar would
 * be claiming the app has more top-level shape than it does.
 *
 * Home leading the row is not the same statement as Home being the landing.
 * The app opens on [RouteShop] — see the onboarding handover — and the bar
 * simply shows Shop selected on arrival. Which tab leads and which screen you
 * land on are two decisions, and only one of them is the reference's.
 */
private val TabRoutes = listOf(RouteWall, RouteShop, RouteSearch, RouteBag, RouteAccount)

/**
 * Sentence case, not the tracked capitals the rest of the chrome uses.
 *
 * The reference sets its labels this way and the point of this bar is to look
 * like the reference. CLAUDE.md's display voice stays everywhere else.
 */
private val TabLabels = mapOf(
    RouteWall to "Home",
    RouteShop to "Shop",
    RouteSearch to "Search",
    RouteBag to "Bag",
    RouteAccount to "Profile",
)
private val TabIcons = mapOf(
    RouteWall to (NavIcons.Home to NavIcons.HomeFilled),
    RouteShop to (NavIcons.Shop to NavIcons.ShopFilled),
    RouteSearch to (NavIcons.Search to NavIcons.SearchFilled),
    RouteBag to (NavIcons.Bag to NavIcons.BagFilled),
    RouteAccount to (NavIcons.Profile to NavIcons.ProfileFilled),
)

@Composable
fun PerspectiveApp() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    // Which campaign photograph is on screen. Hoisted here because it belongs
    // to neither screen on its own: the onboarding hero and the banner at the
    // head of the wall draw from one set, and the picture crosses from one to
    // the other when the shop opens. Two private counters would let it change
    // identity in mid-air, which is the one thing that crossing cannot survive.
    var campaignFrame by rememberSaveable { mutableIntStateOf(0) }

    // Home's scroll position, held above the NavHost rather than inside the
    // screen.
    //
    // The bar pops Home's entry when you leave the tab -- see onSelect for why
    // it no longer asks navigation to save it -- and Home is the long page in
    // this app: the whole catalogue, with the editorial above it. Losing your
    // place in that every time you look in the bag is the bar undoing your
    // work, which is the one thing the saved place was ever for.
    //
    // Only this one. The shop needs no equivalent: it is the anchor, it is
    // never popped, and its category, its query and its own scroll live in its
    // entry untouched. Search, the bag and the account start fresh, which is
    // what each of the three is for -- a search is a question you ask now, not
    // one you left half-asked on another tab.
    val wallGrid = rememberLazyGridState()

    // The bar takes layout space rather than floating over the content, so no
    // screen has to know it is there or pad itself around it. It appears only
    // on the three destinations it carries; everywhere else the content has
    // the whole window, which is what keeps the onboarding photograph full
    // bleed and the product pager edge to edge.
    // A Box, not a Column: the pill floats OVER the content with the page
    // running underneath it, which is how the reference behaves and is the
    // thing a full-width bar in a column got wrong. Screens that scroll clear
    // it with BottomNavSpace.
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                                    // Into the shop, not the wall. The shop is
                                    // the storefront this app opens on, and it
                                    // is also the only screen holding both
                                    // receiving ends of this crossing -- the
                                    // banner the photograph lands in and the
                                    // header the wordmark lands on.
                                    onEnter = {
                                        nav.navigate(RouteShop) {
                                            popUpTo(RouteOnboarding) { inclusive = true }
                                        }
                                    },
                                )
                            }
                        }
                        composable(
                            RouteWall,
                        ) {
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                // No Chrome wrapper, and no header of its own either:
                                // Home is the page with nothing over it. The storefront
                                // header is the shop's, which is where the app lands.
                                ProductWallScreen(
                                    onProductClick = { nav.navigate("product/${it.id}") { launchSingleTop = true } },
                                    onLicences = { nav.navigate(RouteLicences) { launchSingleTop = true } },
                                    gridState = wallGrid,
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
                                                    popUpTo(RouteShop) { inclusive = false }
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                    },
                                    onBrowse = { nav.popBackStack(RouteShop, inclusive = false) },
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
                        // No Chrome on either: they are top-level
                        // destinations like the wall, and a BACK word on a tab
                        // is a way out of somewhere you did not come from.
                        composable(
                            RouteShop,
                            // The non-shared content fades while the image
                            // travels. That is the whole shape of the
                            // transition: one element moves and everything
                            // else gets out of its way.
                            //
                            // Arriving from onboarding the shop is what gets
                            // out of the way, so it fades up behind the
                            // photograph rather than with it. Nothing is
                            // staggered per tile: the grid recycles, so a
                            // stagger would re-fire every time a row scrolled
                            // back.
                            enterTransition = {
                                fadeIn(
                                    tween(
                                        Motion.Standard,
                                        delayMillis = LandingTrailMs,
                                        easing = Motion.Decelerate,
                                    ),
                                )
                            },
                        ) {
                            // The scope matters twice here. The product tiles
                            // need it, same as the wall, or a tap from this tab
                            // would cut where Home animates. And both crossings
                            // out of onboarding land on this screen -- the
                            // photograph in the banner, the wordmark in the
                            // header -- and a shared element with only one
                            // scope degrades silently to nothing.
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                ShopScreen(
                                    onProductClick = {
                                        nav.navigate("product/${it.id}") { launchSingleTop = true }
                                    },
                                    initialCampaignFrame = campaignFrame,
                                )
                            }
                        }
                        composable(RouteSearch) {
                            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                SearchScreen(
                                    onProductClick = {
                                        nav.navigate("product/${it.id}") { launchSingleTop = true }
                                    },
                                )
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

        if (route in TabRoutes) {
            PerspectiveBottomNav(
                modifier = Modifier.align(Alignment.BottomCenter),
                tabs = TabRoutes.map { tab ->
                    NavTab(
                        route = tab,
                        label = TabLabels.getValue(tab),
                        icon = TabIcons.getValue(tab).first,
                        iconFilled = TabIcons.getValue(tab).second,
                        count = if (tab == RouteBag) Bag.count else 0,
                    )
                },
                currentRoute = route,
                onSelect = { tab ->
                    // Already on it. Navigating again would pop this tab's
                    // entry and build a fresh one -- the same screen, its enter
                    // fade replayed, its state thrown away -- for a press that
                    // asked for nothing to change.
                    if (tab.route != route) {
                        nav.navigate(tab.route) {
                            // Pop to the shop, push the tab. Nothing else: no
                            // saveState, no restoreState.
                            //
                            // Those two were here to keep each tab's place, and
                            // on a flat graph they cannot do it safely. popUpTo
                            // saves everything above the anchor as ONE block,
                            // keyed by the first entry it popped, and the next
                            // navigate that matches that key replays the whole
                            // block rather than its root.
                            //
                            // The bag is the case that breaks: it is a tab AND
                            // a screen pushed from the product's chrome, so the
                            // saved block can span two tabs. Four taps, on a
                            // device, no race and no unusual state -- from Home
                            // open a product, open the bag from its chrome,
                            // then press HOME: the bag is what comes back, with
                            // Bag lit in the bar.
                            //
                            // Without them the stack states itself in one line
                            // -- [shop], or [shop, tab], with whatever you have
                            // opened sitting above it -- and a tab press can
                            // land on that tab and nothing else.
                            //
                            // Popped to the shop rather than to the graph's
                            // start: the start is onboarding, and onboarding is
                            // removed from the stack the moment the shop opens,
                            // so popping to an entry that is not there would
                            // stack a tab on every press.
                            //
                            // What this costs is the saved place, and the one
                            // worth keeping is hoisted instead. See wallGrid.
                            popUpTo(RouteShop) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
            )
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
 * It is the top half of a pair now. [PerspectiveBottomNav] carries the three
 * destinations you return to; this bar carries where you are and the one way
 * back out of it. So the trailing word is gone from the screens the bar
 * reaches — two routes to one screen on one page is one too many — and kept
 * on product and order, which the bar does not carry.
 *
 * The absence of a tab bar used to be documented here as a decision taken on
 * brand grounds rather than evidence, to revisit once the app grew past a
 * couple of destinations. It grew to seven. See CLAUDE.md's Navigation
 * section for what replaced it and why it is not Nike's.
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
