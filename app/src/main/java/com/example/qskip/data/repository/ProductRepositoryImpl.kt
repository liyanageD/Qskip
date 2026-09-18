package com.example.qskip.data.repository

import com.example.qskip.domain.model.Product
import com.example.qskip.domain.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProductRepository {

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("active", true)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.let { p ->
                    if (p.productId.isBlank()) p.copy(productId = doc.id) else p
                }
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductById(productId: String): Result<Product> {
        return try {
            val productSnapshot = firestore.collection("products").document(productId).get().await()
            if (!productSnapshot.exists()) {
                return Result.failure(Exception("Product not found"))
            }
            val product = productSnapshot.toObject(Product::class.java)?.let { p ->
                if (p.productId.isBlank()) p.copy(productId = productSnapshot.id) else p
            } ?: return Result.failure(Exception("Failed to parse product"))
            
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductByCode(productCode: String): Result<Product> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("productCode", productCode)
                .limit(1)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                return Result.failure(Exception("Product not found with code: $productCode"))
            }
            
            val document = snapshot.documents.first()
            val product = document.toObject(Product::class.java)?.let { p ->
                if (p.productId.isBlank()) p.copy(productId = document.id) else p
            } ?: return Result.failure(Exception("Failed to parse product"))
            
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("active", true)
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.let { p ->
                    if (p.productId.isBlank()) p.copy(productId = doc.id) else p
                }
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRecommendedProducts(): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("active", true)
                .limit(10)
                .get()
                .await()
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.let { p ->
                    if (p.productId.isBlank()) p.copy(productId = doc.id) else p
                }
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveProduct(product: Product): Result<Unit> {
        return try {
            val productId = if (product.productId.isBlank()) {
                firestore.collection("products").document().id
            } else {
                product.productId
            }

            val updatedProduct = product.copy(
                productId = productId,
                updatedAt = System.currentTimeMillis(),
                createdAt = if (product.createdAt == 0L) System.currentTimeMillis() else product.createdAt
            )

            firestore.collection("products").document(productId).set(updatedProduct).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStock(productId: String, newStock: Int): Result<Unit> {
        return try {
            firestore.collection("products").document(productId)
                .update("stockQuantity", newStock)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            firestore.collection("products").document(productId)
                .update("active", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
