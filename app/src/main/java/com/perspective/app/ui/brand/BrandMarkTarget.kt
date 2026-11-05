package com.perspective.app.ui.brand

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Where the launch sequence's wordmark should come to rest.
 *
 * The screen that receives the mark declares its own position; [BrandMoment]
 * reads it rather than carrying a constant describing where it believes that
 * screen puts things. That is not indirection for its own sake. The constant
 * version drifted out of truth twice — once when the wall swapped its app bar
 * for a storefront header, and again when onboarding became the first screen —
 * and both times the symptom was identical and silent: the mark completed a
 * perfectly correct travel to a position where nothing was waiting, and then
 * vanished in one frame at full opacity. Nothing fails to compile when a
 * handover target moves, so the target has to come from layout.
 *
 * Null outside [BrandGate] — a preview or a test then simply reports nowhere.
 */
val LocalBrandMarkTarget = compositionLocalOf<((Rect) -> Unit)?> { null }

/**
 * True while the launch sequence is still covering the screen.
 *
 * [BrandGate] composes the app underneath the sequence from the first frame, so
 * the first screen is alive and running its effects for the whole of it. That
 * is the point — start-up work happens behind the mark rather than after it —
 * but it means anything a screen does to the WINDOW rather than to its own
 * layout takes effect while the brand ground, not that screen, is what is being
 * looked at. System bar appearance is the case in hand: onboarding wants light
 * icons for its photograph, and setting them at composition put white glyphs on
 * the Ivory splash for the length of the sequence, every launch.
 */
val LocalBrandMomentActive = compositionLocalOf { false }

/**
 * Declares this composable as the resting place of the launch wordmark.
 *
 * Put it on the mark the first screen draws, so the sequence hands over onto
 * its own twin: same glyphs, same size, same tracking, same colour. That is the
 * whole reason the handover is invisible.
 */
@Composable
fun Modifier.brandMarkTarget(): Modifier {
    val report by rememberUpdatedState(LocalBrandMarkTarget.current)
    return this.onGloballyPositioned { coordinates ->
        report?.invoke(coordinates.boundsInRoot())
    }
}
