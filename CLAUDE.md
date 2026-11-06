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
title-above-hero PDP order, its 4:5 hero ratio, and any of its copy strings.
Where a decision could go either way, take the one that differs.

**The one exception, and it is an instruction rather than a drift.** The bottom
bar IS copied from Nike's, five icons and all, measured off a recording of the
original. That rule said to avoid exactly this; the owner overruled it twice,
explicitly, which is his to do. See Navigation. What is still not copied is
Nike's trademark: its Shop tab carries a swoosh and `NavIcons` draws a garment
tag instead. A brand's rules can be waived by the person who wrote them; a
brand's trademark cannot.

## Toolchain

AGP 8.10.1 · Kotlin 2.1.0 · compileSdk 36 · minSdk 26 · JDK 17 ·
Compose BOM 2025.10.00 · Material3 1.4.0 · Compose animation 1.9.3

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
copyright and licence notice ship with the app. Both notices ship in
`assets/licenses/` and are surfaced by `ui/legal/LicencesScreen`, reached from
the footer at the end of the wall. `/licenses` remains the repository's copy;
the assets are what satisfies the binary, so change the two together.

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

**Answered, by growth rather than by evidence:** there is still no measurement
on whether boutique *apps* avoid bottom tab bars — every site measured is a
website and cannot have one. What changed is this app. It has seven
destinations, three of which you return to rather than pass through, and the
Status section already named a tab bar as the first thing to revisit once that
happened. So there is one. See Navigation.

## Motion

Premium motion is precise and confident; luxurious motion is slow and languid.
Everything sits in a 160-400ms band on Material 3's emphasized curves, decelerates
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
- **Shared element, wall -> detail.** Shows that the thing you tapped became the
  thing you are looking at; without it the navigation is a cut and the eye has
  to find the product again.
- **Shared element, onboarding -> shop.** The campaign photograph contracts from
  full bleed into the banner at the head of the shop. Both ends draw from
  `MockImages.banners`, so there is a real object to follow across the boundary
  rather than an invented one — which is what makes the two screens read as one
  place instead of a cut between strangers. The frame index is hoisted into
  `PerspectiveApp` for the same reason: two private counters would let the
  picture change identity in mid-air.
- **Wordmark, onboarding -> bar.** Same glyphs, same size, same tracking at both
  ends; only the ink differs, so it is `sharedBounds` and the two cross-fade.
  It is the last leg of the journey `BrandMoment` starts, so one object carries
  the eye from the first frame of the app to the catalogue.
- **Departure beat.** Press and the page clears itself down to the photograph
  and the mark before the shop opens. It acknowledges the press inside the
  window where a response still reads as instantaneous, and it leaves nothing on
  screen for the crossing to shove aside.
- **Nav crossfade.** Non-shared content gets out of the travelling image's way.
- **Size rule fade-in.** Marks which size the tap landed on.
- **CTA press fill, and one haptic tick.** The rule fills and the label inverts.
  Confirms the press landed; flat and square because a ripple is a Material
  gesture in an interface that speaks no other Material. The tick is the same
  confirmation reaching a second sense. It sat at a single call site while two
  of the three were wired to no-ops — a hardware confirmation for a button that
  does nothing is the component lying on the app's behalf — and moved into
  `PerspectiveButton` once every call site committed to something.
- **ADDED on the product CTA.** The label says the press did what it said, then
  gives the control back. It belongs to the size that was added, so changing
  size reverts it on the same frame.
- **CTA label crossfade.** The label changes meaning; a hard swap reads as a glitch.
- **Accordion expansion.** Shows that content came from the row you pressed.

`Motion.Considered` is for elements crossing between screens. There are three:
the product image, the campaign photograph and the wordmark. The one thing that
holds it without being a crossing is the onboarding frame change, where a
full-bleed photograph replaces another — the only in-screen change big enough
to earn it. `Motion.kt` records that exception; nothing else may join it.

**A handover target is measured, never written down.** `BrandMoment` reads the
receiving wordmark's bounds through `LocalBrandMarkTarget` rather than holding a
constant for where it believes that mark sits. The constant version went wrong
twice — once when the wall swapped its app bar for a storefront header, again
when onboarding became the first screen — and both times the symptom was the
same and silent: a correct travel to a position where nothing was waiting, then
a full-opacity pop. Nothing fails to compile when a screen moves.

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

## Navigation

**The bottom bar is Nike's, copied rather than interpreted**, on instruction —
see the exception under Rules. An earlier version abstracted it into this
brand's own vocabulary: a flat full-width bar of tracked mono words with a
hairline underline. It was rejected, correctly, as far from the thing it was
meant to be.

Geometry is measured from a recording of the original, not estimated. At that
device's scale the pill spans 624 of 720 pixels and stands 115 tall: 312dp
wide, 24dp in from each edge, 58dp high. Its corners are ENDS — radius is half
the height — so `CircleShape` is literally correct and a RoundedCornerShape
with a number in it would approximate something exact.

**It floats.** Inset from both edges, lifted clear of the gesture bar, with
content running underneath. That is why `PerspectiveApp` draws it in a Box over
the NavHost rather than a Column beside it, and why scrolling tabs clear it with
`BottomNavSpace`. A bar reaching the screen edges is the thing that read wrong.

**Five tabs: Home, Shop, Search, Bag, Profile.** Icons above sentence-case
labels in the detail face — not the tracked capitals the rest of the chrome
uses, because the reference sets them that way. Selected is a change of weight,
not colour: the icon swaps outline for solid and the label goes medium. No
underline, no pill behind the selected tab, no accent. The bag carries a filled
disc with its count, overlapping the icon's top-right.

**Icons are drawn in code** (`ui/NavIcons.kt`), not shipped as assets — the same
reason the wordmark is. Ten vectors on one 19-unit optical grid, outline and
solid for each, so selecting fills a mark in without resizing it.

**Dark mode lifts the pill by value, not by shadow — and grades it.** A shadow
cannot lift a black pill off a black ground. Measured, the reference runs
`#1E1E20` at its top edge to `#151515` at its bottom, about nine points, so the
surface reads as catching light from above rather than as a cut-out. This one
runs about fifteen, deliberately harder: it sits on true black where the
reference's page is `#090B11`, so it has further to climb.

Both stops come off the dark neutrals that already existed — the top a fifth of
the way from `Night10` toward `Night35`, the bottom `Night10` itself. The bottom
edge therefore stays 26 points clear of the page and the pill never merges into
the ground it is meant to float over, and no hue was added to achieve it, only a
value on the axis that was already there.

Light mode is the opposite case: the page is already the brightest surface the
brand has, so there the pill is flat, shares the page's ground, and the shadow
does the lifting. This is the only gradient in the app that is not a
photographic scrim.

**The app lands on Shop, not Home, and Shop is the only screen with chrome over
it.** Onboarding hands straight to the Shop tab; Home still leads the bar,
because the bar's order is the reference's and where the app lands is a
separate decision. The storefront header and the campaign banner moved with the
landing rather than staying on the wall, and they had to: the header carries
`brandWordmark()` and the banner carries `campaignFrame()`, which are the
receiving ends of both crossings out of onboarding. A shared element whose
other end sits on a screen nobody navigated to degrades to nothing without
complaining. Home is the other half of that split — no bar, no tabs, no search,
no filter, status-bar padding instead of an overlay, and one page that runs
from the editorial to the end of the catalogue.

**Tab switching is deliberately dumb: `popUpTo(RouteShop) { inclusive = false }`
and nothing else.** No `saveState`, no `restoreState`. Those two were there to
keep each tab's place, and on a flat graph they cannot do it safely: `popUpTo`
saves everything above the anchor as ONE block keyed by the first entry it
popped, and the next navigate matching that key replays the whole block rather
than its root. The bag is the case that breaks — it is a tab AND a screen
pushed from the product's chrome, so the block can span two tabs. Four taps on
a device, no race and no unusual state: from Home open a product, open the bag
from its chrome, press HOME, and the bag is what comes back, with Bag lit in
the bar.

Without the two flags the stack states itself in one line — `[shop]`, or
`[shop, tab]`, with whatever you have opened sitting above it — and a tab press
lands on that tab and nothing else. A press on the tab you are already on does
nothing, rather than rebuilding the same screen and replaying its fade.

Popped to the shop rather than to the graph's start, because the start is
onboarding and onboarding leaves the stack the moment the shop opens; popping
to an entry that is not there would stack a tab on every press.

What that costs is the saved place, and the one worth keeping is hoisted
instead. Home's `LazyGridState` lives in `PerspectiveApp`, above the NavHost:
Home is the long page, and losing your place in the catalogue every time you
look in the bag is the bar undoing your work. The shop needs no equivalent — it
is the anchor, it is never popped, and its category, its query and its scroll
sit in its entry untouched. Search, the bag and the account start fresh, which
is what each of the three is for; a search is a question you ask now, not one
you left half-asked on another tab.

Absent on onboarding, product, sign-in and order, which is what keeps the
onboarding photograph full bleed and the product pager edge to edge.

Three things it replaced rather than joined: the account link in the wall's
footer, the BAG word in the account screen's top chrome, and the BAG word in the
storefront header. Two ways to one screen on one page is one too many.

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

The Play listing's flat 512x512 PNG lives at `store/play-icon-512.png`, which an
adaptive icon cannot supply. It was **rasterised from `ic_launcher_foreground.xml`
itself, never redrawn**, so the store tile and the launcher icon agree: nonzero
winding fill, 4x supersampled, with the 108dp viewport cropped to the 72dp the
launcher mask actually reveals before scaling, so the mark carries the same
weight on a store tile as on a home screen.

**This repository stays Kotlin and Android.** The rasteriser that produced the
PNG is deliberately NOT checked in — a one-off store asset does not justify a
second toolchain in a project that runs on three dependencies. If the launcher
vector ever changes, regenerate the PNG from it rather than editing the PNG,
and check the result against the geometry above.

## Status

Brand system complete, with a storefront on it that can be shopped end to
end: browse, choose a size, fill a bag, sign in, order.

- `ui/catalog/ProductWallScreen` — Home: nothing over it. Featured editorial,
  then the whole catalogue at 2 columns, zero gutter, 40dp row gap, full-bleed
  2:3 plates, label inset 12dp, footer at the end.
- `ui/product/ProductDetailScreen` — full-bleed pager with an n/N counter, name
  and price on one row, inline sizes with strike-through plus grey on
  unavailable, square outlined CTA, accordions.
- `ui/PerspectiveApp` — 56dp chrome carrying the wordmark, word labels instead
  of icons, and a NavHost over ten destinations: onboarding, shop, wall,
  search, product, bag, sign-in, account, order and licences. The chrome is
  only on the ones the bar does not carry. Onboarding hands to shop, and shop
  is the anchor every tab and every return pops back to.
- `ui/onboarding/OnboardingScreen` — full-bleed photography behind a graded
  scrim, one line of copy, one control. Light system-bar icons while it is up,
  because it is a photograph and the theme's own choice tracks a ground that is
  not there.
- `ui/bag/BagScreen` — live lines with a typographic quantity stepper and a
  REMOVE word, totals at body weight, and its own empty state.
- `ui/account/` — passwordless sign-in, the account with its order history, and
  the order confirmation, which doubles as the view of an older order.
- `ui/BottomNav` + `ui/NavIcons` — the five-tab floating pill, copied from
  Nike's. See Navigation.
- `ui/catalog/ShopScreen` — the landing. Storefront header over the grid —
  wordmark, ruled search field, category tabs — then the campaign banner and
  the category tiles, narrowing in place to a 2-column product grid. Tabs and
  tiles set one `Category?`; the ALL tab is what widens it again, so the
  `ALL CATEGORIES` word that used to do that is gone.
- `ui/catalog/SearchScreen` — the ruled field over a live-filtered grid, with
  the category tiles as a starting point before anything is typed. This one is
  extrapolated: the recording never opens its Search tab.

**The bag and the session are in memory and that is the honest size of them.**
`data/Bag` and `data/Session` are singletons holding snapshot state: they
outlive the composition, so rotation keeps them, and they do not outlive the
process, so a cold start opens an empty bag and a signed-out session. Making
either durable means somewhere to persist and an owner with a lifecycle, which
are decisions about dependencies this app has not taken — see the three it
runs on. Until it does, the small version that cannot pretend to be more is the
right one.

**No password, deliberately.** There is no backend, so a password field would
ask for a secret nothing can check and nothing should keep. That is security
theatre, and it would make the sign-in the one screen in the app that lies
about what it does.

**There is no checkout step and no payment.** CHECKOUT places the order
directly: nothing takes an address, calculates shipping or moves money, and the
bag screen shows one Total rather than a subtotal and a shipping line, because
a subtotal is only a subtotal of something. Orders are filed under the address,
so signing out hides them and signing back in with the same one finds them
again. Numbers run `PS-4417` upward in sequence, and a line stops at nine —
`Bag.MaxQuantity`, which the stepper reads so that `+` can visibly stop rather
than going quiet under the thumb.

**The account is in the footer, not the bar.** The bar already carries brand,
search and bag, and its own rule says a fourth control there would be one too
many. The footer is where the storefronts measured put the same thing.

**The first run is one continuous gesture, not three screens.** The mark
assembles, sets itself down on the onboarding block, and later rises into the
storefront bar; the photograph you are shown is the photograph the shop opens
on. Both halves are in the Motion section. The pieces are coupled on purpose
and the couplings are the fragile part: `BrandMoment` needs a reported target,
and the campaign frame index has to stay hoisted. Breaking either fails
silently rather than loudly.

Verified on a physical device and an emulator, in both modes, with animations
on and off — not just compiled. The shopping flow was walked end to end: add
two sizes, change a quantity, sign in at checkout, place the order, read it
back from the history, sign out and back in. Walked again at
`animator_duration_scale 0`, because the bag's removal is built on the
transition settling and that is the setting which makes it settle instantly,
and again at `10`, which is where a model that lagged its own animation was
caught placing an order for a line that had been removed.

Moving the landing to Shop was walked separately, on a physical device in both
modes, at `animator_duration_scale` 1 and 10: cold start into the shop, both
crossings arriving on it rather than on a screen nobody opened, tabs narrowing
in place, Home chrome-free with the status bar clear of the photograph, the
empty bag's THE SHOP returning to the shop with its filter intact, and Back
from the shop leaving the app rather than unwinding a stack of tabs. The
emulator has not been re-checked since that change.

The tab-switching change was walked the same way, and the four-tap bag
reproduction above was run against the old flags and the new ones to be sure
it was the flags: before, HOME opened the bag; after, HOME opens Home, still
at the scroll position it was left at. Then every tab in a lap — Home, Bag,
Profile, Search, Shop — with the shop's filter and Home's scroll both intact
at the end of it. **One thing was checked and turned out not to exist:** a
second reported defect, where pressing a tab from an open product was supposed
to restore the wrong screen, is not reachable — the product destination has no
bottom bar, so there is no tab to press. A first reading of it was a false
positive from tapping where the bar is not.

**Placeholder debt:** `ImagePlate.tone` alternates two near-identical fills purely
so adjacent plates are distinguishable while there is no photography. It is a
reviewing aid, not part of the system — delete the parameter when real images
land.
