package com.perspective.app.data

/** One line of the bag: a product in a chosen size, at a quantity. */
data class BagLine(
    val product: Product,
    val size: String,
    val quantity: Int,
)

/**
 * A standing bag, so the screen has something to be.
 *
 * Mock data, like [SampleCatalogue] — there is no cart service, nothing
 * persists, and adding from the product detail does not reach this. It exists
 * so the bag can be designed against real line lengths rather than lorem.
 */
object SampleBag {
    val lines: List<BagLine> = listOfNotNull(
        SampleCatalogue.byId("01")?.let { BagLine(it, "M", 1) },
        SampleCatalogue.byId("04")?.let { BagLine(it, "L", 1) },
        SampleCatalogue.byId("08")?.let { BagLine(it, "One size", 2) },
    )

    val subtotal: Int get() = lines.sumOf { it.product.price * it.quantity }
}
