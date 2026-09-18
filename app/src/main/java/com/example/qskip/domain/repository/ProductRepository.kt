package com.example.qskip.domain.repository

import com.example.qskip.domain.model.Product
import com.example.qskip.domain.model.ProductVariant
import com.example.qskip.domain.model.ProductWithVariants

interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getProductById(productId: String): Result<ProductWithVariants>
    suspend fun getProductByCode(productCode: String): Result<ProductWithVariants>
    suspend fun getProductVariants(productId: String): Result<List<ProductVariant>>
    suspend fun searchProducts(query: String): Result<List<Product>>
    suspend fun getRecommendedProducts(): Result<List<Product>>
}
