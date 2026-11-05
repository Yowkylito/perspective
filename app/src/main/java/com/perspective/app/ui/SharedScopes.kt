package com.perspective.app.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.perspective.app.ui.theme.Motion

/**
 * Scopes for the elements that cross between screens.
 *
 * Passed as CompositionLocals rather than threaded through every composable's
 * parameters: the product image sits inside a tile, inside a lazy grid, inside a
 * screen, and the official guidance prefers locals for exactly that depth.
 * Both are nullable so a composable used outside a SharedTransitionLayout — a
 * preview, a test — degrades to no animation instead of crashing.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Keys must be stable and unique across destinations; a data class gets equals/hashCode free. */
private data class ProductImageKey(val id: String)

/** There is only ever one campaign frame and one wordmark in flight, so no payload. */
private data object CampaignFrameKey
private data object BrandWordmarkKey

/**
 * The curve every crossing element travels on.
 *
 * The default bounds transform is a spring. This uses the system's own curve and
 * duration instead, so the motion that crosses between screens is governed by
 * the same tokens as everything that happens within one. [Motion.Considered] is
 * reserved for exactly this and nothing else.
 */
private val Crossing = BoundsTransform { _, _ ->
    tween(Motion.Considered, easing = Motion.Decelerate)
}

@Composable
private fun Modifier.crossing(key: Any): Modifier {
    val shared = LocalSharedTransitionScope.current
    val visibility = LocalAnimatedVisibilityScope.current
    return if (shared != null && visibility != null) {
        with(shared) {
            this@crossing.sharedElement(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = visibility,
                boundsTransform = Crossing,
            )
        }
    } else {
        this
    }
}

/**
 * Marks the product image as the element that travels from the wall to the
 * detail screen.
 *
 * It does real work: it shows that the thing you tapped became the thing you are
 * now looking at, so the detail screen reads as the product opening rather than
 * as a new screen loading. Without it the same navigation is a cut, and the eye
 * has to find the product again.
 */
@Composable
fun Modifier.productImage(id: String): Modifier = crossing(ProductImageKey(id))

/**
 * Marks the campaign photograph as the element that travels from the onboarding
 * screen into the banner at the head of the wall.
 *
 * **What it explains:** that the shop you just opened is the same place the
 * picture came from. Onboarding and the banner draw from one set —
 * `MockImages.banners` — so there is a real object to follow across the
 * boundary rather than an invented one, and following it is what makes two
 * screens read as one continuous place instead of a cut between strangers.
 * Without it the entrance is a cross-dissolve between two unrelated images and
 * the eye has nothing to hold.
 *
 * **Put the aspect ratio OUTSIDE this modifier.** `sharedElement` re-measures
 * its subtree under fixed constraints derived from the animated bounds, and
 * `Modifier.aspectRatio` discards the incoming height and re-derives it from
 * the ratio — so a ratio placed to the right of this call locks the element to
 * its target shape for the whole flight and the height never animates. The
 * wall-to-detail transition gets away with it only because both of its ends are
 * [com.perspective.app.ui.theme.Ratio.Product]; these two ends differ.
 */
@Composable
fun Modifier.campaignFrame(): Modifier = crossing(CampaignFrameKey)

/**
 * Marks the wordmark as the element that travels from the onboarding block to
 * the storefront bar.
 *
 * **What it explains:** that the mark is one object and has simply taken its
 * working position. It is the same thread the launch sequence pulls —
 * [com.perspective.app.ui.brand.BrandMoment] assembles the mark and sets it
 * down here — so a single object carries the eye from the first frame of the
 * app to the shop, with no point at which it is lost and re-found.
 *
 * `sharedBounds` rather than `sharedElement`: the geometry is identical at both
 * ends (`labelMedium`, 2dp tracking, boxed) but the ink is not — fixed Ivory on
 * the photograph, theme ink in the bar — so the two contents have to cross-fade
 * rather than one of them being carried across in the wrong colour.
 */
@Composable
fun Modifier.brandWordmark(): Modifier {
    val shared = LocalSharedTransitionScope.current
    val visibility = LocalAnimatedVisibilityScope.current
    return if (shared != null && visibility != null) {
        with(shared) {
            this@brandWordmark.sharedBounds(
                sharedContentState = rememberSharedContentState(key = BrandWordmarkKey),
                animatedVisibilityScope = visibility,
                enter = fadeIn(tween(Motion.Standard, easing = Motion.Decelerate)),
                exit = fadeOut(tween(Motion.Quick, easing = Motion.Accelerate)),
                boundsTransform = Crossing,
                // Above the photograph, by declaration rather than by luck.
                // Both crossings render into the same overlay and both default
                // to zero, which leaves draw order falling back to composition
                // order -- so the mark currently stays visible only because the
                // lazy grid happens to compose before the header. Hoist the
                // header above the grid and a full-screen photograph would
                // cover the travelling mark for most of the crossing, which is
                // the exact thing this crossing exists to prevent.
                zIndexInOverlay = 1f,
            )
        }
    } else {
        this
    }
}
