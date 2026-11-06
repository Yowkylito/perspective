package com.perspective.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Five navigation marks, drawn in code.
 *
 * The same reasoning that keeps the wordmark out of a bitmap applies here, and
 * more sharply: an icon set is where an app's drawing conventions either agree
 * or quietly stop agreeing. Vectors authored in one file share one grid, one
 * stroke and one pair of terminals, and a change to any of those is a change to
 * all ten marks rather than ten files to remember. Nothing is shipped as a
 * resource, nothing arrives from `material-icons`, and no asset exists that a
 * designer could replace one of without the other nine noticing.
 *
 * `Color.Black` is the authored ink and never the rendered ink. These are plain
 * vals, not composables; the call site tints them. Reading `MaterialTheme` in
 * here would put a `@Composable` in front of every accessor and hand the bar a
 * new vector on every recomposition, which is the opposite of what the caching
 * below is for.
 *
 * ## How the five are held together
 *
 * **One optical box: 19 units of ink, centred, 2.5 clear on every side.** Cap
 * line at y 2.5, baseline at y 21.5, centre at x 12. Material's own grid uses
 * two boxes — 20 across for round forms, 18 for square ones — so that a circle
 * and a square read the same size. That correction is right, and it is the
 * wrong tool here: these five have to agree with each other, not with Material,
 * and five marks reconciled to one number are easier to keep honest than five
 * reconciled to two. 19 is the midpoint, and the forms are tuned inside it.
 *
 * **No optical overshoot on the round terminals.** Type sets round glyphs a
 * hair past the baseline because at text sizes the eye catches it. Here the
 * correction would be about 0.3 of a unit, which at 24dp is under one physical
 * pixel even at xxhdpi — too small to land as correction and large enough, once
 * it rounds the wrong way, to land as a drawing error. So the magnifier's
 * handle tip and the profile's shoulders stop on the same line the house, the
 * tag and the bag sit on.
 *
 * **Widths vary, because forms do.** The house, the tag and the magnifier fill
 * the box; the bag is 15 across because a bag taller than it is wide is the
 * only thing that reads as a bag; the profile is 19 at the shoulders and 9.5 at
 * the head. Height is what the eye compares in a row. Width is what tells the
 * five apart.
 *
 * **Round caps and joins everywhere, and that is load bearing.** A round join
 * puts every corner's ink exactly half a stroke from its vertex, in every
 * direction. That is what makes the 19-unit box exact instead of approximate:
 * the house apex and the tag's corners are acute, and under a miter join they
 * would each throw ink a different distance past the line they were drawn to.
 *
 * **Every solid is authored to the outer edge of its own outline, not to the
 * outline's centreline.** Selecting a tab fills a mark in; it must not also
 * resize it. The cost is that a filled corner is sharp where the stroked one is
 * rounded — about a tenth of a unit, which is the one difference worth taking,
 * because the alternative is a silhouette that jumps when you press it.
 *
 * Deliberately absent: `autoMirror`. A magnifier and a tag both point somewhere,
 * and in a right-to-left layout they conventionally point the other way. This
 * app ships two faces carrying latin and latin-ext only, so it has no RTL build
 * to mirror for; turning the flag on now would be a behaviour nothing can be
 * checked against. Turn it on with the locale, not before.
 */
object NavIcons {

    /**
     * A house: pitched roof over a block body, one closed silhouette.
     *
     * The eave overhang went in and came out again. To read as an eave rather
     * than as a kink in the roofline it has to stand at least two stroke widths
     * clear of the wall, and buying that much overhang inside a 19-unit box
     * means dropping the body until the whole mark reads as a hut. The body is
     * therefore a block rather than the square it would be at a larger size —
     * 17.5 across by 10.5 down, under a roof pitched at 0.8.
     */
    val Home: ImageVector by lazy { outline("NavHome", HomeOutline) }

    val HomeFilled: ImageVector by lazy { solid("NavHomeFilled", HomeSolid) }

    /**
     * A garment swing tag, long axis on the diagonal, hole at the narrow end.
     *
     * A hanger was the other candidate and it fails at the second version: a
     * hanger is a line object, so filling it has nothing to fill, and the solid
     * state comes out a triangle with a hook on it. A tag has a body, which
     * means it has a silhouette, which means the selected state is the same
     * drawing with its interior closed. A shoe was never a candidate — it says
     * footwear, and this shop sells clothing.
     *
     * The 45 degrees is not decoration. It is what puts the nose's two edges on
     * the horizontal and the vertical, which is the detail that separates a tag
     * from the house standing next to it; upright, the two silhouettes converge.
     *
     * The hole is the whole mark. Below a 1.5 centreline radius the 1.5 stroke
     * closes it up and the tag becomes a chip, so that is the floor, and it sits
     * 6.2 along the axis rather than hard against the nose — far enough that the
     * ring keeps a clear unit between itself and both wedge edges.
     */
    val Shop: ImageVector by lazy { outline("NavShop", ShopOutline) }

    /**
     * Punched with [PathFillType.EvenOdd] rather than wound backwards, because
     * the hole is the one feature doing the identifying and a winding rule is a
     * quiet way to lose it.
     */
    val ShopFilled: ImageVector by lazy {
        solid("NavShopFilled", ShopSolid, PathFillType.EvenOdd)
    }

    /**
     * A magnifier: lens on the box's top-left, handle running out to its
     * bottom-right corner.
     *
     * The lens carries the mark and the handle only points it — 15.5 of ink
     * across against about 7 of tail. A longer handle balances the two and
     * costs the lens the size it needs to still read as a lens at 24dp.
     *
     * The handle starts on the rim's centreline, not outside it, so the two
     * strokes overlap by half a width each and the joint closes. Starting it at
     * the rim's outer edge leaves a seam that only appears on device.
     */
    val Search: ImageVector by lazy { outline("NavSearch", SearchOutline) }

    /**
     * Ring becomes disc, handle stays a handle. This is the one pair where the
     * outline and the solid share an identical silhouette and differ only in
     * whether the interior is closed — which is what the other four are
     * approximating.
     */
    val SearchFilled: ImageVector by lazy { solid("NavSearchFilled", SearchSolid) }

    /**
     * A carrier bag: tapered body, handle standing above the top edge.
     *
     * The taper is 5.7 degrees a side. More and it reads as a bucket; none at
     * all and it reads as a box, which is a parcel and belongs to a shipping
     * tracker rather than a shop.
     *
     * The handle takes nearly a third of the height, which is what keeps the
     * body at 15 by 14 rather than the portrait it would otherwise be. Drawing
     * the handle inside the body — the usual way to buy that height back —
     * makes the mark a rectangle with an arc inscribed in it, and at this size
     * that is a bag only if you already know it is one.
     *
     * The legs stop exactly on the body's top edge so their round caps and that
     * edge's stroke occupy the same band, and the junction has no seam.
     */
    val Bag: ImageVector by lazy { outline("NavBag", BagOutline) }

    /**
     * The body goes solid; the handle stays a 1.5-wide staple, drawn as its own
     * closed loop rather than a stroke. A solid handle is a lump, and the arc is
     * the only thing in the mark that says the bag is carried.
     */
    val BagFilled: ImageVector by lazy { solid("NavBagFilled", BagSolid) }

    /**
     * A head over shoulders, with 1.2 units of ground between them.
     *
     * That gap is the mark. Closed up, the two become one blob and the figure
     * stops being a figure; much wider and the head floats free of what is
     * holding it.
     *
     * The shoulders are a 148-degree arc rather than a semicircle, so they leave
     * the baseline close to vertical and only flatten across the top — the
     * difference between shoulders and a dome.
     */
    val Profile: ImageVector by lazy { outline("NavProfile", ProfileOutline) }

    /**
     * Solid disc, and the shoulders fill in as the region the arc bounds rather
     * than as a thickened arc. Outline shows a boundary; solid shows what the
     * boundary contains. The outer silhouette is unchanged either way.
     */
    val ProfileFilled: ImageVector by lazy { solid("NavProfileFilled", ProfileSolid) }
}

// ---------------------------------------------------------------------------
// The grid
// ---------------------------------------------------------------------------

private val IconSize = 24.dp
private const val Viewport = 24f

/**
 * One stroke for all five, which is most of why they read as a set.
 *
 * Every outline coordinate below is a centreline, so ink reaches half a stroke
 * beyond it. The box constants are stated in ink and the paths are drawn at
 * [Cap] + [Half] and [Base] - [Half]; keeping that arithmetic visible is what
 * stops the next mark being drawn to the wrong one of the two.
 */
private const val StrokeWidth = 1.5f
private const val Half = StrokeWidth / 2f

/** Topmost ink. */
private const val Cap = 2.5f

/** Bottommost ink. All five sit on it. */
private const val Base = 21.5f

/** Leftmost and rightmost ink for a mark that fills the box. */
private const val Edge = 2.5f
private const val EdgeEnd = 21.5f

/** Centre. */
private const val Mid = 12f

/** The same four values as centrelines, for stroked paths. */
private const val CapC = Cap + Half          // 3.25
private const val BaseC = Base - Half        // 20.75
private const val EdgeC = Edge + Half        // 3.25
private const val EdgeEndC = EdgeEnd - Half  // 20.75

/** Bezier circle constant. A quarter arc is this fraction of its radius. */
private const val Kappa = 0.5523f

// ---------------------------------------------------------------------------
// Paths
//
// Held as vals rather than inlined so the two halves of each pair sit next to
// each other and can be compared on the page. The solids are authored at the
// outline's outer ink edge — see the note on the object.
// ---------------------------------------------------------------------------

/** Apex, down the right slope, down the wall, across the floor, up the left. */
private val HomeOutline: PathBuilder.() -> Unit = {
    moveTo(Mid, CapC)
    lineTo(EdgeEndC, 10.25f)
    lineTo(EdgeEndC, BaseC)
    lineTo(EdgeC, BaseC)
    lineTo(EdgeC, 10.25f)
    close()
}

private val HomeSolid: PathBuilder.() -> Unit = {
    moveTo(Mid, Cap)
    lineTo(EdgeEnd, 10.1f)
    lineTo(EdgeEnd, Base)
    lineTo(Edge, Base)
    lineTo(Edge, 10.1f)
    close()
}

/**
 * Nose at the bottom-left corner, body running up to the top-right. The two
 * edges leaving the nose are axis-aligned because the tag's own point is a
 * right angle and the whole shape is turned 45 degrees.
 */
private val ShopOutline: PathBuilder.() -> Unit = {
    moveTo(EdgeC, BaseC)
    lineTo(EdgeC, 12.41f)
    lineTo(12.41f, CapC)
    lineTo(EdgeEndC, 11.59f)
    lineTo(11.59f, BaseC)
    close()
    circle(7.63f, 16.37f, 1.5f)
}

private val ShopSolid: PathBuilder.() -> Unit = {
    moveTo(Edge, Base)
    lineTo(Edge, 12.44f)
    lineTo(12.44f, Cap)
    lineTo(EdgeEnd, 11.56f)
    lineTo(11.56f, Base)
    close()
    // Centre and radius are the stroked ring's, unscaled, while the body around
    // it grows by half a stroke. The hole reads as a hole either way; a hole
    // that moved half a unit when the tab was pressed would read as a twitch.
    circle(7.63f, 16.37f, 1.5f)
}

/** Lens, then a handle along the diagonal from the rim to the far corner. */
private val SearchOutline: PathBuilder.() -> Unit = {
    circle(10.25f, 10.25f, 7f)
    moveTo(15.2f, 15.2f)
    lineTo(BaseC, BaseC)
}

/**
 * Disc, then the handle as a capsule: two parallel edges half a stroke either
 * side of the same diagonal, closed by the same semicircular cap the stroked
 * version ends with. Its back edge is buried inside the disc, and both subpaths
 * wind the same way so non-zero fill unions them instead of punching one.
 */
private val SearchSolid: PathBuilder.() -> Unit = {
    circle(10.25f, 10.25f, 7.75f)
    moveTo(14.53f, 13.47f)
    lineTo(21.28f, 20.22f)
    arcTo(Half, Half, 0f, false, true, 20.22f, 21.28f)
    lineTo(13.47f, 14.53f)
    close()
}

/** Body, then the handle as a separate open staple standing on its top edge. */
private val BagOutline: PathBuilder.() -> Unit = {
    moveTo(6.5f, 8.25f)
    lineTo(17.5f, 8.25f)
    lineTo(18.75f, BaseC)
    lineTo(5.25f, BaseC)
    close()

    moveTo(9.4f, 8.25f)
    lineTo(9.4f, 5.85f)
    arcTo(2.6f, 2.6f, 0f, false, true, 14.6f, 5.85f)
    lineTo(14.6f, 8.25f)
}

/**
 * The staple is one closed loop: out along the far edge, over the top on the
 * outer radius, back along the near edge and under the top on the inner one.
 * Both radii are the stroked version's centreline radius give or take half a
 * stroke, so the two versions' handles occupy the same band.
 */
private val BagSolid: PathBuilder.() -> Unit = {
    moveTo(5.8f, 7.5f)
    lineTo(18.2f, 7.5f)
    lineTo(19.5f, Base)
    lineTo(4.5f, Base)
    close()

    moveTo(8.65f, 8.25f)
    lineTo(8.65f, 5.85f)
    arcTo(3.35f, 3.35f, 0f, false, true, 15.35f, 5.85f)
    lineTo(15.35f, 8.25f)
    lineTo(13.85f, 8.25f)
    lineTo(13.85f, 5.85f)
    arcTo(1.85f, 1.85f, 0f, false, false, 10.15f, 5.85f)
    lineTo(10.15f, 8.25f)
    close()
}

private val ProfileOutline: PathBuilder.() -> Unit = {
    circle(Mid, 7.35f, 4.1f)
    moveTo(EdgeC, BaseC)
    arcTo(9.1f, 9.1f, 0f, false, true, EdgeEndC, BaseC)
}

/** The arc's ends are squared off onto the baseline; the stroked ones are capped. */
private val ProfileSolid: PathBuilder.() -> Unit = {
    circle(Mid, 7.35f, 4.85f)
    moveTo(Edge, 20.65f)
    arcTo(9.85f, 9.85f, 0f, false, true, EdgeEnd, 20.65f)
    lineTo(EdgeEnd, Base)
    lineTo(Edge, Base)
    close()
}

// ---------------------------------------------------------------------------
// Construction
// ---------------------------------------------------------------------------

/**
 * A circle, clockwise from the top.
 *
 * Four cubics rather than two arcs, because the winding is then something the
 * code states rather than something an arc flag decides. Every solid here
 * unions overlapping subpaths under the non-zero rule, which turns an opposed
 * winding into a hole — the exact failure that is invisible in source and
 * obvious on screen.
 */
private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    val k = r * Kappa
    moveTo(cx, cy - r)
    curveTo(cx + k, cy - r, cx + r, cy - k, cx + r, cy)
    curveTo(cx + r, cy + k, cx + k, cy + r, cx, cy + r)
    curveTo(cx - k, cy + r, cx - r, cy + k, cx - r, cy)
    curveTo(cx - r, cy - k, cx - k, cy - r, cx, cy - r)
    close()
}

/**
 * The unselected form. Stroke settings live here and nowhere else, so the five
 * cannot drift apart one argument at a time.
 */
private fun outline(name: String, draw: PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = IconSize,
        defaultHeight = IconSize,
        viewportWidth = Viewport,
        viewportHeight = Viewport,
    ).path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = StrokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = draw,
    ).build()

/** The selected form. Fill only — a solid carrying a stroke would grow by half of one. */
private fun solid(
    name: String,
    draw: PathBuilder.() -> Unit,
    fillType: PathFillType = PathFillType.NonZero,
): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = IconSize,
        defaultHeight = IconSize,
        viewportWidth = Viewport,
        viewportHeight = Viewport,
    ).path(
        fill = SolidColor(Color.Black),
        pathFillType = fillType,
        pathBuilder = draw,
    ).build()
