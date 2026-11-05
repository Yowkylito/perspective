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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp as lerpColor
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
import com.perspective.app.ui.theme.Ivory
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Space

/**
 * Fallback geometry, used only when no screen reports a target.
 *
 * Mirrors the bar height in PerspectiveApp's Chrome, and displaySmall (22sp)
 * down to labelMedium (13sp), which is what every wordmark outside this
 * sequence is set in. The live path measures both instead -- see
 * [LocalBrandMarkTarget].
 */
private val FallbackBarHeight = 56.dp

/**
 * displaySmall (22sp) down to labelMedium (13sp), which is what every wordmark
 * outside this sequence is set in.
 *
 * This is NOT fallback-only, and the name said so once and was wrong. The
 * travel's scale comes from the two measured widths, but the mark's internal
 * proportions -- frame insets, tracking, rule stroke -- are pre-divided by this
 * so they land on their design values once the travel has scaled everything
 * down. The two agree exactly today, because both the glyph component and the
 * tracking component of the width are in the same 13/22 ratio and Courier Prime
 * is monospaced. Set the receiving mark in a different style and the outer
 * width would still match while the insets landed a few percent out.
 */
private const val MarkScale = 13f / 22f

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

/**
 * The mark's ink at a given point in the sequence.
 *
 * Its ground changes under it mid-sequence: the splash ground until the opening
 * starts, photography after. Theme ink would then be tracking a background that
 * is no longer there, which is the rule OnboardingScreen states for its own
 * block -- so the mark crosses to the fixed Ivory it will be wearing when it
 * lands, over the first [Motion.Instant] of the travel.
 *
 * A function rather than a value read in [BrandMoment]'s own scope, because
 * reading an Animatable there subscribes the whole composable to every frame of
 * the master tween -- and [BrandMoment] is a BoxWithConstraints, so a
 * recomposition rebuilds its measure policy and remeasures the mark as well.
 * Fifty-five remeasure passes of an eleven-glyph Row, in the window the gate's
 * own docstring reserves for start-up work. Called from draw scopes and from
 * [TravellingMark] instead.
 */
private fun markInk(resting: Color, progress: Float): Color = lerpColor(
    start = resting,
    stop = Ivory,
    fraction = phase(progress, TravelStart, TravelStart + Motion.Instant, Motion.Standard_),
)

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

    // Where the mark is going, reported by whichever screen draws the receiving
    // wordmark. See LocalBrandMarkTarget for why this is measured rather than
    // written down: a handover target that is a constant goes silently wrong
    // the moment the receiving screen moves, and twice already has.
    val markTarget = remember { mutableStateOf<Rect?>(null) }
    // Stops accepting once the sequence is over. The receiving mark goes on to
    // travel into the storefront bar, which moves its bounds every frame of
    // that crossing; nothing reads the target by then, and recording it would
    // be pure churn.
    val reportTarget = remember<(Rect) -> Unit> {
        { bounds -> if (!done && markTarget.value != bounds) markTarget.value = bounds }
    }

    val active = !done && !animationsOff

    CompositionLocalProvider(
        LocalBrandMarkTarget provides reportTarget,
        LocalBrandMomentActive provides active,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            if (active) {
                BrandMoment(target = markTarget.value, onDone = { done = true })
            }
        }
    }
}

/**
 * The wordmark assembling itself, then opening into the storefront.
 *
 * **What it explains**, since this codebase does not allow motion that explains
 * nothing: where the wordmark on the first screen came from, and where that
 * screen came from. The mark is built at full size; then its rule opens outward
 * until it is the screen, with the shop inside it, while the wordmark travels
 * to the exact position and size the receiving screen draws it at — reported by
 * that screen through [LocalBrandMarkTarget], never assumed here.
 *
 * It is the first leg of one journey, not a self-contained flourish. The mark
 * set down here is the same object that later rises into the storefront bar
 * when the shop opens, so a single element carries the eye from the first frame
 * of the app to the catalogue without ever being lost and re-found.
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
private fun BrandMoment(target: Rect?, onDone: () -> Unit) {
    val finish by rememberUpdatedState(onDone)

    val progress = remember { Animatable(0f) }
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Skipping finishes the sequence; it does not cut it. Dropping a
    // full-screen ground in one frame is a worse artefact than the thing being
    // skipped, and this is the gesture of someone saying "yes, get on with it",
    // not of something going wrong. The remainder runs at Motion.Quick on the
    // leaving curve, which is decisive without being a jump cut.
    var skipped by remember { mutableStateOf(false) }
    LaunchedEffect(skipped) {
        if (skipped) {
            progress.animateTo(1f, tween(Motion.Quick, easing = Motion.Accelerate))
        } else {
            progress.animateTo(1f, tween(TotalMs, easing = LinearEasing))
        }
        finish()
    }

    val restingInk = MaterialTheme.colorScheme.onBackground
    val ground = MaterialTheme.colorScheme.background
    // The rule's resting bounds, captured once at first layout so the opening
    // starts exactly where the rule is rather than a frame behind it.
    var ruleBounds by remember { mutableStateOf<Rect?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { skipped = true } }
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        // Only reached when no screen reported a target -- a preview, a test, or
        // a first screen that draws no mark of its own. It puts the mark where a
        // 56dp bar would carry it, which is where the app bar screens draw it.
        val fallbackY = with(LocalDensity.current) {
            (statusBar + FallbackBarHeight / 2).toPx() - constraints.maxHeight / 2f
        }
        val strokeDp = Space.Hairline / MarkScale

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
                val from = ruleBounds
                val to = if (from != null && from.width > 0f) target else null
                // Scale comes from the two measured widths rather than from a
                // ratio of point sizes, so the mark arrives the same OUTER
                // width as the one underneath it however that one is set. Its
                // internal proportions still come from MarkScale -- see there.
                if (from != null && to != null) {
                    translationX = lerp(0f, to.center.x - from.center.x, t)
                    translationY = lerp(0f, to.center.y - from.center.y, t)
                    scaleX = lerp(1f, to.width / from.width, t)
                } else {
                    translationX = 0f
                    translationY = lerp(0f, fallbackY, t)
                    scaleX = lerp(1f, MarkScale, t)
                }
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
                        horizontal = Space.Md / MarkScale,
                        vertical = (Space.Sm + Space.Xs) / MarkScale,
                    )
                    .drawWithContent {
                        val w = phase(progress.value, WipeStart, WipeEnd, Motion.Decelerate)
                        clipRect(right = size.width * w) {
                            this@drawWithContent.drawContent()
                        }
                    },
            ) {
                TravellingMark(restingInk = restingInk, progress = { progress.value })
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
                    color = markInk(restingInk, progress.value).copy(alpha = alpha),
                    topLeft = Offset((size.width - width) / 2f + stroke / 2f, stroke / 2f),
                    size = Size(width - stroke, size.height - stroke),
                    style = Stroke(width = stroke),
                )
            }
        }
    }
}

/**
 * The wordmark, in its own composition scope.
 *
 * [progress] is read here rather than by the caller so that the per-frame
 * invalidation stops at this node. Outside the ink ramp the colour is a stable
 * value, so [Wordmark] itself skips and the glyph tree is not rebuilt.
 */
@Composable
private fun TravellingMark(restingInk: Color, progress: () -> Float) {
    Wordmark(
        boxed = false,
        style = MaterialTheme.typography.displaySmall,
        tracking = 2.dp / MarkScale,
        color = markInk(restingInk, progress()),
    )
}
