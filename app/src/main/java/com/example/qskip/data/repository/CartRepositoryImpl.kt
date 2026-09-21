package com.example.qskip.data.repository

import com.example.qskip.domain.model.Cart
import com.example.qskip.domain.model.CartItem
import com.example.qskip.domain.model.Product
import com.example.qskip.domain.repository.CartRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
                val cart = snapshot.toObject(Cart::class.java) ?: Cart(userId = getUserId() ?: "")
                trySend(cart)
            } else {
                trySend(Cart(userId = getUserId() ?: ""))
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun addToCart(productId: String, quantity: Int): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val productSnapshot = firestore.collection("products").document(productId).get().await()

            if (!productSnapshot.exists()) {
                return Result.failure(Exception("Product not found"))
            }

            val product = productSnapshot.toObject(Product::class.java)!!

            if (product.stockQuantity < quantity) {
                return Result.failure(Exception("Only ${product.stockQuantity} units available for ${product.name} (${product.size}/${product.color})"))
            }

            val newItem = CartItem(
                productId = product.productId,
                productCode = product.productCode,
                name = product.name,
                size = product.size,
                color = product.color,
                unitPrice = product.price,
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
                val existingItemIndex = currentItems.indexOfFirst { it.productId == productId }

                if (existingItemIndex != -1) {
                    val existing = currentItems[existingItemIndex]
                    if (existing.quantity + quantity > product.stockQuantity) {
                        throw Exception("Cannot add more than available stock (${product.stockQuantity})")
                    }
                    currentItems[existingItemIndex] = existing.copy(quantity = existing.quantity + quantity)
                } else {
                    currentItems.add(newItem)
                }

                transaction.set(cartRef, cart.copy(items = currentItems), SetOptions.merge())
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        if (quantity <= 0) return removeFromCart(productId)

        return try {
            val productSnapshot = firestore.collection("products").document(productId).get().await()
            val product = productSnapshot.toObject(Product::class.java)
            
            if (product == null || product.stockQuantity < quantity) {
                return Result.failure(Exception("Requested quantity not available in stock"))
            }

            firestore.runTransaction { transaction ->
                val cartRef = firestore.collection("carts").document(userId)
                val snapshot = transaction.get(cartRef)
                
                if (snapshot.exists()) {
                    val cart = snapshot.toObject(Cart::class.java)!!
                    val updatedItems = cart.items.map {
                        if (it.productId == productId) {
                            it.copy(quantity = quantity)
                        } else it
                    }
                    transaction.set(cartRef, mapOf("items" to updatedItems), SetOptions.merge())
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            firestore.runTransaction { transaction ->
                val cartRef = firestore.collection("carts").document(userId)
                val snapshot = transaction.get(cartRef)
                
                if (snapshot.exists()) {
                    val cart = snapshot.toObject(Cart::class.java)!!
                    val updatedItems = cart.items.filterNot { it.productId == productId }
                    transaction.set(cartRef, mapOf("items" to updatedItems), SetOptions.merge())
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
            firestore.collection("carts").document(userId)
                .set(mapOf("items" to emptyList<CartItem>()), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setBudget(budget: Double): Result<Unit> {
        val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))
        return try {
            firestore.collection("carts").document(userId)
                .set(mapOf("budget" to budget, "userId" to userId), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
