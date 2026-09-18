package com.example.qskip.data.repository

import com.example.qskip.domain.model.Product
import com.example.qskip.domain.model.ProductVariant
import com.example.qskip.domain.model.ProductWithVariants
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
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductById(productId: String): Result<ProductWithVariants> {
        return try {
            val productSnapshot = firestore.collection("products").document(productId).get().await()
            if (!productSnapshot.exists()) {
                return Result.failure(Exception("Product not found"))
            }
            val product = productSnapshot.toObject(Product::class.java) ?: return Result.failure(Exception("Failed to parse product"))
            
            val variantsSnapshot = firestore.collection("products").document(productId)
                .collection("variants")
                .whereEqualTo("active", true)
                .get()
                .await()
            val variants = variantsSnapshot.toObjects(ProductVariant::class.java)
            
            Result.success(ProductWithVariants(product, variants))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductByCode(productCode: String): Result<ProductWithVariants> {
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
            val product = document.toObject(Product::class.java) ?: return Result.failure(Exception("Failed to parse product"))
            
            val variantsSnapshot = document.reference
                .collection("variants")
                .whereEqualTo("active", true)
                .get()
                .await()
            val variants = variantsSnapshot.toObjects(ProductVariant::class.java)
            
            Result.success(ProductWithVariants(product, variants))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductVariants(productId: String): Result<List<ProductVariant>> {
        return try {
            val snapshot = firestore.collection("products").document(productId)
                .collection("variants")
                .whereEqualTo("active", true)
                .get()
                .await()
            val variants = snapshot.toObjects(ProductVariant::class.java)
            Result.success(variants)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchProducts(query: String): Result<List<Product>> {
        // Basic prefix search using Firestore. 
        // For production, Algolia, Typesense, or Firebase Extensions (Elastic) is recommended.
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("active", true)
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .get()
                .await()
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRecommendedProducts(): Result<List<Product>> {
        // Simplified recommendation: return the newest active products
        return try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("active", true)
                // .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING) // Requires Index
                .limit(5)
                .get()
                .await()
            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveProduct(product: Product, variants: List<ProductVariant>): Result<Unit> {
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

            val batch = firestore.batch()
            val productRef = firestore.collection("products").document(productId)
            batch.set(productRef, updatedProduct)

            variants.forEach { variant ->
                val variantId = if (variant.variantId.isBlank()) {
                    productRef.collection("variants").document().id
                } else {
                    variant.variantId
                }
                val updatedVariant = variant.copy(
                    variantId = variantId,
                    productId = productId
                )
                val variantRef = productRef.collection("variants").document(variantId)
                batch.set(variantRef, updatedVariant)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateVariantStock(productId: String, variantId: String, newStock: Int): Result<Unit> {
        return try {
            firestore.collection("products").document(productId)
                .collection("variants").document(variantId)
                .update("stock", newStock)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            // Soft delete by setting active = false
            firestore.collection("products").document(productId)
                .update("active", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
