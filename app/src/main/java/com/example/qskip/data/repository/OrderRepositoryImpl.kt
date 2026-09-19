package com.example.qskip.data.repository

import com.example.qskip.domain.model.ExitToken
import com.example.qskip.domain.model.Order
import com.example.qskip.domain.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : OrderRepository {

    private fun getUserId(): String? = auth.currentUser?.uid

    override fun getUserOrders(): Flow<List<Order>> = callbackFlow {
        val userId = getUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val orders = snapshot?.toObjects(Order::class.java) ?: emptyList()
                trySend(orders.sortedByDescending { it.createdAt })
            }

        awaitClose { listener.remove() }
    }

    override fun getOrderById(orderId: String): Flow<Order?> = callbackFlow {
        val listener = firestore.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val order = snapshot?.toObject(Order::class.java)
                trySend(order)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getExitToken(tokenId: String): Result<ExitToken> {
        return try {
            val snapshot = firestore.collection("exitTokens").document(tokenId).get().await()
            val token = snapshot.toObject(ExitToken::class.java)
                ?: return Result.failure(Exception("Exit token not found"))
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyExit(tokenId: String, staffId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val tokenRef = firestore.collection("exitTokens").document(tokenId)
                val tokenSnapshot = transaction.get(tokenRef)
                val token = tokenSnapshot.toObject(ExitToken::class.java)
                    ?: throw Exception("Invalid exit token")

                if (token.status == "VERIFIED") {
                    throw Exception("Exit QR already used.")
                }

                val orderRef = firestore.collection("orders").document(token.orderId)
                val orderSnapshot = transaction.get(orderRef)
                val order = orderSnapshot.toObject(Order::class.java)
                    ?: throw Exception("Associated order not found")

                if (order.paymentStatus != "MOCK_SUCCESS" && order.paymentStatus != "SUCCESS") {
                    throw Exception("Payment not completed for this order.")
                }

                val now = System.currentTimeMillis()

                // Mark Token as VERIFIED
                transaction.update(tokenRef, mapOf(
                    "status" to "VERIFIED",
                    "verifiedAt" to now,
                    "verifiedBy" to staffId
                ))

                // Mark Order as COMPLETED / EXIT_VERIFIED
                transaction.update(orderRef, mapOf(
                    "exitStatus" to "VERIFIED",
                    "orderStatus" to "COMPLETED",
                    "verifiedAt" to now,
                    "verifiedBy" to staffId
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun flagExit(tokenId: String, staffId: String, reason: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val tokenRef = firestore.collection("exitTokens").document(tokenId)
                val tokenSnapshot = transaction.get(tokenRef)
                val token = tokenSnapshot.toObject(ExitToken::class.java)
                    ?: throw Exception("Invalid token")

                val orderRef = firestore.collection("orders").document(token.orderId)
                val orderSnapshot = transaction.get(orderRef)

                val now = System.currentTimeMillis()

                transaction.update(tokenRef, mapOf(
                    "status" to "FLAGGED",
                    "verifiedAt" to now,
                    "verifiedBy" to staffId,
                    "flaggedReason" to reason
                ))

                transaction.update(orderRef, mapOf(
                    "exitStatus" to "FLAGGED",
                    "orderStatus" to "FLAGGED",
                    "flaggedReason" to reason,
                    "verifiedBy" to staffId,
                    "verifiedAt" to now
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllOrders(): Flow<List<Order>> = callbackFlow {
        val listener = firestore.collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val orders = snapshot?.toObjects(Order::class.java) ?: emptyList()
                trySend(orders.sortedByDescending { it.createdAt })
            }

        awaitClose { listener.remove() }
    }
}
