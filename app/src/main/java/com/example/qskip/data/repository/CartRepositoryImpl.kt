package com.example.qskip.data.repository

import com.example.qskip.domain.model.Cart
import com.example.qskip.domain.model.CartItem
import com.example.qskip.domain.model.Product
import com.example.qskip.domain.model.ProductVariant
import com.example.qskip.domain.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CartRepository {

    private fun getUserId(): String? = auth.currentUser?.uid
    
    private fun getCartDocRef() = getUserId()?.let { firestore.collection("carts").document(it) }

    override fun getCart(): Flow<Cart> = callbackFlow {
        val docRef = getCartDocRef()
        if (docRef == null) {
            trySend(Cart())
            close()
            return@callbackFlow
        }

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                // To keep it simple, we serialize the Cart object directly if possible,
                // but since Firestore structure might need custom parsing for lists:
                val cart = snapshot.toObject(Cart::class.java) ?: Cart(userId = getUserId() ?: "")
                trySend(cart)
            } else {
                trySend(Cart(userId = getUserId() ?: ""))
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun addToCart(productId: String, variantId: String, quantity: Int): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            // First, fetch product and variant details to construct the CartItem
            val productSnapshot = firestore.collection("products").document(productId).get().await()
            val variantSnapshot = firestore.collection("products").document(productId)
                .collection("variants").document(variantId).get().await()

            if (!productSnapshot.exists() || !variantSnapshot.exists()) {
                return Result.failure(Exception("Product or variant not found"))
            }

            val product = productSnapshot.toObject(Product::class.java)!!
            val variant = variantSnapshot.toObject(ProductVariant::class.java)!!

            if (variant.stock < quantity) {
                return Result.failure(Exception("Not enough stock available"))
            }

            val newItem = CartItem(
                productId = productId,
                variantId = variantId,
                name = product.name,
                size = variant.size,
                color = variant.color,
                unitPrice = variant.price,
                imageUrl = product.imageUrls.firstOrNull(),
                quantity = quantity
            )

            firestore.runTransaction { transaction ->
                val cartRef = firestore.collection("carts").document(userId)
                val snapshot = transaction.get(cartRef)
                
                val cart = if (snapshot.exists()) {
                    snapshot.toObject(Cart::class.java) ?: Cart(userId = userId)
                } else {
                    Cart(userId = userId)
                }

                val currentItems = cart.items.toMutableList()
                val existingItemIndex = currentItems.indexOfFirst { it.productId == productId && it.variantId == variantId }

                if (existingItemIndex != -1) {
                    val existing = currentItems[existingItemIndex]
                    if (existing.quantity + quantity > variant.stock) {
                        throw Exception("Cannot add more than available stock")
                    }
                    currentItems[existingItemIndex] = existing.copy(quantity = existing.quantity + quantity)
                } else {
                    currentItems.add(newItem)
                }

                transaction.set(cartRef, cart.copy(items = currentItems))
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateQuantity(productId: String, variantId: String, quantity: Int): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        if (quantity <= 0) return removeFromCart(productId, variantId)

        return try {
            // Check stock first
            val variantSnapshot = firestore.collection("products").document(productId)
                .collection("variants").document(variantId).get().await()
            val variant = variantSnapshot.toObject(ProductVariant::class.java)
            
            if (variant == null || variant.stock < quantity) {
                return Result.failure(Exception("Requested quantity not available in stock"))
            }

            firestore.runTransaction { transaction ->
                val cartRef = firestore.collection("carts").document(userId)
                val snapshot = transaction.get(cartRef)
                
                if (snapshot.exists()) {
                    val cart = snapshot.toObject(Cart::class.java)!!
                    val updatedItems = cart.items.map {
                        if (it.productId == productId && it.variantId == variantId) {
                            it.copy(quantity = quantity)
                        } else it
                    }
                    transaction.update(cartRef, "items", updatedItems)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromCart(productId: String, variantId: String): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            firestore.runTransaction { transaction ->
                val cartRef = firestore.collection("carts").document(userId)
                val snapshot = transaction.get(cartRef)
                
                if (snapshot.exists()) {
                    val cart = snapshot.toObject(Cart::class.java)!!
                    val updatedItems = cart.items.filterNot { it.productId == productId && it.variantId == variantId }
                    transaction.update(cartRef, "items", updatedItems)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCart(): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        return try {
            firestore.collection("carts").document(userId).update("items", emptyList<CartItem>()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setBudget(budget: Double): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        return try {
            firestore.collection("carts").document(userId).update("budget", budget).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
