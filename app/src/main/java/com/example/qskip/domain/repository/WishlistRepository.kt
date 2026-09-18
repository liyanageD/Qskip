package com.example.qskip.domain.repository

import com.example.qskip.domain.model.WishlistItem
import kotlinx.coroutines.flow.Flow

interface WishlistRepository {
    fun getWishlist(): Flow<List<WishlistItem>>
    suspend fun addToWishlist(productId: String): Result<Unit>
    suspend fun removeFromWishlist(productId: String): Result<Unit>
    suspend fun isInWishlist(productId: String): Result<Boolean>
}
