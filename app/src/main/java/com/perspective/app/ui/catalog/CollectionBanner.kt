package com.perspective.app.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil.compose.AsyncImage
import com.perspective.app.ui.campaignFrame
import com.perspective.app.ui.components.FrameCounter
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
 *
 * **[initialFrame] is the frame the onboarding screen was showing.** This is
 * where the photograph from the first screen lands, and it has to land as
 * itself: opening at frame 0 while the person was looking at frame 2 would swap
 * the picture mid-flight and spend the continuity the crossing exists to buy.
 * It seeds the pager once, on arrival; the banner still never advances itself.
 *
 * **Why this no longer draws through `ImagePlate`.** The ratio has to sit
 * OUTSIDE the shared node and nothing inside it may re-derive a size —
 * `ImagePlate` appends its own `aspectRatio` after the caller's modifier, which
 * would put it inside, and `Modifier.aspectRatio` discards the animated height
 * it is handed. The banner would then hold 3:4 for the whole flight and only
 * the width would move. [com.perspective.app.ui.campaignFrame] has the detail.
 */
@Composable
fun CollectionBanner(
    frames: List<String>,
    captions: List<String> = emptyList(),
    initialFrame: Int = 0,
    modifier: Modifier = Modifier,
) {
    if (frames.isEmpty()) return
    val pager = rememberPagerState(
        initialPage = initialFrame.coerceIn(0, frames.lastIndex),
    ) { frames.size }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Campaign images, ${frames.size} frames, swipe to change"
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(Ratio.Banner)
                .campaignFrame(),
        ) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = frames[page],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }
        // Counter and caption sit OUTSIDE the shared node deliberately. Carried
        // inside it they would ride the photograph across at full-screen scale
        // — a frame counter the size of a headline over the first screen — and
        // then shrink into place. They belong to the banner, so they arrive
        // with the wall.
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
