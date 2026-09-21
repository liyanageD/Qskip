package com.example.qskip.domain.model

data class WishlistItem(
    val productId: String = "",
    val name: String = "",
    val categoryId: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val active: Boolean = true,
    val addedAt: Long = 0L
) {
    val basePrice: Double
        get() = price
}
