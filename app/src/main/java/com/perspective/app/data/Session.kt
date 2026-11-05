package com.perspective.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Who is signed in. There is no password here and there is nothing to check. */
data class Account(val name: String, val email: String)

private val Placed: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)

/**
 * The signed-in session and the orders placed in it.
 *
 * **Passwordless, and that is a decision rather than a shortcut.** There is no
 * backend, so a password field would be asking for a secret nothing can check
 * and nothing should keep — security theatre, which is worse than none, and
 * which would also make this the one screen in the app that lies about what it
 * does. An email and a way forward is the shape several of the storefronts this
 * takes after actually use, and it is honest at this size.
 *
 * Orders are filed under the address, so signing out hides them and signing
 * back in with the same address finds them again. Like [Bag], none of it
 * outlives the process.
 */
object Session {
    var account by mutableStateOf<Account?>(null)
        private set

    val signedIn: Boolean get() = account != null

    private val placed = mutableStateListOf<Order>()

    /**
     * Newest first — an order history is read from the top — and only this
     * account's.
     *
     * Orders are filed under the address rather than under the session, so
     * signing out and back in with the same address finds them again. The first
     * version cleared them on sign-out, which made the sentence above false:
     * they belonged to whoever happened to be signed in, which is not what
     * belonging means.
     */
    val orders: List<Order>
        get() = account?.let { signed -> placed.filter { it.account == signed.email } }.orEmpty()

    fun signIn(email: String) {
        val trimmed = email.trim()
        account = Account(name = nameFrom(trimmed), email = trimmed)
    }

    fun signOut() {
        account = null
    }

    /**
     * Moves the bag into an order and empties it. Returns the order so the
     * caller can route to it by reference rather than by position.
     */
    fun placeOrder(): Order? {
        if (Bag.isEmpty) return null
        val signed = account ?: return null
        val order = Order(
            reference = OrderNumbers.take(),
            placed = LocalDate.now().format(Placed),
            lines = Bag.lines.toList(),
            total = Bag.subtotal,
            account = signed.email,
        )
        placed.add(0, order)
        Bag.clear()
        return order
    }

    fun order(reference: String): Order? = orders.firstOrNull { it.reference == reference }

    /**
     * A display name from the local part of the address, so the account screen
     * has something to greet. "ada.lovelace@example.com" reads as Ada Lovelace;
     * anything that does not split cleanly is left as it was typed.
     */
    private fun nameFrom(email: String): String {
        val local = email.substringBefore('@').ifBlank { return email }
        return local
            .split('.', '_', '-')
            .filter { it.isNotBlank() }
            .joinToString(" ") { part ->
                part.replaceFirstChar { it.titlecase(Locale.UK) }
            }
            .ifBlank { email }
    }
}
