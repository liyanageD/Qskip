package com.example.qskip.domain.repository

import com.example.qskip.domain.model.Flyer
import com.example.qskip.domain.model.Promotion
import com.example.qskip.domain.model.User
import kotlinx.coroutines.flow.Flow

data class RewardSettings(
    val pointsPerHundred: Int = 1,
    val discountPerPoint: Double = 0.5,
    val minPointsToRedeem: Int = 10
)

interface AdminRepository {
    fun getAllCustomers(): Flow<List<User>>
    suspend fun updateCustomerStatus(userId: String, newStatus: String): Result<Unit>
    
    fun getPromotions(): Flow<List<Promotion>>
    suspend fun savePromotion(promotion: Promotion): Result<Unit>
    suspend fun togglePromotionStatus(promotionId: String, active: Boolean): Result<Unit>
    
    fun getRewardSettings(): Flow<RewardSettings>
    suspend fun saveRewardSettings(settings: RewardSettings): Result<Unit>

    fun getFlyers(): Flow<List<Flyer>>
    suspend fun saveFlyer(flyer: Flyer): Result<Unit>
    suspend fun toggleFlyerStatus(flyerId: String, active: Boolean): Result<Unit>
    suspend fun deleteFlyer(flyerId: String): Result<Unit>
}
