package com.perspective.app.ui.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.perspective.app.ui.components.Accordion
import com.perspective.app.ui.theme.Space

/**
 * The licence notices for the two bundled typefaces.
 *
 * This screen is not optional polish. Courier Prime and Instrument Sans ship
 * under SIL OFL 1.1, which permits bundling in a commercial app only while the
 * copyright notice and licence travel with the software. Keeping the texts in
 * `/licenses` satisfies the repository; this is what satisfies the binary.
 *
 * The two notices sit in accordions rather than running down the page, which is
 * the same control the product detail uses for composition and care. A licence
 * is reference material: it has to be reachable and complete, and it does not
 * have to be the first thing anyone reads.
 *
 * Body copy is the detail voice at `bodySmall`. The display face is a
 * typewriter face and four thousand characters of it would be punishing; the
 * licence is the one place in this app where legibility outranks the brand's
 * preference for mono.
 */
@Composable
fun LicencesScreen(modifier: Modifier = Modifier) {
    val assets = LocalContext.current.assets

    // Read once. Both files are about 4KB, so this is cheaper than the
    // machinery it would take to move it off the composition.
    val courier = remember {
        assets.open("licenses/courier-prime.txt").bufferedReader().use { it.readText() }
    }
    val instrument = remember {
        assets.open("licenses/instrument-sans.txt").bufferedReader().use { it.readText() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.Gutter),
    ) {
        Spacer(Modifier.height(Space.Lg))
        Text(text = "LICENCES", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(Space.Sm))
        Text(
            text = "Perspective sets its type in two open-source faces. Both are " +
                "used unmodified and unrenamed, under the SIL Open Font License 1.1, " +
                "which asks that the notice below travel with the software.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Space.Xl))

        Accordion(title = "COURIER PRIME") { LicenceBody(courier) }
        Accordion(title = "INSTRUMENT SANS") { LicenceBody(instrument) }

        Spacer(Modifier.height(Space.Xxl))
    }
}

@Composable
private fun LicenceBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Space.Md),
    )
}
