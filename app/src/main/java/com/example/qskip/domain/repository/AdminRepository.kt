package com.example.qskip.domain.repository

import com.example.qskip.domain.model.Promotion
import com.example.qskip.domain.model.Review
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
    
    fun getReviews(): Flow<List<Review>>
    suspend fun toggleReviewVisibility(reviewId: String, active: Boolean): Result<Unit>
    
    fun getRewardSettings(): Flow<RewardSettings>
    suspend fun saveRewardSettings(settings: RewardSettings): Result<Unit>
}
