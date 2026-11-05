package com.perspective.app.ui.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.perspective.app.data.MockImages
import com.perspective.app.ui.brand.Wordmark
import com.perspective.app.ui.components.PerspectiveButton
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Space
import androidx.compose.ui.unit.dp
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
 */
@Composable
fun OnboardingScreen(
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val frames = MockImages.banners
    var index by remember { mutableIntStateOf(0) }

    LaunchedEffect(frames.size) {
        if (frames.size <= 1) return@LaunchedEffect
        while (true) {
            delay(HoldMs)
            index = (index + 1) % frames.size
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(
            targetState = index,
            animationSpec = tween(Motion.Considered, easing = Motion.Standard_),
            label = "onboardingFrame",
        ) { frame ->
            AsyncImage(
                model = frames[frame],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .padding(horizontal = Space.Gutter, vertical = Space.Xl),
        ) {
            Wordmark(style = MaterialTheme.typography.labelMedium, tracking = 2.dp)
            Spacer(Modifier.height(Space.Lg))
            Text(
                text = "Clothing made to be worn, not displayed. " +
                    "Cut plainly, in colours that hold.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.Xl))
            PerspectiveButton(
                label = "ENTER THE SHOP",
                onClick = onEnter,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
