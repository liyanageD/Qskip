package com.example.qskip.data.repository

import com.example.qskip.domain.model.Promotion
import com.example.qskip.domain.model.Review
import com.example.qskip.domain.model.User
import com.example.qskip.domain.repository.AdminRepository
import com.example.qskip.domain.repository.RewardSettings
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminRepository {

    override fun getAllCustomers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateCustomerStatus(userId: String, newStatus: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .update("accountStatus", newStatus)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPromotions(): Flow<List<Promotion>> = callbackFlow {
        val listener = firestore.collection("promotions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val promos = snapshot?.toObjects(Promotion::class.java) ?: emptyList()
                trySend(promos)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun savePromotion(promotion: Promotion): Result<Unit> {
        return try {
            val id = if (promotion.promotionId.isBlank()) {
                firestore.collection("promotions").document().id
            } else {
                promotion.promotionId
            }
            val promo = promotion.copy(promotionId = id)
            firestore.collection("promotions").document(id).set(promo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun togglePromotionStatus(promotionId: String, active: Boolean): Result<Unit> {
        return try {
            firestore.collection("promotions").document(promotionId)
                .update("active", active)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getReviews(): Flow<List<Review>> = callbackFlow {
        val listener = firestore.collection("reviews")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val reviews = snapshot?.toObjects(Review::class.java) ?: emptyList()
                trySend(reviews)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun toggleReviewVisibility(reviewId: String, active: Boolean): Result<Unit> {
        return try {
            firestore.collection("reviews").document(reviewId)
                .update("active", active)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRewardSettings(): Flow<RewardSettings> = callbackFlow {
        val listener = firestore.collection("appSettings").document("rewards")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val settings = snapshot?.toObject(RewardSettings::class.java) ?: RewardSettings()
                trySend(settings)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveRewardSettings(settings: RewardSettings): Result<Unit> {
        return try {
            firestore.collection("appSettings").document("rewards").set(settings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
