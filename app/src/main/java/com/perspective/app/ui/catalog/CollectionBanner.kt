package com.perspective.app.ui.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.perspective.app.ui.components.FrameCounter
import com.perspective.app.ui.components.ImagePlate
import androidx.compose.foundation.layout.padding
import com.perspective.app.ui.theme.Ivory
import com.perspective.app.ui.theme.Ratio
import com.perspective.app.ui.theme.Space

/**
 * The campaign banner at the head of the wall.
 *
 * **It carries no words of its own, and that is the design.** The brand's
 * campaign art already has typography burned into it — SIGNATURE TEE, a
 * colourway, a size run. Setting a Courier micro-label and a title on top would
 * put two type systems in one frame, each fighting the other for the same
 * corner. The app's job here is to present the artwork, not to annotate it.
 *
 * So the only chrome is the [FrameCounter], which is not decoration: without it
 * a still image gives no sign that two more frames exist behind it.
 *
 * No auto-advance. A banner that moves on its own is the storefront talking
 * while nobody asked — luxurious rather than premium, and it takes the reading
 * pace away from the person holding the phone. No dot row either; the counter
 * already states the count. And no CTA: there is no collection screen to send
 * anyone to, and a button that goes nowhere is worse than no button.
 *
 * [Ratio.Banner] rather than [Ratio.Product] keeps this from reading as one
 * more tile — see the token for why 3:4 and not 2:3 or 4:5.
 */
@Composable
fun CollectionBanner(
    frames: List<String>,
    captions: List<String> = emptyList(),
    modifier: Modifier = Modifier,
) {
    if (frames.isEmpty()) return
    val pager = rememberPagerState { frames.size }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Campaign images, ${frames.size} frames, swipe to change"
            },
    ) {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth()) { page ->
            ImagePlate(model = frames[page], ratio = Ratio.Banner)
        }
        // The caption rides the same scrim as the counter, opposite corner. It
        // names what the frame is; without it a campaign image is a handsome
        // picture of nobody's collection in particular.
        captions.getOrNull(pager.currentPage)?.let { caption ->
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = Ivory,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Space.Md),
            )
        }
        FrameCounter(current = pager.currentPage + 1, total = frames.size)
    }
}
