package com.example.qskip.domain.model

data class Promotion(
    val promotionId: String = "",
    val title: String = "",
    val code: String = "",
    val discountType: String = "PERCENTAGE", // PERCENTAGE or FIXED
    val discountValue: Double = 0.0,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
