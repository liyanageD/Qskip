package com.example.qskip.domain.model

data class CartItem(
    val productId: String,
    val variantId: String,
    val name: String,
    val size: String,
    val color: String,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String? = null
) {
    val subtotal: Double
        get() = (unitPrice - discount) * quantity
}

data class Cart(
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val budget: Double = 0.0, // Move to user document realistically, but helpful here for calculation
    val rewardPointsToUse: Int = 0
) {
    val subtotal: Double
        get() = items.sumOf { it.subtotal }
        
    val discountTotal: Double
        get() = 0.0 // To be implemented later (e.g. coupon logic)
        
    val rewardsDiscount: Double
        get() = rewardPointsToUse * 0.5 // E.g., 1 point = 0.5 Rs
        
    val total: Double
        get() = maxOf(0.0, subtotal - discountTotal - rewardsDiscount)
}
