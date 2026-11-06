package com.perspective.app.ui.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.perspective.app.ui.BottomNavSpace
import com.perspective.app.data.Order
import com.perspective.app.data.Session
import com.perspective.app.data.asPrice
import com.perspective.app.ui.components.PerspectiveButton
import com.perspective.app.ui.theme.Space

/**
 * The account, and what has been ordered from it.
 *
 * Name and address first, as one block — the name is derived from the address,
 * so neither is a heading for the other — then the history, newest first, in
 * the order [Session] hands it over.
 *
 * **An order reads as a row, not a card.** Reference and date at the start,
 * total at the end, a hairline between each: the same anatomy as a bag line,
 * because an order and a bag are the same thing seen at two different moments.
 * Cards would be a second container shape in an app whose only container is a
 * rule, and a chevron at the end of each row would be the icon asset the chrome
 * has spent the whole app avoiding — the row is tappable because it is a row of
 * one thing, which is how every other list here already behaves.
 *
 * **Signing out takes the orders with it.** That is [Session]'s decision and
 * the right one; they belong to the account. The consequence lands here, and is
 * worth stating plainly: the moment the control is pressed this screen has
 * nothing left to draw and falls through its own guard, so it empties under
 * whatever transition carries it away. It is deliberately not given a copy of
 * the account to hold while it leaves — that would be the page showing
 * something that has stopped being true in order to make an exit look smoother.
 * If that reads badly on device, the fix belongs in the navigation that owns
 * the order of the two calls, not in a stale cache here.
 *
 * **No confirmation dialog on the way out.** This app has no dialog anywhere,
 * and Material's is a rounded container with filled text buttons in it — three
 * things the system does not otherwise own, introduced for one use. What sign
 * out costs is also one session's orders, none of which survive the process in
 * the first place.
 *
 * The signed-out branch is a guard rather than a state: navigation reaches here
 * only when someone is signed in, so copy for the other case would be a screen
 * nobody can arrive at.
 */
@Composable
fun AccountScreen(
    onOrder: (reference: String) -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val account = Session.account ?: return
    val orders = Session.orders
    val rule = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Space.Gutter),
    ) {
        Spacer(Modifier.height(Space.Lg))
        Text(text = "ACCOUNT", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(Space.Lg))
        Text(text = account.name, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(Space.Xs))
        Text(
            text = account.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Space.Xl))
        Text(text = "ORDERS", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(Space.Md))

        if (orders.isEmpty()) {
            // One line, and it does not apologise or invite. An empty history
            // is a fact about this account, not a prompt to go and fix it.
            Text(
                text = "Nothing ordered yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // Ruled above each row and closed under the last, so the list is
            // bounded at both ends rather than trailing into the sign-out
            // control below it.
            orders.forEach { order ->
                HorizontalDivider(thickness = Space.Hairline, color = rule)
                OrderRow(order = order, onClick = { onOrder(order.reference) })
            }
            HorizontalDivider(thickness = Space.Hairline, color = rule)
        }

        Spacer(Modifier.height(Space.Xl))
        PerspectiveButton(
            label = "SIGN OUT",
            onClick = {
                Session.signOut()
                onSignedOut()
            },
        )

        // The page's own end margin plus the floating bar's, since this is a
        // tab and the pill is drawn over it.
        Spacer(Modifier.height(Space.Xxl + BottomNavSpace))
    }
}

/**
 * One order in the history.
 *
 * Date and piece count share a line for the reason a bag line puts size and
 * quantity on one: both are small facts about the same order, and stacking them
 * would give each the weight of a heading. The total sits at the end at body
 * size and regular weight, as every price in this app does.
 */
@Composable
private fun OrderRow(order: Order, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Space.Md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = order.reference, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(Space.Xs))
            Text(
                text = "${order.placed}    ${pieces(order.pieces)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(Space.Md))
        Text(text = order.total.asPrice(), style = MaterialTheme.typography.bodyMedium)
    }
}

/** Pieces, not lines — the same count the chrome keeps for the bag. */
private fun pieces(count: Int): String = if (count == 1) "1 piece" else "$count pieces"
