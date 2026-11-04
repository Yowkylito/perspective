package com.perspective.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.perspective.app.ui.theme.Space

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
 */
@Composable
fun PerspectiveButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val ink = MaterialTheme.colorScheme.onBackground
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(0.dp),
        // Disabled uses the secondary ink, not the hairline grey. An outlined
        // button whose label drops to outline-grey on ivory is unreadable --
        // inactive has to stay legible, or it reads as a rendering fault.
        border = BorderStroke(
            Space.Hairline,
            if (enabled) ink else MaterialTheme.colorScheme.outline,
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = ink,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}
