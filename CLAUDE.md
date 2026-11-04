# Perspective

Premium commerce app. Jetpack Compose, single module for now.

## Positioning

Perspective sells clothing. It is **premium, not luxurious** — and the distinction
is not decoration, it decides values.

Luxury signals through indulgence: slow languid motion, ornament, elements that
exist to be admired. Premium signals through precision: every element earns its
place by doing a job.

**The test, applied before anything is added:** name the purpose it serves —
explaining a state change, a spatial relationship, hierarchy, or legibility. If
the only answer is "it looks expensive", cut it.

Everything below follows from this.

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

## Motion

Premium motion is precise and confident; luxurious motion is slow and languid.
Everything sits in a 120-360ms band on Material 3's emphasized curves, decelerates
into rest, and never bounces. **No springs with visible overshoot** — that reads
playful, which is a different brand.

`Motion.kt` holds the tokens. There is no Material motion-token layer to lean on:
`MotionScheme` arrives in material3 1.5.0-beta02 and the BOM pins **1.4.0**, so
these are hand-rolled on purpose, not from ignorance of the Material API.

**The test, before adding any animation:** name what it explains — a state change,
a spatial relationship, hierarchy, or where something came from. "It looks
expensive" is not a purpose.

Two things that failed the test and are deliberately absent:
- **Staggered tile entrance on the wall.** A lazy grid recycles, so it re-fires
  every time a row scrolls back. Motion with nothing to say, repeated.
- **Parallax between PDP pager images.** Defensible once there is photography;
  against flat placeholder plates it is a visual no-op that cannot be verified.

What is present, and what each one earns its place by doing:
- **Shared element, wall -> detail.** The only use of `Motion.Considered`. Shows
  that the thing you tapped became the thing you are looking at; without it the
  navigation is a cut and the eye has to find the product again.
- **Nav crossfade.** Non-shared content gets out of the travelling image's way.
- **Size rule fade-in.** Marks which size the tap landed on.
- **CTA label crossfade.** The label changes meaning; a hard swap reads as a glitch.
- **Accordion expansion.** Shows that content came from the row you pressed.

**Do not hand-roll reduced-motion duration scaling.** Compose reads
`Settings.Global.ANIMATOR_DURATION_SCALE` itself and observes it live, so
animations already snap when the user disables them — a second check would
double-apply. Read the setting only for *structural* choices the scale cannot
express (swapping a shared element for a plain crossfade, say). The same
mechanism makes transitions easy to inspect: `adb shell settings put global
animator_duration_scale 10` slows them for screenshotting.

**Shared-element API is version-sensitive.** On Compose animation 1.9.3 it is
still `@ExperimentalSharedTransitionApi` (opted in once in `app/build.gradle.kts`)
and several parameters have pre-rename spellings — `placeHolderSize` with a
capital H, `ResizeMode.ScaleToBounds` with a capital S. The live developer.android
reference documents 1.12/1.13 and **will not compile here**. Check the version
before copying a sample.

## Icon

The wordmark in its rule — the brand's actual logo, not a monogram derived from
it. Every glyph is extracted from the shipped `CourierPrime-Bold.ttf` and placed
by transform, mirrored Es included, using the same construction as `Wordmark.kt`
so the icon and the in-app mark cannot drift apart.

**Why not a single letter.** The first attempt was a Courier `P`. It was perfectly
legible and wrong: Courier is a typewriter face, so a lone serifed letter reads as
a document or a writing app rather than apparel. Premium clothing apps almost
universally put the whole wordmark on a plain tile — The Row, Aesop, SSENSE,
Jil Sander — and accept that the letters are small, because the tile is the
recognition cue, not the reading.

**The honest tradeoff.** Eleven monospaced characters in a 48dp icon cannot be
read. Cap height lands at 4.3dp. At launcher size it registers as a framed
word-block, which is the intended effect and is what the reference brands ship;
it is not an accident to be "fixed" by enlarging the type, which would only push
the rule outside the mask.

Geometry: rule 64x18dp centred in the 108dp viewport, so its furthest corner sits
33.2dp from centre against the mask's 36dp radius. Ivory ground, ink glyph —
the brand's primary surface, and a near-white tile holds contrast against dark
wallpaper where a black one would sink. A `monochrome` layer ships for Android 13+
themed icons.

minSdk 26, so `mipmap-anydpi-v26` covers every device and there are no legacy PNG
density buckets to maintain.

**Still needed for release:** a flat 512x512 PNG for the Play listing, which an
adaptive icon cannot supply. Generate from the same paths — do not redraw.

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
