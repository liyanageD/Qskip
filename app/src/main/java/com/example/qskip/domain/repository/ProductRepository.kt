package com.example.qskip.domain.repository

import com.example.qskip.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductsFlow(): Flow<List<Product>>
    fun getProductByIdFlow(productId: String): Flow<Product?>
    fun getProductByCodeFlow(productCode: String): Flow<Product?>
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getProductById(productId: String): Result<Product>
    suspend fun getProductByCode(productCode: String): Result<Product>
    suspend fun searchProducts(query: String): Result<List<Product>>
    suspend fun getRecommendedProducts(): Result<List<Product>>
    
    // Admin operations
    suspend fun saveProduct(product: Product): Result<Unit>
    suspend fun updateStock(productId: String, newStock: Int): Result<Unit>
    suspend fun deleteProduct(productId: String): Result<Unit>
}
