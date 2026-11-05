package com.perspective.app.ui.brand

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Space

/** Mirrors the bar height in PerspectiveApp's Chrome. Keep the two in step. */
private val ChromeHeight = 56.dp

/**
 * displaySmall (22sp) down to labelMedium (13sp), which is what the chrome
 * wordmark is set in. Every other splash-side measurement is divided by this so
 * that at the end of the travel the mark is not merely similar to the chrome
 * mark but geometrically identical to it — same tracking, same rule inset, same
 * hairline. That is the whole reason the handover is invisible.
 */
private const val ChromeScale = 13f / 22f

/**
 * The timeline, in milliseconds from the start of the sequence.
 *
 * Every phase is driven off ONE animation rather than sequenced with `delay`,
 * and that is a correctness requirement, not a style choice. With `delay` the
 * schedule keeps running when the platform has animations turned off: the tweens
 * snap to their end values while the waits do not, so the mark appears fully
 * formed and then just sits there for a second -- a static logo blocking the
 * app, which is precisely what a launch sequence must never become. Measured on
 * an emulator with animations disabled: the rule held one unchanging frame for
 * 367ms, then the glyphs appeared in a single step.
 *
 * Driven from one [Animatable], that case collapses correctly: the master tween
 * completes immediately, every phase reads its end value, and the sequence
 * finishes without ever being seen.
 *
 * The phases themselves each sit inside the 120-360ms band the motion system
 * requires. The master tween is a timeline carrying them, not an animation in
 * its own right, which is why it is linear and longer.
 */
private const val RuleStart = 0
private const val RuleEnd = 240
private const val WipeStart = 160
private const val WipeEnd = 420
private const val TravelStart = 560
private const val TravelEnd = 920
private const val TotalMs = TravelEnd

/** Maps overall [progress] onto one phase's own 0..1, with that phase's curve. */
private fun phase(progress: Float, startMs: Int, endMs: Int, easing: Easing): Float {
    val raw = (progress * TotalMs - startMs) / (endMs - startMs)
    return easing.transform(raw.coerceIn(0f, 1f))
}

/**
 * The launch sequence, and the gate that decides whether it runs at all.
 *
 * [content] is composed immediately and underneath, so start-up work happens
 * behind the mark rather than after it.
 */
@Composable
fun BrandGate(content: @Composable () -> Unit) {
    val context = LocalContext.current

    // A fast path, NOT the safety net -- the timeline in BrandMoment is what
    // actually makes the animations-off case correct, by collapsing to nothing
    // when its one tween completes instantly.
    //
    // This check cannot be relied on, and that was measured rather than assumed:
    // on an emulator reporting no value for ANIMATOR_DURATION_SCALE this read
    // returns the 1f default while the platform runs every animation at zero
    // scale, so it reports animations ON while they are in fact OFF. It is kept
    // because when it IS right it saves composing the sequence at all, and a
    // structural read like this is the only kind the motion system permits --
    // scaling durations here would double-apply what Compose already does.
    val animationsOff = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }

    var done by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (!done && !animationsOff) {
            BrandMoment(onDone = { done = true })
        }
    }
}

/**
 * The wordmark assembling itself, then opening into the storefront.
 *
 * **What it explains**, since this codebase does not allow motion that explains
 * nothing: where the wordmark in the app bar came from, and where the shop came
 * from. The mark is built at full size; then its rule opens outward until it is
 * the screen, with the storefront inside it, while the wordmark travels to the
 * exact position and size it occupies in the chrome.
 *
 * **The opening is the brand's own geometry, deliberately.** The reference for
 * this was a sportswear app whose splash is swept away by a diagonal band --
 * and that diagonal is its logo's angle, which is exactly why it cannot be
 * borrowed. This brand owns one piece of geometry, a thin rectangular rule
 * around a monospaced word, and it is orthogonal rather than dynamic. So the
 * screen does not arrive on a diagonal; it arrives through the rule, because
 * the rule is the only shape this mark has. Same mechanic, different alphabet.
 *
 * **The gesture is the brand's own construction.** The rule opens from the
 * centre, then the glyphs wipe in left to right inside it — a woven garment
 * label being made, which is exactly what the mark is: a wordmark inside a
 * rectangular rule. It is one idea, not a sequence of tricks.
 *
 * Timings are the [Motion] tokens: each step sits inside the 120-360ms band and
 * decelerates into rest. Nothing bounces. The travel is the one use of
 * [Motion.Considered] outside the wall-to-detail transition, and for the same
 * reason — an element crossing between screens.
 *
 * Tap anywhere to skip. A brand moment that cannot be dismissed is the
 * interface making you wait, which is the luxurious register, not this one.
 */
@Composable
private fun BrandMoment(onDone: () -> Unit) {
    val finish by rememberUpdatedState(onDone)

    val progress = remember { Animatable(0f) }
    val ink = MaterialTheme.colorScheme.onBackground
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(TotalMs, easing = LinearEasing))
        finish()
    }

    val ground = MaterialTheme.colorScheme.background
    // The rule's resting bounds, captured once at first layout so the opening
    // starts exactly where the rule is rather than a frame behind it.
    var ruleBounds by remember { mutableStateOf<Rect?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { finish() } }
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        val targetY = with(LocalDensity.current) {
            (statusBar + ChromeHeight / 2).toPx() - constraints.maxHeight / 2f
        }
        val strokeDp = Space.Hairline / ChromeScale

        // Ground as four bands around a growing hole, rather than a sheet that
        // fades. A fade dissolves the splash everywhere at once and says
        // nothing; an opening says the storefront came OUT of the mark. The
        // bands are drawn rather than clipped because four rects cost nothing
        // and a clip path on every frame of a full-screen layer does not.
        Canvas(modifier = Modifier.matchParentSize()) {
            val e = phase(progress.value, TravelStart, TravelEnd, Motion.Standard_)
            val b = ruleBounds
            if (e <= 0f || b == null) {
                drawRect(color = ground)
                return@Canvas
            }
            val l = lerp(b.left, 0f, e)
            val t = lerp(b.top, 0f, e)
            val r = lerp(b.right, size.width, e)
            val bo = lerp(b.bottom, size.height, e)
            drawRect(ground, Offset(0f, 0f), Size(size.width, t.coerceAtLeast(0f)))
            drawRect(ground, Offset(0f, bo), Size(size.width, (size.height - bo).coerceAtLeast(0f)))
            drawRect(ground, Offset(0f, t), Size(l.coerceAtLeast(0f), (bo - t).coerceAtLeast(0f)))
            drawRect(ground, Offset(r, t), Size((size.width - r).coerceAtLeast(0f), (bo - t).coerceAtLeast(0f)))
        }

        Box(
            modifier = Modifier.graphicsLayer {
                val t = phase(progress.value, TravelStart, TravelEnd, Motion.Standard_)
                translationY = lerp(0f, targetY, t)
                scaleX = lerp(1f, ChromeScale, t)
                scaleY = scaleX
            },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coords ->
                        if (ruleBounds == null) ruleBounds = coords.boundsInRoot()
                    }
                    .padding(
                        // The chrome's own frame insets, pre-divided so they
                        // land on 16dp and 12dp once the travel has scaled
                        // everything down.
                        horizontal = Space.Md / ChromeScale,
                        vertical = (Space.Sm + Space.Xs) / ChromeScale,
                    )
                    .drawWithContent {
                        val w = phase(progress.value, WipeStart, WipeEnd, Motion.Decelerate)
                        clipRect(right = size.width * w) {
                            this@drawWithContent.drawContent()
                        }
                    },
            ) {
                Wordmark(
                    boxed = false,
                    style = MaterialTheme.typography.displaySmall,
                    tracking = 2.dp / ChromeScale,
                    color = ink,
                )
            }

            // The rule is drawn rather than bordered so it can open from the
            // centre at a constant stroke width. Scaling a border instead would
            // stretch the hairline with it.
            Canvas(modifier = Modifier.matchParentSize()) {
                val stroke = strokeDp.toPx()
                val width = size.width * phase(progress.value, RuleStart, RuleEnd, Motion.Decelerate)
                if (width <= stroke) return@Canvas
                // Fades as the opening takes over: past this point the hole's
                // own edge is the rule, and two of them would read as a seam.
                val alpha = 1f - phase(progress.value, TravelStart, TravelEnd, Motion.Accelerate)
                drawRect(
                    color = ink.copy(alpha = alpha),
                    topLeft = Offset((size.width - width) / 2f + stroke / 2f, stroke / 2f),
                    size = Size(width - stroke, size.height - stroke),
                    style = Stroke(width = stroke),
                )
            }
        }
    }
}
