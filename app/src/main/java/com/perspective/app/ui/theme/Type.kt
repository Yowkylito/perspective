@file:OptIn(ExperimentalTextApi::class)

package com.perspective.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.perspective.app.R

/**
 * Two voices, no more.
 *
 * DISPLAY — Courier Prime, the wordmark's own face, extended into the interface:
 * titles, nav labels, letter-spaced micro-labels. Always uppercase, always
 * tracked. Its bracketed serifs and modulated stroke are the brand's character;
 * everything else in the UI stays out of the way.
 *
 * DETAIL — Instrument Sans. Neutral, slightly sturdier, and proportional —
 * opposing the mono on width, stroke modulation and serif treatment all at once.
 * Courier Prime is a delicate face; the body text needs a partner that does not
 * also feel fragile, which is the job Instrument Sans is doing here.
 *
 * Both bundled in res/font rather than fetched through Play Services, so there is
 * no first-launch fallback flash on brand-critical type and no GMS dependency.
 * Both SIL OFL 1.1 — see /licenses.
 */
private val CourierPrime = FontFamily(
    Font(R.font.courier_prime_regular, FontWeight.Normal),
    Font(R.font.courier_prime_bold, FontWeight.Bold),
)

private val InstrumentSans = FontFamily(
    Font(
        R.font.instrument_sans_variable,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.instrument_sans_variable,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
)

/**
 * The display sizes below are NOT arbitrary, and must not be "tidied" to round
 * numbers that match the body scale.
 *
 * Measured from the shipping binaries: Courier Prime's cap height is 0.580 em
 * against Instrument Sans's 0.720 em. At equal point size its capitals render
 * 19% shorter. Every mono size here is therefore set about 1.24x its sans
 * counterpart so that a caps label and a line of body copy carry the same
 * optical weight. Drop the mono sizes to match the sans numerically and the
 * labels will look undersized next to everything they sit beside.
 *
 * Line height is always explicit. Courier Prime's default line box is 1.123 em —
 * tight enough that tracked capitals crowd the line above without help.
 *
 * TRACKING — resist the urge to open it up. Wide letter-spacing belongs to the
 * wordmark, not to the type system. Measured across The Row and TOTEME, every
 * sampled text node returns letter-spacing: normal and text-transform: none;
 * Our Legacy's display headings are tracked NEGATIVE at -0.01em. The wide-set
 * look people associate with luxury lives in those brands' logos, which are SVG
 * artwork, not CSS. Tracking here therefore runs from -0.015em at display sizes
 * up to 0.10em only at the smallest caps labels, where it aids legibility.
 */
private fun mono(
    size: Int,
    line: Int,
    tracking: Double,
    weight: FontWeight = FontWeight.Normal,
) = TextStyle(
    fontFamily = CourierPrime,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.em,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

private fun sans(
    size: Int,
    line: Int,
    weight: FontWeight = FontWeight.Normal,
    tracking: Double = 0.0,
) = TextStyle(
    fontFamily = InstrumentSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.em,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

val PerspectiveTypography = Typography(
    // Display tier — editorial section headings. Tracking is NEGATIVE here:
    // large type needs tightening, not opening. Measured on Our Legacy at
    // 40px/700/-0.4px, which is -0.01em.
    displayLarge = mono(40, 46, -0.015, FontWeight.Bold),
    displayMedium = mono(28, 34, -0.010),
    displaySmall = mono(22, 28, 0.0),

    // Screen titles sit at BODY size, not above it. The Row's page heading is
    // 13px against a 13px body; COS's is 16px against 16px. 17sp here is 14sp
    // body x the 1.24 Courier Prime cap correction - optically level, not larger.
    titleLarge = mono(17, 24, 0.0),
    titleMedium = mono(15, 21, 0.04),

    labelLarge = mono(14, 19, 0.06),
    labelMedium = mono(13, 18, 0.08),
    labelSmall = mono(12, 16, 0.10),

    // Detail voice.
    titleSmall = sans(14, 20, FontWeight.Medium),
    bodyLarge = sans(16, 24),
    bodyMedium = sans(14, 21),
    bodySmall = sans(12, 18),
)

/**
 * Price is [Typography.bodyMedium] — body size, regular weight, the same as any
 * other line of copy. Enlarging it is a discount-retail reflex; holding it level
 * with the description is the opposite signal. Do not give it its own style.
 */
