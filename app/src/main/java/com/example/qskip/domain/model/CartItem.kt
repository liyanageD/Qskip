package com.example.qskip.domain.model

data class CartItem(
    val productId: String = "",
    val productCode: String = "",
    val name: String = "",
    val size: String = "",
    val color: String = "",
    val unitPrice: Double = 0.0,
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
    val budget: Double = 0.0,
    val rewardPointsToUse: Int = 0
) {
    val subtotal: Double
        get() = items.sumOf { it.subtotal }
        
    val discountTotal: Double
        get() = 0.0
        
    val rewardsDiscount: Double
        get() = rewardPointsToUse * 0.5
        
    val total: Double
        get() = maxOf(0.0, subtotal - discountTotal - rewardsDiscount)
}
