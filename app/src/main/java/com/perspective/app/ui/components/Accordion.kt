package com.perspective.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Space

/**
 * Secondary information folds away. Measured on all three reference PDPs —
 * details, fit, materials and shipping are never stacked open down the page.
 *
 * The open/closed marker is a plus and minus rather than a chevron: it matches
 * the wordmark's typographic register, and it costs no icon asset.
 */
@Composable
fun Accordion(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var open by remember { mutableStateOf(false) }

    Column(modifier = modifier.animateContentSize(animationSpec = Motion.standard())) {
        HorizontalDivider(
            thickness = Space.Hairline,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { open = !open }
                .semantics { stateDescription = if (open) "Expanded" else "Collapsed" }
                .padding(vertical = Space.Md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = if (open) "−" else "+", style = MaterialTheme.typography.titleMedium)
        }
        if (open) {
            Column(modifier = Modifier.padding(bottom = Space.Lg)) { content() }
        }
    }
}
