package com.example.qskip.domain.model

data class Review(
    val reviewId: String = "",
    val productId: String = "",
    val productName: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
