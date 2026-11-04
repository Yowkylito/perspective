package com.perspective.app.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.perspective.app.ui.theme.Motion

/**
 * Scopes for the wall -> detail shared element.
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

/**
 * The default bounds transform is a spring. This uses the system's own curve and
 * duration instead, so the one motion that crosses between screens is governed
 * by the same tokens as everything that happens within one.
 */
private val ProductBounds = BoundsTransform { _, _ ->
    tween(Motion.Considered, easing = Motion.Decelerate)
}

/**
 * Marks the product image as the element that travels from the wall to the
 * detail screen.
 *
 * This is the one animation in the app that earns [Motion.Considered]. It does
 * real work: it shows that the thing you tapped became the thing you are now
 * looking at, so the detail screen reads as the product opening rather than as a
 * new screen loading. Without it the same navigation is a cut, and the eye has
 * to find the product again.
 */
@Composable
fun Modifier.productImage(id: String): Modifier {
    val shared = LocalSharedTransitionScope.current
    val visibility = LocalAnimatedVisibilityScope.current
    return if (shared != null && visibility != null) {
        with(shared) {
            this@productImage.sharedElement(
                sharedContentState = rememberSharedContentState(key = ProductImageKey(id)),
                animatedVisibilityScope = visibility,
                boundsTransform = ProductBounds,
            )
        }
    } else {
        this
    }
}
