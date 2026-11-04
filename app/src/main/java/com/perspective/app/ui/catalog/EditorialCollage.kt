package com.perspective.app.ui.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.perspective.app.ui.components.ImagePlate
import com.perspective.app.ui.theme.Ratio

/**
 * Share of the width taken by the lead frame: 0.692.
 *
 * Not a taste value — it is the only split that makes BOTH cells land on
 * ratios the system already has. Give the lead `a` of the width at
 * [Ratio.Banner], and its height is `a / 0.75 = 1.333a`. The two stacked
 * frames are then `(1 - a)` wide and `0.667a` tall each, so their ratio is
 * `(1 - a) / 0.667a`. Setting that equal to [Ratio.Product] gives
 * `1 - a = 0.4447a`, so `a = 0.692`. Change this number and the right-hand
 * frames stop being 2:3.
 */
private const val LeadWidth = 0.692f

/**
 * A composed block of three frames, set into the wall.
 *
 * **Why it is here at all:** the grid can only say "here are eight things, one
 * after another". This says "here is how they go together" — figure first,
 * then two stills of the goods themselves. That is a reading the grid cannot
 * give, which is what earns the block its place rather than "it looks
 * expensive".
 *
 * **Zero gutter between the cells, deliberately.** The wall runs no horizontal
 * gutter so its rows read as discrete objects against a 40dp row gap. Here the
 * intent is the opposite: three frames butted flush read as ONE composition,
 * which is what separates a composed block from three more tiles. The 40dp row
 * gaps above and below do the separating, as air and not a rule.
 *
 * No label, no caption, no price. The block is not merchandise; annotating it
 * would turn it into a product row with the prices missing.
 */
@Composable
fun EditorialCollage(
    frames: List<String>,
    modifier: Modifier = Modifier,
) {
    if (frames.size < 3) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Campaign composition, three images" },
    ) {
        ImagePlate(
            model = frames[0],
            ratio = Ratio.Banner,
            modifier = Modifier.weight(LeadWidth),
        )
        Column(modifier = Modifier.weight(1f - LeadWidth)) {
            ImagePlate(model = frames[1], ratio = Ratio.Product)
            ImagePlate(model = frames[2], ratio = Ratio.Product)
        }
    }
}
