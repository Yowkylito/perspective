package com.perspective.app.data

data class SizeOption(
    val label: String,
    val available: Boolean = true,
)

data class Product(
    val id: String,
    val name: String,
    val colourway: String,
    val price: Int,
    val sizes: List<SizeOption>,
    val description: String,
    val imageCount: Int = 4,
)

/** Price as a plain string. No currency-symbol styling, no decimals on whole amounts. */
fun Int.asPrice(): String = "₱" + "%,d".format(this)

/**
 * Placeholder catalogue. Names are deliberately generic — copying a real
 * retailer's product names is the kind of borrowing the brand rules rule out.
 */
object SampleCatalogue {
    private fun sizes(vararg unavailable: String) =
        listOf("XXS", "XS", "S", "M", "L", "XL").map {
            SizeOption(it, available = it !in unavailable)
        }

    val items = listOf(
        Product("01", "Boxed Rule Shirt", "Undyed Cotton", 7_450, sizes("XXS", "XL"),
            "Cut straight through the body with a half-placket and a single patch pocket. Woven from undyed long-staple cotton that softens with each wash."),
        Product("02", "Reversed Seam Trouser", "Ink", 9_900, sizes("XS"),
            "A wide, flat-fronted trouser with the seams turned outward. Dry handle, holds a crease without pressing."),
        Product("03", "Monotype Knit", "Bone", 12_300, sizes(),
            "Fine-gauge merino worked on a single bed, with set-in sleeves and a narrow rib at the cuff and hem."),
        Product("04", "Counterform Coat", "Charcoal", 24_800, sizes("XXS", "XS", "L"),
            "An unstructured single-breasted coat in a dense wool melton. Falls below the knee and takes a layer underneath without strain."),
        Product("05", "Negative Space Tee", "Undyed Cotton", 3_200, sizes(),
            "A heavyweight jersey tee with a bound neck and dropped shoulder."),
        Product("06", "Baseline Trouser", "Flax", 8_600, sizes("M"),
            "Tapered through the leg with a flat waistband and no belt loops."),
        Product("07", "Mirror Shirt Jacket", "Olive", 14_200, sizes("XXS"),
            "Cut as an overshirt in a washed cotton twill, with two flapped chest pockets."),
        Product("08", "Kerning Scarf", "Bone", 4_900, listOf(SizeOption("One size")),
            "A long rectangular scarf in brushed lambswool, finished with hand-knotted fringing."),
    )

    fun byId(id: String): Product? = items.firstOrNull { it.id == id }
}
