package com.perspective.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perspective.app.ui.theme.Motion
import com.perspective.app.ui.theme.Space

/**
 * Geometry, measured off the reference rather than guessed.
 *
 * Taken from a 720x1600 screen recording of the app this copies, converted at
 * that device's 2.0 scale: the pill spans 624 of 720 pixels with a 48px side
 * margin and stands 115px tall, which is 312dp wide, 24dp in from each edge,
 * 58dp high. Its corners are not rounded, they are ends — the radius is half
 * the height, so [CircleShape] is literally correct and a RoundedCornerShape
 * with a number in it would be an approximation of something exact.
 */
private val PillHeight = 58.dp
private val PillMargin = 24.dp
private val PillLift = 16.dp

/**
 * What a scrolling screen under the bar has to clear.
 *
 * The pill floats, so content runs beneath it by design — but the LAST thing on
 * a page has to end above it or it cannot be read or reached.
 */
val BottomNavSpace = PillHeight + PillLift
private val IconSize = 24.dp
private val BadgeSize = 18.dp

/** Label size. The reference sets roughly 11sp; the type scale has nothing that small. */
private val LabelSize = 11.sp

/**
 * How far the pill's top edge climbs above its bottom, as a fraction of the
 * distance from [com.perspective.app.ui.theme.Night10] to
 * [com.perspective.app.ui.theme.Night35].
 *
 * A fifth lands on #272624, thirteen points above the bottom stop against the
 * reference's nine. Deliberately the stronger of the two: this pill sits on
 * true black and has further to lift than one sitting on #090B11.
 */
private const val GradeLift = 0.20f

/**
 * One top-level destination in the bar.
 *
 * Two icons rather than one with a tint: the reference swaps outline for solid
 * on selection, which is a change of shape and not of colour, and a single
 * vector cannot be both.
 */
data class NavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconFilled: ImageVector,
    /** Drawn as a filled badge over the icon when > 0. */
    val count: Int = 0,
)

/**
 * The bottom navigation, copied from the reference app rather than interpreted.
 *
 * **This is a deliberate departure from the brand rules, made on instruction.**
 * CLAUDE.md rules out the reference's icon bar by name, holds the app to words
 * where other apps use glyphs, and keeps every surface flat. This has icons, a
 * drop shadow and sentence-case labels in the detail face. It was asked for
 * twice, explicitly, against a recording of the original — the rules are the
 * owner's own document and he is entitled to overrule them. What is NOT copied
 * is the reference's trademarked mark: its Shop tab carries a swoosh, and
 * [NavIcons] draws a garment tag instead. A brand's rules can be waived by the
 * person who wrote them; a brand's trademark cannot.
 *
 * **It floats.** The pill is inset from both edges, lifted clear of the gesture
 * bar, and content runs underneath it — which is why the caller must give the
 * content the whole window and let this draw over it, not place it in a column
 * below. A bar that reaches the screen edges is the thing this replaced and is
 * what read as wrong.
 *
 * Selection is a change of weight, not of colour: the icon goes from outline to
 * solid and the label from regular to medium, both in the foreground ink. No
 * underline, no pill behind the selected tab, no accent — the reference has
 * none of those and neither does this.
 */
@Composable
fun PerspectiveBottomNav(
    tabs: List<NavTab>,
    currentRoute: String?,
    onSelect: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    // A shadow cannot lift a black pill off a black ground.
    //
    // Measured from the reference's dark mode: its pill is #1A1B1D against a
    // #090B11 page — lifted by VALUE, roughly 17 points, because in the dark
    // an elevation shadow has nothing to darken. Light mode is the opposite
    // case: the page is already the brightest surface the brand has, so there
    // the shadow does the lifting and the pill shares the page's ground.
    //
    // Night10 is #1A1918, within three points of the measurement, so this
    // needs no new hue — the token that draws hairlines in the dark is the
    // same value the reference lifts its bar by.
    //
    // It is GRADED rather than flat, which is the reference's own treatment:
    // sampled top to bottom its pill runs #1E1E20 to #151515, about nine
    // points, so the surface reads as catching light from above instead of
    // being a cut-out. This one runs a little harder than that because it has
    // further to climb — true black underneath, where the reference's page is
    // #090B11.
    //
    // Both stops come off the existing dark neutrals: the top is a fifth of
    // the way from Night10 toward Night35, the bottom is Night10 itself. So
    // the bottom edge still stands 26 points clear of the page and the pill
    // never merges into the ground it is supposed to float over — and no hue
    // was added to do it, only a value on the axis that was already there.
    val dark = isSystemInDarkTheme()
    val ground = if (dark) {
        Brush.verticalGradient(
            listOf(lerp(scheme.surfaceVariant, scheme.outline, GradeLift), scheme.surfaceVariant),
        )
    } else {
        SolidColor(scheme.surface)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = PillMargin)
            .padding(bottom = PillLift)
            // Shadow before background, or the shape clips the elevation it is
            // meant to cast.
            .shadow(elevation = Space.Sm, shape = CircleShape, clip = false)
            .background(brush = ground, shape = CircleShape)
            .height(PillHeight),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            tabs.forEach { tab ->
                NavTabCell(
                    tab = tab,
                    selected = tab.route == currentRoute,
                    onSelect = { onSelect(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NavTabCell(
    tab: NavTab,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val ink by animateColorAsState(
        targetValue = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
        animationSpec = Motion.quick(),
        label = "navInk",
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .selectable(selected = selected, role = Role.Tab, onClick = onSelect),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box {
            Icon(
                imageVector = if (selected) tab.iconFilled else tab.icon,
                contentDescription = null,
                tint = ink,
                modifier = Modifier.size(IconSize),
            )
            if (tab.count > 0) {
                Badge(count = tab.count)
            }
        }
        Text(
            text = tab.label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = LabelSize),
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = ink,
            maxLines = 1,
            modifier = Modifier.padding(top = Space.Xs / 2),
        )
    }
}

/**
 * The count, as a filled disc over the icon's top-right corner.
 *
 * Offset outward by a third of its own size so it breaks the icon's silhouette
 * rather than sitting inside it — a badge wholly inside the glyph reads as part
 * of the drawing. The reference does the same.
 */
@Composable
private fun BoxScope.Badge(count: Int) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = BadgeSize / 3, y = -BadgeSize / 3)
            .size(BadgeSize)
            .background(color = scheme.onSurface, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 9) "9+" else count.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
            color = scheme.surface,
            maxLines = 1,
        )
    }
}
