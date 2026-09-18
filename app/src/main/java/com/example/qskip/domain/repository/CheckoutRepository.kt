package com.example.qskip.domain.repository

import com.example.qskip.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface CheckoutRepository {
    suspend fun createOrderFromCart(): Result<Order>
    suspend fun processMockPayment(orderId: String): Result<Unit>
    suspend fun applyRewardPoints(points: Int): Result<Unit>
    fun getUserRewardPoints(): Flow<Int>
}
