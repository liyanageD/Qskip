package com.example.qskip.data.repository

import com.example.qskip.domain.model.Product
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
                Product(
                    productCode = "TSHIRT-BLK-M",
                    name = "Classic T-Shirt",
                    description = "100% Cotton soft crewneck t-shirt. Ideal for casual everyday wear.",
                    categoryId = "T-Shirts",
                    size = "M",
                    color = "Black",
                    price = 3500.0,
                    stockQuantity = 10,
                    active = true
                ),
                Product(
                    productCode = "TSHIRT-BLK-L",
                    name = "Classic T-Shirt",
                    description = "100% Cotton soft crewneck t-shirt. Ideal for casual everyday wear.",
                    categoryId = "T-Shirts",
                    size = "L",
                    color = "Black",
                    price = 3500.0,
                    stockQuantity = 7,
                    active = true
                ),
                Product(
                    productCode = "TSHIRT-WHT-M",
                    name = "Classic T-Shirt",
                    description = "100% Cotton soft crewneck t-shirt. Ideal for casual everyday wear.",
                    categoryId = "T-Shirts",
                    size = "M",
                    color = "White",
                    price = 3500.0,
                    stockQuantity = 5,
                    active = true
                ),
                Product(
                    productCode = "JEANS-BLU-32",
                    name = "Slim Fit Jeans",
                    description = "Premium denim slim fit jeans with modern stretch comfort.",
                    categoryId = "Jeans",
                    size = "32",
                    color = "Blue",
                    price = 5500.0,
                    stockQuantity = 12,
                    active = true
                ),
                Product(
                    productCode = "SHIRT-NVY-M",
                    name = "Casual Button-Down Shirt",
                    description = "Breathable linen blend casual button-down shirt.",
                    categoryId = "Shirts",
                    size = "M",
                    color = "Navy",
                    price = 3500.0,
                    stockQuantity = 8,
                    active = true
                ),
                Product(
                    productCode = "DRESS-RED-S",
                    name = "Summer Floral Dress",
                    description = "Lightweight sleeveless A-line summer dress with floral pattern.",
                    categoryId = "Dresses",
                    size = "S",
                    color = "Red Floral",
                    price = 4500.0,
                    stockQuantity = 2,
                    active = true
                )
            )

            for (product in sampleProducts) {
                productRepository.saveProduct(product)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
