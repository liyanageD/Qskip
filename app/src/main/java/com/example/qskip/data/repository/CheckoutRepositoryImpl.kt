package com.example.qskip.data.repository

import com.example.qskip.domain.model.Cart
import com.example.qskip.domain.model.ExitToken
import com.example.qskip.domain.model.Order
import com.example.qskip.domain.repository.CheckoutRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckoutRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CheckoutRepository {

    private fun getUserId(): String? = auth.currentUser?.uid

    override suspend fun createOrderFromCart(): Result<Order> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val cartSnapshot = firestore.collection("carts").document(userId).get().await()
            val cart = cartSnapshot.toObject(Cart::class.java)

            if (cart == null || cart.items.isEmpty()) {
                return Result.failure(Exception("Cart is empty"))
            }

            val orderId = "QSK-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
            
            val order = Order(
                orderId = orderId,
                userId = userId,
                items = cart.items,
                subtotal = cart.subtotal,
                discount = cart.discountTotal,
                rewardDiscount = cart.rewardsDiscount,
                total = cart.total,
                paymentStatus = "PENDING",
                orderStatus = "CREATED",
                exitStatus = "NOT_READY",
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("orders").document(orderId).set(order).await()
            
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun processMockPayment(orderId: String): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

        return try {
            firestore.runTransaction { transaction ->
                // ==================== PHASE 1: ALL READS ====================
                // 1. Read Order
                val orderRef = firestore.collection("orders").document(orderId)
                val orderSnapshot = transaction.get(orderRef)
                val order = orderSnapshot.toObject(Order::class.java) 
                    ?: throw Exception("Order not found")

                if (order.paymentStatus != "PENDING") {
                    throw Exception("Order is not in pending state")
                }

                // 2. Read Inventory Products
                val productReadMap = mutableMapOf<DocumentReference, Long>()
                for (item in order.items) {
                    val productRef = firestore.collection("products").document(item.productId)
                    val productSnapshot = transaction.get(productRef)
                    val currentStock = productSnapshot.getLong("stockQuantity") ?: 0
                    if (currentStock < item.quantity) {
                        throw Exception("Insufficient stock for item: ${item.name} (${item.size}/${item.color}). Only $currentStock left in stock.")
                    }
                    productReadMap[productRef] = currentStock - item.quantity
                }

                // 3. Read User Rewards Profile
                val userRef = firestore.collection("users").document(userId)
                val userSnapshot = transaction.get(userRef)
                val currentPoints = userSnapshot.getLong("rewardPoints") ?: 0

                // ==================== PHASE 2: ALL WRITES ====================
                // 1. Generate Payment Ref and Exit Token
                val paymentRef = "PAY-${System.currentTimeMillis()}-${(1000..9999).random()}"
                val tokenId = "EXIT-" + UUID.randomUUID().toString().substring(0, 12).uppercase()
                
                val exitToken = ExitToken(
                    tokenId = tokenId,
                    orderId = orderId,
                    userId = userId
                )
                val tokenRef = firestore.collection("exitTokens").document(tokenId)
                transaction.set(tokenRef, exitToken)

                // 2. Update Order
                transaction.update(orderRef, mapOf(
                    "paymentStatus" to "MOCK_SUCCESS",
                    "orderStatus" to "PAID",
                    "exitStatus" to "READY",
                    "paymentReference" to paymentRef,
                    "exitTokenId" to tokenId
                ))

                // 3. Clear the Cart
                val cartRef = firestore.collection("carts").document(userId)
                transaction.update(cartRef, "items", emptyList<Any>())
                transaction.update(cartRef, "rewardPointsToUse", 0)

                // 4. Update Inventory
                for ((productRef, newStock) in productReadMap) {
                    transaction.update(productRef, "stockQuantity", newStock)
                }

                // 5. Award Reward Points (e.g., 1 point per 100 Rs spent)
                val pointsEarned = (order.total / 100).toLong()
                val pointsUsed = (order.rewardDiscount / 0.5).toLong() // 1 point = 0.5 Rs
                val finalPoints = currentPoints + pointsEarned - pointsUsed
                transaction.update(userRef, "rewardPoints", maxOf(0, finalPoints))
                
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun applyRewardPoints(points: Int): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val userSnapshot = firestore.collection("users").document(userId).get().await()
            val availablePoints = userSnapshot.getLong("rewardPoints")?.toInt() ?: 0
            
            if (points > availablePoints) {
                return Result.failure(Exception("Not enough reward points available"))
            }

            firestore.collection("carts").document(userId).update("rewardPointsToUse", points).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserRewardPoints(): Flow<Int> = callbackFlow {
        val userId = getUserId()
        if (userId == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val points = snapshot?.getLong("rewardPoints")?.toInt() ?: 0
                trySend(points)
            }

        awaitClose { listener.remove() }
    }
}
