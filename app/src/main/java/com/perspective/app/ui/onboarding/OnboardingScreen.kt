package com.perspective.app.ui.onboarding

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.perspective.app.data.MockImages
import com.perspective.app.ui.brand.Wordmark
import com.perspective.app.ui.brand.LocalBrandMomentActive
import com.perspective.app.ui.brand.brandMarkTarget
import com.perspective.app.ui.brandWordmark
import com.perspective.app.ui.campaignFrame
import com.perspective.app.ui.components.PerspectiveButton
import com.perspective.app.ui.theme.Ink
import com.perspective.app.ui.theme.Ivory
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Space
import kotlinx.coroutines.delay

/**
 * How long a frame holds before the next one takes over.
 *
 * The hold is not animation, so the motion band does not govern it; the change
 * between frames is, and that runs at [Motion.Considered] — a decisive
 * crossfade rather than the long dissolve the reference uses. A languid
 * dissolve is the luxurious register, and this brand is the other one.
 */
private const val HoldMs = 3200L

/**
 * Where the scrim starts, as a fraction of screen height.
 *
 * It covers the lower 88% and is transparent at its top edge, so there is no
 * line anywhere on the screen where the photograph stops. A solid block would
 * be simpler and is what the first version did, but it cuts the picture in half
 * and turns the bottom of the screen into a panel — which is the layout of a
 * form, not of a storefront.
 *
 * It began at 58% and reads as too thin on device. The reason is which frames
 * it has to carry: the street shot the preview used is already dark where the
 * copy sits, so a short ramp looks sufficient there, while the brighter sky
 * frame and the pale wall frame leave the type fighting the photograph. The
 * scrim has to be depth enough for the WORST frame in the set, not the one it
 * was judged on.
 */
private const val ScrimHeight = 0.88f

/**
 * How much of the scrim survives the departure.
 *
 * The first version lifted it to nothing: the copy is gone, so the thing that
 * carried the copy should go too, and the shop should be handed the photograph
 * rather than a darkened one. On device that is half right. The wordmark does
 * not leave with the copy — it travels — so a full lift leaves the one element
 * the eye is following sitting on bare photography with no contrast guarantee,
 * which is precisely the failure the frame counter's scrim exists to prevent.
 *
 * So it lifts to roughly the depth the banner wears anyway (96dp to Ink 0.45).
 * Two thirds of the darkening comes off, which is plainly visible as the cover
 * coming away, and the picture crosses already dressed the way it will be
 * dressed when it lands.
 */
private const val ScrimFloor = 0.38f

/**
 * The first screen: the brand, and a way in.
 *
 * Full-bleed photography cycling behind a block that carries the wordmark, one
 * line about what this is, and the control that enters the shop. The reference
 * puts its logo, a benefits line and two buttons in exactly this position, and
 * the arrangement is right: the picture does the selling, the words say what
 * you are looking at, the control is the last thing your thumb reaches.
 *
 * **One control, not two.** The reference offers Sign Up beside Sign In. There
 * are no accounts in this app yet, and a button that cannot do what it says is
 * worse than an absent one — so there is a single way forward until there is
 * something real to sign into.
 *
 * **The copy is about the clothes, not about membership.** The reference sells
 * free shipping and members-only products, which is the promotional register
 * the brand rules exclude. This says what the shop is and stops.
 *
 * **The frame index is hoisted.** It is not this screen's private business: the
 * banner at the head of the wall draws from the same set, and the photograph
 * crosses from here to there when the shop opens. If the two kept their own
 * counters the picture would change identity mid-flight, which breaks the one
 * thing the crossing is for. [frame] in, [onFrame] out, and the wall seeds its
 * pager from the same number.
 *
 * **Leaving is a beat of its own**, and it is the first thing the app does that
 * the person asked for. Press and three things happen at once: the control
 * fills under the thumb, a single haptic tick confirms it, and the page clears
 * itself down to the photograph and the mark. Only then does the shop open. The
 * acknowledgement lands inside the window where a response still reads as
 * instantaneous, so the 120ms that follows is read as the interface doing as it
 * was told rather than as the interface being slow — and it leaves nothing on
 * screen that the crossing would have to shove aside.
 */
@Composable
fun OnboardingScreen(
    frame: Int,
    onFrame: (Int) -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val frames = MockImages.banners
    if (frames.isEmpty()) return
    val current by rememberUpdatedState(frame)
    val enter by rememberUpdatedState(onEnter)
    // Saveable: rotating inside the 120ms departure recreates the Activity, and
    // a plain remember would drop the press on the floor -- scrim back up, copy
    // back, nothing having happened.
    var leaving by rememberSaveable { mutableStateOf(false) }

    // Stops on the way out, so the frame the wall opens on is the frame that
    // was under the thumb. An advance mid-departure would swap the photograph
    // while it is in flight.
    LaunchedEffect(frames.size, leaving) {
        if (frames.size <= 1 || leaving) return@LaunchedEffect
        while (true) {
            delay(HoldMs)
            onFrame((current + 1) % frames.size)
        }
    }

    // Driven by an Animatable rather than a delay, for the reason BrandMoment
    // documents: with animations switched off a tween completes instantly while
    // a delay still waits, so a scheduled navigation would leave the person
    // staring at a cleared screen for the length of an animation they asked not
    // to see. Driven this way, that case collapses to an immediate open.
    val clearing = remember { Animatable(1f) }
    LaunchedEffect(leaving) {
        if (!leaving) return@LaunchedEffect
        clearing.animateTo(0f, tween(Motion.Instant, easing = Motion.Accelerate))
        enter()
    }

    // Not while the launch sequence is still covering the screen: the window
    // belongs to whatever is on top of it, which until then is the brand ground.
    LightSystemBars(enabled = !LocalBrandMomentActive.current)

    Box(modifier = modifier.fillMaxSize()) {
        // The photograph is the element that crosses to the wall, so the shared
        // node wraps the Crossfade rather than sitting on the image inside it:
        // mid-fade there are two images composed, and two nodes claiming one
        // key in one destination is not a thing the matcher can resolve.
        Box(modifier = Modifier.fillMaxSize().campaignFrame()) {
            Crossfade(
                targetState = frame,
                animationSpec = tween(Motion.Considered, easing = Motion.Standard_),
                label = "onboardingFrame",
            ) { index ->
                AsyncImage(
                    model = frames[index.coerceIn(0, frames.lastIndex)],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }

        // Three stops, not two. A single transparent-to-black ramp still shows a
        // faint band where it begins; carrying most of the darkening into the
        // lower third keeps the top of the scrim genuinely invisible and leaves
        // the photograph readable for longer down the frame.
        //
        // Most of it goes with the copy on the way out — see [ScrimFloor] for
        // what stays behind and why the wordmark is the reason.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .fillMaxHeight(ScrimHeight)
                .graphicsLayer { alpha = ScrimFloor + (1f - ScrimFloor) * clearing.value }
                .background(
                    Brush.verticalGradient(
                        0.00f to Color.Transparent,
                        0.28f to Ink.copy(alpha = 0.24f),
                        0.62f to Ink.copy(alpha = 0.70f),
                        1.00f to Ink.copy(alpha = 0.95f),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = Space.Gutter, end = Space.Gutter, bottom = Space.Xl),
        ) {
            // Fixed Ivory throughout: this block sits on a photograph in both
            // modes, so anything theme-derived would be tracking a background
            // that is not there.
            //
            // The mark does NOT clear with the rest of the block. It is the one
            // thing on this screen that has somewhere else to be: the launch
            // sequence set it down here, and when the shop opens it rises into
            // the storefront bar. Fading it out and drawing it again in the bar
            // would break a continuity that has held since the first frame of
            // the app.
            Wordmark(
                style = MaterialTheme.typography.labelMedium,
                tracking = 2.dp,
                color = Ivory,
                modifier = Modifier.brandMarkTarget().brandWordmark(),
            )
            Column(modifier = Modifier.graphicsLayer { alpha = clearing.value }) {
                Spacer(Modifier.height(Space.Lg))
                Text(
                    text = "Clothing made to be worn, not displayed. " +
                        "Cut plainly, in colours that hold.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ivory.copy(alpha = 0.82f),
                )
                Spacer(Modifier.height(Space.Xl))
                PerspectiveButton(
                    label = "ENTER THE SHOP",
                    // Disabled rather than merely faded. graphicsLayer alpha
                    // hides a control from the eye and from nothing else: at
                    // alpha 0 it is still hit-testable and still a focus stop.
                    // It has also genuinely stopped being available.
                    enabled = !leaving,
                    onClick = { leaving = true },
                    ink = Ivory,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Light status-bar icons for as long as this screen is up.
 *
 * The screen is a photograph that runs under the status bar, and the theme's
 * own choice — dark icons in light mode — puts black glyphs on whatever happens
 * to be at the top of the frame. That is the same mistake as theme-derived ink
 * anywhere else on this screen: tracking a background that is not there. The
 * previous appearance is restored on the way out, so the wall gets the icons
 * its own ground calls for, and the swap happens at the end of the crossing
 * rather than during it.
 */
@Composable
private fun LightSystemBars(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(view, enabled) {
        if (!enabled) return@DisposableEffect onDispose { }
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousStatus = controller?.isAppearanceLightStatusBars
        val previousNav = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            if (controller != null && previousStatus != null && previousNav != null) {
                controller.isAppearanceLightStatusBars = previousStatus
                controller.isAppearanceLightNavigationBars = previousNav
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
