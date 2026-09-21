package com.example.qskip.data.repository

import com.example.qskip.domain.model.Flyer
import com.example.qskip.domain.model.Product
import com.example.qskip.domain.repository.AdminRepository
import com.example.qskip.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSeeder @Inject constructor(
    private val productRepository: ProductRepository,
    private val adminRepository: AdminRepository
) {
    suspend fun seedSampleProducts(): Result<Unit> {
        return try {
            val sampleProducts = listOf(
                Product(
                    productCode = "TSHIRT-BLK-M",
                    name = "Classic T-Shirt",
                    description = "100% Cotton soft crewneck t-shirt. Ideal for casual everyday wear.",
                    categoryId = "Men",
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
                    categoryId = "Men",
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
                    categoryId = "Men",
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
                    categoryId = "Men",
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
                    categoryId = "Men",
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
                    categoryId = "Women",
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

            val sampleFlyers = listOf(
                Flyer(
                    flyerId = "FLYER_1",
                    title = "Summer 2026 Collection",
                    subtitle = "Up to 40% off selected styles",
                    imageUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=800",
                    active = true,
                    displayOrder = 1
                ),
                Flyer(
                    flyerId = "FLYER_2",
                    title = "Trending Fashion Styles",
                    subtitle = "Discover new arrivals in Men & Women",
                    imageUrl = "https://images.unsplash.com/photo-1445205170230-053b83016050?w=800",
                    active = true,
                    displayOrder = 2
                ),
                Flyer(
                    flyerId = "FLYER_3",
                    title = "Exclusive Weekend Offer",
                    subtitle = "Earn double reward points on all items",
                    imageUrl = "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=800",
                    active = true,
                    displayOrder = 3
                )
            )

            for (flyer in sampleFlyers) {
                adminRepository.saveFlyer(flyer)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
