package com.perspective.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spatial scale.
 *
 * The values here are measured off live boutique storefronts at a 390px
 * viewport, not estimated. Where an earlier guess disagreed with the
 * measurement, the measurement won — see the notes on [Gutter] and
 * [GridGapHorizontal] in particular.
 */
object Space {
    val Hairline = 1.dp

    /**
     * The smallest anything you press may be.
     *
     * The chrome is words rather than buttons, and a bare Text takes its hit
     * area from its own glyphs — about 16dp tall at labelSmall, against the
     * 48dp Android asks for. Material components apply this for you; nothing
     * here is a Material component.
     */
    val TouchTarget = 48.dp

    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 16.dp
    val Lg = 24.dp
    val Xl = 40.dp
    val Xxl = 64.dp

    /**
     * Screen side margin: 16dp.
     *
     * Measured: Our Legacy 16.3px, TOTEME 16px — both ~4.1% of a 390px
     * viewport. Nothing measured went wider. A larger margin reads as a
     * content-starved app rather than a generous one; the air that signals
     * premium goes into [SectionTop], not into the sides.
     */
    val Gutter = 16.dp

    /** Label inset when the image runs full-bleed instead. The Row 10px, COS 12px. */
    val BleedLabelInset = 12.dp

    /**
     * Grid rhythm: no horizontal gutter, generous row gap.
     *
     * Measured column-gap is 0px on Our Legacy, COS and TOTEME (The Row 10px),
     * against row gaps of 40px, 24px and 16px. The row-gap:gutter ratio is the
     * signal — target at least 3:1. Nike, for contrast, runs a 6px gutter with
     * no meaningful row gap, which is what makes its wall read as a continuous
     * mass rather than a sequence of objects.
     */
    val GridGapHorizontal = 0.dp
    val GridGapVertical = 40.dp

    /** Gap from image bottom to the first line of label. Our Legacy 8px. */
    val LabelGap = 8.dp

    /** Gap between name and price within a label block. The Row row-gap 4px. */
    val LabelLineGap = 4.dp

    /**
     * Air above a section heading: 120dp.
     *
     * Measured 159px on Our Legacy's PDP and 102px on The Row's — 12–19% of
     * viewport height, and 4–6x the grid's own row gap. Sections earn
     * separation from emptiness, never from rules or dividers.
     */
    val SectionTop = 120.dp
}

/**
 * Image ratios.
 *
 * 2:3 portrait, measured identically on The Row (CSS 2000/3000), Our Legacy
 * (200/300) and COS (0.667). TOTEME runs a slightly squarer 5:7. Four premium
 * storefronts, zero squares — Nike's 1:1 is the outlier, and portrait vs square
 * is the clearest single divide in the product wall.
 */
object Ratio {
    const val Product = 2f / 3f
    const val ProductSquarer = 5f / 7f
    const val Hero = 2f / 3f

    /**
     * Campaign banner: 3:4.
     *
     * Deliberately NOT [Product]. A banner at the product ratio reads as an
     * oversized tile — one more thing in the catalogue rather than the frame
     * around it. 3:4 also keeps the source campaign art nearly whole: the
     * brand's own posters are 4:5, so this trims about 6% of width, where 2:3
     * would take 17% and cut through their typography.
     *
     * Not Nike's 4:5 either, which is the hero ratio the brand rules rule out.
     */
    const val Banner = 3f / 4f
}
