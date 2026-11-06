package com.perspective.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween

/**
 * Motion tokens.
 *
 * The brand is premium, not luxurious, and the two register differently in
 * motion than anywhere else in the system — which is why this file exists rather
 * than letting each call site pick a duration.
 *
 * Luxury motion is slow and languid; it asks to be admired, and it reads as the
 * interface making you wait. Premium motion is precise and confident: it moves
 * decisively, arrives settled, and gets out of the way. Everything here sits in
 * the 160-400ms band, decelerates into rest, and never bounces or overshoots. A
 * spring with visible bounce is playful, not premium — do not add one.
 *
 * **The band moved up one step, and the reason was the result rather than the
 * theory.** It was 120-360, and on device the app read as though beats were
 * missing — a press acknowledged and gone before it registered, screens
 * arriving without having been seen to arrive. The ceiling was not the
 * cause: fourteen of eighteen call sites sat at Quick or below, so the band
 * was a range the implementation never actually used. Every token moved up
 * one step together, which keeps every relationship between them and leaves
 * the character alone. 400ms is still not languid; 160 is still decisive.
 *
 * Shift them together or not at all. The relationships are load bearing —
 * PerspectiveApp's WallTrailMs + Standard == Considered is a documented
 * identity, and it survives only because all four moved by the same step.
 *
 * THE TEST, before adding any animation: name what it explains — a state change,
 * a spatial relationship, hierarchy, or where something came from. "It looks
 * expensive" is not a purpose. Decorative motion is the easiest thing in an app
 * to mistake for quality and the fastest to become tiresome.
 *
 * Deliberately absent: staggered entrance animation on the product wall. It
 * looks considered in a demo, but a lazy grid recycles, so it re-fires every time
 * a row scrolls back into view — motion with nothing to say, repeated. The grid
 * already reserves its geometry; content simply being there is correct.
 */
object Motion {
    /** Colour and alpha changes on something already on screen. */
    const val Instant = 160

    /** Small state changes: selection, label swaps, control feedback. */
    const val Quick = 240

    /** The default. Layout changes, expansion, anything that moves an edge. */
    const val Standard = 320

    /**
     * Elements crossing between screens, and the onboarding frame crossfade.
     *
     * Three things cross: the product image, the campaign photograph and the
     * wordmark. The one exception that is not a crossing is the onboarding
     * screen changing frame, which is a full-bleed photograph replacing
     * another and is the only in-screen change large enough to earn it.
     */
    const val Considered = 400

    /**
     * Emphasized decelerate — Material 3's own curve, not an invented one.
     * Starts fast, settles slowly. Use for anything ARRIVING.
     */
    val Decelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Emphasized accelerate. Use for anything LEAVING. */
    val Accelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Standard in-and-out, for changes that both start and end on screen. */
    val Standard_: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> instant(): FiniteAnimationSpec<T> = tween(Instant, easing = Standard_)
    fun <T> quick(): FiniteAnimationSpec<T> = tween(Quick, easing = Standard_)
    fun <T> standard(): FiniteAnimationSpec<T> = tween(Standard, easing = Standard_)
    fun <T> arriving(): FiniteAnimationSpec<T> = tween(Standard, easing = Decelerate)
    fun <T> leaving(): FiniteAnimationSpec<T> = tween(Quick, easing = Accelerate)
}
