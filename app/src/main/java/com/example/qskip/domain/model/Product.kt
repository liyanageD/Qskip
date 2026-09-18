package com.example.qskip.domain.model

data class Product(
    val productId: String = "",
    val productCode: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "General",
    val size: String = "M",
    val color: String = "Black",
    val price: Double = 0.0,
    val stockQuantity: Int = 0,
    val lowStockThreshold: Int = 5,
    val imageUrls: List<String> = emptyList(),
    val active: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
