# Perspective

Premium commerce app. Jetpack Compose, single module for now.

## Brand

The wordmark is `PERSPECT1VE`: all caps in a monospaced face, inside a thin
rectangular rule, with a numeral `1` standing in for the I and some Es mirrored.
Source artwork lives outside the repo; the mark is reproduced in code, not shipped
as a bitmap, so it stays crisp and recolours with the theme.

Sampled from the artwork:

| Token  | Value     | Role                                   |
|--------|-----------|----------------------------------------|
| Ivory  | `#FFFDED` | light ground (91% of artwork pixels)   |
| Ink    | `#000000` | light ink, dark ground — true black    |
| Paper  | `#FFFFFF` | dark ink                               |

Light and dark are a straight inversion. One wordmark, two modes — never a second
logo asset.

## Rules

**Monochrome by intent.** No sale red, no badge colour, no rating amber, no
dynamic colour. Emphasis comes from weight, spacing and position. Adding a hue to
`Color.kt` needs a stated reason.

**True black, not off-black.** `#000000`, deliberately. Do not soften it to
`#111111` — that is a different brand's decision.

**Two type voices, no more.** A monospaced display face (the wordmark's own) for
titles, nav labels and letter-spaced micro-labels; a quiet detail face for body,
subtitles, metadata and price. Price sits at body size and regular weight — it is
not emphasised.

**Nike is reference, not template.** Its restraint is worth learning from; its
layout is not to be reproduced. Specifically avoid: its 1:1 grid tiles, its
title-above-hero PDP order, its 4:5 hero ratio, its five-icon bottom bar, and any
of its copy strings. Where a decision could go either way, take the one that
differs.

## Toolchain

AGP 8.10.1 · Kotlin 2.1.0 · compileSdk 36 · minSdk 26 · JDK 17 ·
Compose BOM 2025.10.00 · Material3 1.9.0-beta02

The Gradle wrapper points at a local distribution
(`~/MyFolders/gradle-9.1.0-all.zip`), matching the other projects in this
workspace.

## Type

Display — **Courier Prime** (400/700), confirmed as the wordmark's face. Titles,
nav labels, letter-spaced micro-labels. Always uppercase, always tracked.

Detail — **Instrument Sans** (variable, 400/500). Neutral and slightly sturdier.
Courier Prime is a delicate, modulated face; body copy needs a partner that does
not also read fragile. The pair opposes on width, stroke modulation and serif
treatment at once.

**Cap-height correction — do not "tidy" the display sizes.** Measured from the
binaries: Courier Prime cap 0.580 em vs Instrument Sans 0.720 em. At equal point
size its capitals render 19% shorter, so every mono size in `Type.kt` is set
about **1.24x** its sans counterpart. Flattening the mono sizes to match the sans
numbers will make every label look undersized.

Other measured values worth keeping in mind: Courier Prime advance 0.600 em,
default line box 1.123 em, **383 codepoints**; Instrument Sans advance 0.728 em,
line box 1.220 em, **343 codepoints** (latin + latin-ext only). Both charsets are
thin — check them before adding a locale beyond Latin.

Line height is always set explicitly, never inherited from font metrics.

**Never use a literal `Ǝ` (U+018E).** Courier Prime does not carry it, but
Android's Noto Sans does, so the glyph silently falls back to a proportional face
~9% narrower than the surrounding monospace — broken on device, fine in Figma.
Mirror a normal `E` with a graphics-layer flip, as `Wordmark.kt` does.

Price uses `bodyMedium` — body size, regular weight. Emphasising it is a
discount-retail reflex; holding it level is the opposite signal.

## Licences

Courier Prime and Instrument Sans are SIL OFL 1.1; full texts in `/licenses`.
Bundling in a commercial app is permitted unmodified and unrenamed, provided the
copyright and licence notice ship with the app. **TODO: surface both in an in-app
licences screen before release.**

## Structure

Measured at a 390px viewport on The Row, Our Legacy, TOTEME and COS, with Nike as
the contrast reference. Where an earlier guess disagreed with a measurement, the
measurement won.

1. **Product images 2:3 portrait.** `Ratio.Product`. Four premium storefronts,
   zero squares; Nike's 1:1 is the outlier. Portrait vs square is the single
   clearest divide in the product wall.
2. **Two fixed columns, no density switcher.** Column count proves nothing — all
   five sites use two. The *switcher* is the tell.
3. **Zero horizontal gutter, 40dp row gap.** Target a row-gap:gutter ratio of at
   least 3:1. Nike's 6px gutter with no row gap is what makes its wall read as a
   continuous mass instead of discrete objects.
4. **One margin strategy, never mixed:** uniform 16dp inset, or full-bleed image
   with a 12dp label inset. Both are premium; combining them is not.
5. **Price at the same size and weight as the name, or lighter. Never larger.**
   Nike sets price 16px/500 against a 14px/500 name — the inverse.
6. **Screen titles at body size**, normal or negative tracking, no letter-spaced
   small caps in the type system.
7. **Label under the image**, 8dp gap, name then price, never overlaid or
   hover-revealed.
8. **PDP leads with a full-bleed 2:3 image pager** carrying an "n/N" counter —
   not a thumbnail strip.
9. **Name and price on one row**, name start, price end, immediately under the
   image. Centred-and-stacked is an equally valid alternative; pick one.
10. **CTA is an outlined rectangle, 40dp tall, 0dp radius** — not a filled pill.
    Sticky vs inline placement is genuinely optional; the outlined square styling
    is not.

**The subtractive rule underwriting all ten:** zero star ratings, zero review
counts, zero urgency copy, zero badge overlays, no promo bar. A regex over
rendered text returned an empty match on The Row and TOTEME, against Free
Shipping / Just In x3 / Best Seller / Sale on Nike. Wishlist hearts are the one
safe optional.

**Open question, deliberately unanswered:** every site measured is a website, so
there is no verified evidence on whether boutique *apps* avoid bottom tab bars.
Treat drawer-vs-tabs as undecided rather than settled.

## Status

Brand system complete and a working two-screen scaffold on top of it.

- `ui/catalog/ProductWallScreen` — 2 columns, zero gutter, 40dp row gap,
  full-bleed 2:3 plates, label inset 12dp.
- `ui/product/ProductDetailScreen` — full-bleed pager with an n/N counter, name
  and price on one row, inline sizes with strike-through plus grey on
  unavailable, square outlined CTA, accordions.
- `ui/PerspectiveApp` — 56dp chrome carrying the wordmark, word labels instead
  of icons, NavHost between wall and detail.

Verified on a physical device in both modes, not just compiled.

**Placeholder debt:** `ImagePlate.tone` alternates two near-identical fills purely
so adjacent plates are distinguishable while there is no photography. It is a
reviewing aid, not part of the system — delete the parameter when real images
land.

**Open:** no bottom tab bar, decided on brand grounds rather than evidence (every
storefront measured is a website and cannot have tabs). Revisit first if the app
grows past a couple of destinations.
