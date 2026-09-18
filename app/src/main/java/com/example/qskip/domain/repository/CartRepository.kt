package com.example.qskip.domain.repository

import com.example.qskip.domain.model.Cart
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCart(): Flow<Cart>
    suspend fun addToCart(productId: String, quantity: Int = 1): Result<Unit>
    suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit>
    suspend fun removeFromCart(productId: String): Result<Unit>
    suspend fun clearCart(): Result<Unit>
    suspend fun setBudget(budget: Double): Result<Unit>
}
