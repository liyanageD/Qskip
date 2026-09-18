package com.example.qskip.data.repository

import com.example.qskip.domain.model.Product
import com.example.qskip.domain.model.WishlistItem
import com.example.qskip.domain.repository.WishlistRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : WishlistRepository {

    private fun getUserId(): String? = auth.currentUser?.uid

    private fun getWishlistCollectionRef() = getUserId()?.let { 
        firestore.collection("users").document(it).collection("wishlist")
    }

    override fun getWishlist(): Flow<List<WishlistItem>> = callbackFlow {
        val collectionRef = getWishlistCollectionRef()
        if (collectionRef == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = collectionRef.orderBy("addedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(WishlistItem::class.java)
                    trySend(items)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun addToWishlist(productId: String): Result<Unit> {
        val collectionRef = getWishlistCollectionRef() ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val productSnapshot = firestore.collection("products").document(productId).get().await()
            val product = productSnapshot.toObject(Product::class.java) 
                ?: return Result.failure(Exception("Product not found"))

            val wishlistItem = WishlistItem(
                productId = product.productId,
                name = product.name,
                categoryId = product.categoryId,
                basePrice = product.price,
                imageUrl = product.imageUrls.firstOrNull(),
                active = product.active,
                addedAt = System.currentTimeMillis()
            )

            collectionRef.document(productId).set(wishlistItem).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromWishlist(productId: String): Result<Unit> {
        val collectionRef = getWishlistCollectionRef() ?: return Result.failure(Exception("User not authenticated"))

        return try {
            collectionRef.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isInWishlist(productId: String): Result<Boolean> {
        val collectionRef = getWishlistCollectionRef() ?: return Result.success(false)

        return try {
            val snapshot = collectionRef.document(productId).get().await()
            Result.success(snapshot.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
