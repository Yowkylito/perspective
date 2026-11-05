package com.perspective.app.ui.components

import androidx.compose.animation.Crossfade
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
import com.perspective.app.ui.theme.Motion
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
 *
 * [ink] exists for the one case where the button does not sit on a themed
 * surface: over photography, where `onBackground` would track the theme and so
 * track the wrong background — black on a dark scrim in light mode. Pass a
 * fixed colour there, as the frame counter and the campaign labels do.
 */
@Composable
fun PerspectiveButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    ink: Color = MaterialTheme.colorScheme.onBackground,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(0.dp),
        // Disabled uses the secondary ink, not the hairline grey. An outlined
        // button whose label drops to outline-grey on ivory is unreadable --
        // inactive has to stay legible, or it reads as a rendering fault.
        border = BorderStroke(
            Space.Hairline,
            if (enabled) ink else ink.copy(alpha = 0.38f),
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = ink,
            disabledContentColor = ink.copy(alpha = 0.38f),
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        // The label changes meaning when a size is chosen. Cross-fading marks
        // that as a state change; a hard swap reads as a glitch at this size.
        Crossfade(targetState = label, animationSpec = Motion.quick(), label = "cta") {
            Text(text = it, style = MaterialTheme.typography.labelLarge)
        }
    }
}
