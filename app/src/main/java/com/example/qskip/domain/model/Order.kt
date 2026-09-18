package com.example.qskip.domain.model

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val rewardDiscount: Double = 0.0,
    val total: Double = 0.0,
    val paymentStatus: String = "PENDING", // PENDING, SUCCESS, FAILED, MOCK_SUCCESS
    val orderStatus: String = "CREATED",   // CREATED, PAID, EXIT_PENDING, EXIT_VERIFIED, COMPLETED, CANCELLED
    val paymentReference: String? = null,
    val exitTokenId: String? = null,
    val exitStatus: String = "NOT_READY",  // NOT_READY, READY, VERIFIED
    val createdAt: Long = System.currentTimeMillis()
)

data class ExitToken(
    val tokenId: String = "",
    val orderId: String = "",
    val userId: String = "",
    val status: String = "READY", // READY, VERIFIED, EXPIRED
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 1000 * 60 * 60 * 2 // 2 hours
)
