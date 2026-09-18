package com.example.qskip.domain.repository

import com.example.qskip.domain.model.ExitToken
import com.example.qskip.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getUserOrders(): Flow<List<Order>>
    fun getOrderById(orderId: String): Flow<Order?>
    suspend fun getExitToken(tokenId: String): Result<ExitToken>
    suspend fun verifyExit(tokenId: String, staffId: String): Result<Unit>
    suspend fun flagExit(tokenId: String, staffId: String, reason: String): Result<Unit>
    
    // Admin functions
    fun getAllOrders(): Flow<List<Order>>
}
