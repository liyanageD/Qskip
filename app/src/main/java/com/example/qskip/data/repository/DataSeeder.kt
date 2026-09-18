package com.example.qskip.data.repository

import com.example.qskip.domain.model.Product
import com.example.qskip.domain.model.ProductVariant
import com.example.qskip.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend fun seedSampleProducts(): Result<Unit> {
        return try {
            val sampleProducts = listOf(
                Pair(
                    Product(
                        productCode = "QSK-TSHIRT-001",
                        name = "Classic T-Shirt",
                        description = "100% Cotton soft crewneck t-shirt. Ideal for casual everyday wear.",
                        basePrice = 2500.0,
                        active = true
                    ),
                    listOf(
                        ProductVariant(size = "S", color = "Black", price = 2500.0, stock = 10),
                        ProductVariant(size = "M", color = "Black", price = 2500.0, stock = 5),
                        ProductVariant(size = "L", color = "Black", price = 2500.0, stock = 8),
                        ProductVariant(size = "S", color = "White", price = 2500.0, stock = 4),
                        ProductVariant(size = "M", color = "White", price = 2500.0, stock = 7),
                        ProductVariant(size = "L", color = "White", price = 2500.0, stock = 3)
                    )
                ),
                Pair(
                    Product(
                        productCode = "QSK-JEANS-002",
                        name = "Slim Fit Jeans",
                        description = "Premium denim slim fit jeans with modern stretch comfort.",
                        basePrice = 5500.0,
                        active = true
                    ),
                    listOf(
                        ProductVariant(size = "30", color = "Blue", price = 5500.0, stock = 6),
                        ProductVariant(size = "32", color = "Blue", price = 5500.0, stock = 12),
                        ProductVariant(size = "34", color = "Blue", price = 5500.0, stock = 4),
                        ProductVariant(size = "32", color = "Dark Wash", price = 5500.0, stock = 8)
                    )
                ),
                Pair(
                    Product(
                        productCode = "QSK-SHIRT-003",
                        name = "Casual Button-Down Shirt",
                        description = "Breathable linen blend casual button-down shirt.",
                        basePrice = 3500.0,
                        active = true
                    ),
                    listOf(
                        ProductVariant(size = "M", color = "Navy", price = 3500.0, stock = 9),
                        ProductVariant(size = "L", color = "Navy", price = 3500.0, stock = 6),
                        ProductVariant(size = "M", color = "Beige", price = 3500.0, stock = 5)
                    )
                ),
                Pair(
                    Product(
                        productCode = "QSK-DRESS-004",
                        name = "Summer Floral Dress",
                        description = "Lightweight sleeveless A-line summer dress with floral pattern.",
                        basePrice = 4500.0,
                        active = true
                    ),
                    listOf(
                        ProductVariant(size = "S", color = "Red Floral", price = 4500.0, stock = 5),
                        ProductVariant(size = "M", color = "Red Floral", price = 4500.0, stock = 7),
                        ProductVariant(size = "M", color = "Yellow Floral", price = 4500.0, stock = 2) // Low stock threshold test
                    )
                )
            )

            for ((product, variants) in sampleProducts) {
                productRepository.saveProduct(product, variants)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
