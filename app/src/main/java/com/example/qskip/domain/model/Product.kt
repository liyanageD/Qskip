package com.example.qskip.domain.model

data class Product(
    val productId: String = "",
    val productCode: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val imageUrls: List<String> = emptyList(),
    val basePrice: Double = 0.0,
    val active: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class ProductVariant(
    val variantId: String = "",
    val productId: String = "",
    val size: String = "",
    val color: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val lowStockThreshold: Int = 5,
    val active: Boolean = true
)

data class ProductWithVariants(
    val product: Product,
    val variants: List<ProductVariant>
)
