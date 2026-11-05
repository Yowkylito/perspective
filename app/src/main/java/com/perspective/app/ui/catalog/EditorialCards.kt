package com.perspective.app.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.perspective.app.ui.components.ImagePlate
import com.perspective.app.ui.theme.Ink
import com.perspective.app.ui.theme.Ivory
import com.perspective.app.ui.theme.Ratio
import com.perspective.app.ui.theme.Space

/**
 * Full-width editorial cards, stacked.
 *
 * This is the section pattern the reference uses below its category mosaic: one
 * picture per row, running the full width, with a name sitting on the image
 * rather than beneath it. It reads as a stack of places to go, which is what
 * distinguishes it from the catalogue grid further down — the grid is a list of
 * things for sale, this is a list of ways in.
 *
 * Cards are [Ratio.Banner], the same 3:4 the hero uses, so the whole front page
 * speaks in two ratios rather than four: 3:4 for anything editorial, 2:3 for
 * anything that is a product.
 *
 * The gap between cards is the grid's own row gap. Running them flush would
 * make the section one block, and these are meant to be separate doors.
 */
@Composable
fun EditorialCards(
    cards: List<Pair<String, String>>,
    /**
     * Null until there is somewhere for a card to go.
     *
     * These were wrapped in a clickable that called an empty lambda: three
     * screen-tall cards on the front page that took a press and did nothing,
     * and three unactionable stops for a screen reader. The campaign banner
     * and the onboarding control both state the rule this broke — a control
     * that cannot do what it says is worse than an absent one.
     */
    onCard: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        cards.forEachIndexed { index, (label, image) ->
            if (index > 0) Spacer(Modifier.height(Space.GridGapVertical))
            Box(modifier = if (onCard != null) Modifier.clickable { onCard(label) } else Modifier) {
                ImagePlate(model = image, ratio = Ratio.Banner)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to Ink.copy(alpha = 0.45f),
                            ),
                        )
                        .padding(top = 72.dp),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Ivory,
                        modifier = Modifier.padding(Space.BleedLabelInset),
                    )
                }
            }
        }
    }
}
