package com.perspective.app.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.perspective.app.ui.theme.Ink
import com.perspective.app.ui.theme.Ivory
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Space

/**
 * How much ground a focused or hovered control takes, against a pressed one's
 * whole fill. Enough to locate the focus ring on a keyboard or a desk setup,
 * not enough to read as pressed.
 */
private const val Quiet = 0.10f

/**
 * The only button shape in the app: outlined, square, short.
 *
 * Measured on the storefronts this takes after — The Row 358x30 with a 1px rule
 * and 0px radius, Our Legacy 358x47 on a transparent fill. A filled 56dp pill is
 * the mainstream-retail default and the single most recognisable big-box tell in
 * the component layer.
 *
 * Label is set uppercase by the caller's style, never by transforming the string,
 * so screen readers still announce ordinary words.
 *
 * [ink] exists for the one case where the button does not sit on a themed
 * surface: over photography, where `onBackground` would track the theme and so
 * track the wrong background — black on a dark scrim in light mode. Pass a
 * fixed colour there, as the frame counter and the campaign labels do.
 *
 * **Pressing fills the rule; it does not ripple.** This is not built on
 * `OutlinedButton` any more, and the reason is the press state rather than the
 * resting one: Material's button draws a ripple from the touch point, tinted
 * from the content colour, and a circle spreading out of a finger is a Material
 * gesture in an interface that speaks no other Material. Everything else here
 * is orthogonal and flat — words instead of icons, 0dp radius, hairline rules —
 * so the press is the rectangle filling, edge to edge. The label inverts with
 * it, which is the only way to keep it legible on its own ink and is why a
 * monochrome system makes this cheap.
 *
 * **Why the interaction stream and not `collectIsPressedAsState`.** Inside a
 * scrollable container — which is both other call sites — `Modifier.clickable`
 * holds the press back by `ViewConfiguration.getTapTimeout()` to see whether
 * the touch becomes a scroll, and on an ordinary tap then emits Press and
 * Release back to back in one coroutine. A state flag sampled from that never
 * settles on `true` for a single frame, so the fill simply never appears: it
 * shows up under a press-and-hold, which is exactly how one checks it by hand,
 * and never under a tap, which is how it is actually used. Collecting the
 * stream on one coroutine and suspending on each animation makes the press-in
 * run to completion before the release starts, so the gesture is seen whether
 * it lasted 40ms or 4 seconds. The source buffers 16 interactions, so nothing
 * is dropped while this is mid-animation.
 *
 * Focus and hover get the same fill at [Quiet]. `indication = null` removed the
 * ripple's state layers along with the ripple, and a control that cannot be
 * seen to have focus is a real defect on a keyboard or in DeX, where this is
 * the only button in the app.
 */
@Composable
fun PerspectiveButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    ink: Color = MaterialTheme.colorScheme.onBackground,
) {
    val interactions = remember { MutableInteractionSource() }
    val level = remember { Animatable(0f) }

    LaunchedEffect(interactions) {
        var pressed = false
        var focused = false
        var hovered = false
        interactions.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> pressed = true
                is PressInteraction.Release, is PressInteraction.Cancel -> pressed = false
                is FocusInteraction.Focus -> focused = true
                is FocusInteraction.Unfocus -> focused = false
                is HoverInteraction.Enter -> hovered = true
                is HoverInteraction.Exit -> hovered = false
            }
            val target = when {
                pressed -> 1f
                focused || hovered -> Quiet
                else -> 0f
            }
            if (target != level.targetValue) {
                val rising = target > level.value
                level.animateTo(
                    targetValue = target,
                    animationSpec = tween(
                        durationMillis = if (rising) Motion.Instant else Motion.Quick,
                        easing = Motion.Standard_,
                    ),
                )
            }
        }
    }

    // Disabled uses the secondary ink, not the hairline grey. An outlined
    // button whose label drops to outline-grey on ivory is unreadable --
    // inactive has to stay legible, or it reads as a rendering fault.
    val rule = if (enabled) ink else ink.copy(alpha = 0.38f)
    // The system is monochrome, so the contrasting ink is decidable rather than
    // another token: whichever of the two grounds the fill is not. Only ever
    // called with Ivory, Paper or Ink, which sit at the extremes of the range.
    val onInk = if (ink.luminance() > 0.5f) Ink else Ivory

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            // Drawn rather than read into composition, so the press animation
            // does not recompose the label on every frame of its own fade.
            .drawBehind { drawRect(ink.copy(alpha = level.value)) }
            .border(Space.Hairline, rule)
            // Role.Button on the clickable, which is where Material puts it.
            // The semantics tree this produces was diffed against the
            // OutlinedButton it replaces and is identical node for node, so
            // nothing a screen reader sees has changed.
            .clickable(
                interactionSource = interactions,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Label(
            label = label,
            inkProvider = {
                when {
                    !enabled -> ink.copy(alpha = 0.38f)
                    // Inverts only across the upper half of the travel, so the
                    // quiet focus wash leaves the label alone.
                    else -> lerpColor(ink, onInk, ((level.value - 0.5f) * 2f).coerceIn(0f, 1f))
                }
            },
        )
    }
}

/**
 * Isolated so that reading the press level recomposes the label and nothing
 * above it.
 *
 * The label changes meaning when a size is chosen. Cross-fading marks that as a
 * state change; a hard swap reads as a glitch at this size.
 */
@Composable
private fun Label(label: String, inkProvider: () -> Color) {
    Crossfade(targetState = label, animationSpec = Motion.quick(), label = "cta") {
        Text(text = it, style = MaterialTheme.typography.labelLarge, color = inkProvider())
    }
}
