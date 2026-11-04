package com.perspective.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Brand ground and ink, sampled directly from the PERSPECT1VE wordmark artwork.
 *
 *   Ivory #FFFDED — 91% of the artwork's pixels
 *   Ink    #000000 — true black, not an off-black
 *
 * The wordmark is one asset in both modes: box stroke and glyphs take [ink],
 * the plate takes [ground]. Light and dark are a straight inversion.
 */
val Ivory = Color(0xFFFFFDED)
val Ink = Color(0xFF000000)
val Paper = Color(0xFFFFFFFF)

/* Warm neutrals: Ink mixed into Ivory, so greys stay on the brand's warm axis
   instead of drifting cool. Percentage = how much Ink. */
val Ivory08 = Color(0xFFEBE9DA) // hairlines, dividers
val Ivory25 = Color(0xFFBFBEB2) // disabled, struck-through
val Ivory55 = Color(0xFF73726B) // secondary text, metadata

/* The same ramp on black: Ivory stepped down toward Ink, keeping the warm cast
   so dark mode reads as the same brand rather than a generic grey theme. */
val Night10 = Color(0xFF1A1918) // hairlines, dividers
val Night35 = Color(0xFF595953) // disabled, struck-through
val Night62 = Color(0xFF9E9D93) // secondary text, metadata

/* Reserved. The system is monochrome by intent — no sale red, no badge colour,
   no rating amber. Anything that needs attention earns it through weight,
   spacing or position, never hue. Add to this file only with a reason. */
