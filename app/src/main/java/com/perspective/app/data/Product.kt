package com.perspective.app.data

/**
 * Wall filters. Labels are the tab text, so they are uppercase here rather than
 * uppercased at the call site -- the display face is always set in caps and
 * doing it in one place keeps a stray lowercase tab from ever appearing.
 */
enum class Category(val label: String) {
    Tops("TOPS"),
    Trousers("TROUSERS"),
    Outerwear("OUTERWEAR"),
    Accessories("ACCESSORIES"),
}

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
    val category: Category,
    /** Stand-in photography; see [MockImages]. Empty until a catalogue fills it. */
    val images: List<String> = emptyList(),
) {
    val imageCount: Int get() = images.size
}

/**
 * Stand-in photography, taken from the brand's own Facebook catalogue and
 * cropped to [com.perspective.app.ui.theme.Ratio.Product].
 *
 * These are campaign and lookbook frames, not per-product shots: there is no
 * plate that belongs to any one garment, so a product's four pager frames are
 * the seven plates rotated by its position. That is mock data standing in for a
 * backend, not a scheme - delete this object the moment per-product photography
 * exists, along with the assets under `assets/mock/`.
 */
object MockImages {
    val plates: List<String> = (1..7).map { "file:///android_asset/mock/%02d.jpg".format(it) }

    /**
     * Campaign frames for the banner: street photography, no overlaid type.
     *
     * These carry the boxed typewriter wordmark on the garment itself rather
     * than a caption set over the image, so the banner needs no words of its
     * own and no scrim to fight. The studio posters they replaced moved to
     * [collage], where their burned-in typography is the artwork rather than
     * something to crop around.
     */
    val banners: List<String> = (1..3).map { "file:///android_asset/mock/banner/b%d.jpg".format(it) }

    /**
     * The three frames of the editorial block, in composition order: lead,
     * then the two stacked stills. Order matters here in a way it does not
     * for [plates] -- the first is cropped 3:4 for the lead cell and the
     * other two 2:3 for the column beside it.
     */
    val collage: List<String> = (1..3).map { "file:///android_asset/mock/collage/c%d.jpg".format(it) }

    /** Four frames for the product at [index], distinct within the product. */
    fun forIndex(index: Int, frames: Int = 4): List<String> =
        List(frames) { plates[(index + it) % plates.size] }
}

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

    private val drafts = listOf(
        Product("01", "Boxed Rule Shirt", "Undyed Cotton", 7_450, sizes("XXS", "XL"),
            "Cut straight through the body with a half-placket and a single patch pocket. Woven from undyed long-staple cotton that softens with each wash.",
            category = Category.Tops),
        Product("02", "Reversed Seam Trouser", "Ink", 9_900, sizes("XS"),
            "A wide, flat-fronted trouser with the seams turned outward. Dry handle, holds a crease without pressing.",
            category = Category.Trousers),
        Product("03", "Monotype Knit", "Bone", 12_300, sizes(),
            "Fine-gauge merino worked on a single bed, with set-in sleeves and a narrow rib at the cuff and hem.",
            category = Category.Tops),
        Product("04", "Counterform Coat", "Charcoal", 24_800, sizes("XXS", "XS", "L"),
            "An unstructured single-breasted coat in a dense wool melton. Falls below the knee and takes a layer underneath without strain.",
            category = Category.Outerwear),
        Product("05", "Negative Space Tee", "Undyed Cotton", 3_200, sizes(),
            "A heavyweight jersey tee with a bound neck and dropped shoulder.",
            category = Category.Tops),
        Product("06", "Baseline Trouser", "Flax", 8_600, sizes("M"),
            "Tapered through the leg with a flat waistband and no belt loops.",
            category = Category.Trousers),
        Product("07", "Mirror Shirt Jacket", "Olive", 14_200, sizes("XXS"),
            "Cut as an overshirt in a washed cotton twill, with two flapped chest pockets.",
            category = Category.Outerwear),
        Product("08", "Kerning Scarf", "Bone", 4_900, listOf(SizeOption("One size")),
            "A long rectangular scarf in brushed lambswool, finished with hand-knotted fringing.",
            category = Category.Accessories),
    )

    /** Plates are attached by position, so the wall never shows two neighbours alike. */
    val items: List<Product> = drafts.mapIndexed { i, p -> p.copy(images = MockImages.forIndex(i)) }

    fun byId(id: String): Product? = items.firstOrNull { it.id == id }
}
