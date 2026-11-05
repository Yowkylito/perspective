package com.perspective.app.data

import androidx.compose.runtime.mutableStateListOf

/** One line of the bag: a product in a chosen size, at a quantity. */
data class BagLine(
    val product: Product,
    val size: String,
    val quantity: Int,
) {
    /** What makes two lines the same line. A size is a different thing to own. */
    val key: String get() = "${product.id}/$size"
}

/**
 * The bag, for the length of one run of the app.
 *
 * **In memory, deliberately, and this is not what you would ship.** It is a
 * singleton holding snapshot state: it survives rotation because it outlives the
 * composition, and it does not survive the process, so a cold start opens an
 * empty bag. A real one needs somewhere to persist and an owner with a
 * lifecycle, and both are decisions about dependencies that this app has not
 * taken yet — see the three it currently runs on. Until then the honest version
 * is the small one that cannot pretend to be more.
 *
 * Snapshot state rather than a plain list so the screens recompose when it
 * changes; `androidx.compose.runtime` is already here and is not a UI
 * dependency, so `data` stays free of anything that draws.
 */
object Bag {
    /**
     * The most of any one line. A bag is not a wholesale order.
     *
     * Public because the screen needs it too: the cap is enforced here whatever
     * the screen believes, but a control that stops has to be able to SAY it has
     * stopped rather than going quiet under the thumb, and a second copy of the
     * number is how that drifts.
     */
    const val MaxQuantity = 9

    private val entries = mutableStateListOf<BagLine>()

    val lines: List<BagLine> get() = entries

    /** Pieces, not lines — what the chrome counts. */
    val count: Int get() = entries.sumOf { it.quantity }

    val subtotal: Int get() = entries.sumOf { it.product.price * it.quantity }

    val isEmpty: Boolean get() = entries.isEmpty()

    /**
     * Adds one, merging into the line that already holds this product in this
     * size. Two taps of ADD TO BAG on the same size is one line of two, not two
     * lines of one — the second reading would make the bag a log of what you
     * pressed rather than a statement of what you are buying.
     *
     * Returns whether anything actually went in, so the control that called it
     * can say ADDED only when it is true. A button that confirms a press it
     * dropped at the ceiling is the component lying on the app's behalf, which
     * is the same rule that decides where the haptic lives.
     */
    fun add(product: Product, size: String): Boolean {
        val at = entries.indexOfFirst { it.product.id == product.id && it.size == size }
        if (at < 0) {
            entries.add(BagLine(product = product, size = size, quantity = 1))
            return true
        }
        val line = entries[at]
        if (line.quantity >= MaxQuantity) return false
        entries[at] = line.copy(quantity = line.quantity + 1)
        return true
    }

    /**
     * Relative, not absolute, and that is the point.
     *
     * A stepper that computes `quantity + 1` from what it last composed and
     * sends the answer loses the second of two taps inside one frame: both read
     * the same stale number and both write it. Asking for a direction instead
     * makes the arithmetic happen where the truth is.
     */
    fun nudge(key: String, by: Int) {
        val at = entries.indexOfFirst { it.key == key }
        if (at < 0) return
        setQuantity(key, entries[at].quantity + by)
    }

    /** Quantity below one removes the line; there is no zero-quantity state. */
    fun setQuantity(key: String, quantity: Int) {
        val at = entries.indexOfFirst { it.key == key }
        if (at < 0) return
        if (quantity < 1) entries.removeAt(at)
        else entries[at] = entries[at].copy(quantity = quantity.coerceAtMost(MaxQuantity))
    }

    fun remove(key: String) {
        entries.removeAll { it.key == key }
    }

    fun clear() = entries.clear()

}

/** An order placed in this session. */
data class Order(
    val reference: String,
    val placed: String,
    val lines: List<BagLine>,
    val total: Int,
    /** The address it was filed under. See [com.perspective.app.data.Session]. */
    val account: String,
) {
    val pieces: Int get() = lines.sumOf { it.quantity }
}

/**
 * Order references.
 *
 * Sequential within the run rather than random: a reference that jumps around
 * looks generated, and the point of showing one at all is that it reads as a
 * thing the shop has recorded.
 */
internal object OrderNumbers {
    private var next = 4417

    fun take(): String = "PS-${next++}"
}
