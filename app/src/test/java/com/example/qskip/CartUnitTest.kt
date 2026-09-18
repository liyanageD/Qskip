package com.example.qskip

import com.example.qskip.domain.model.Cart
import com.example.qskip.domain.model.CartItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CartUnitTest {

    @Test
    fun cart_subtotalCalculation_isCorrect() {
        val item1 = CartItem(
            productId = "P1",
            productCode = "TSHIRT-BLK-M",
            name = "Shirt",
            size = "M",
            color = "Blue",
            unitPrice = 2000.0,
            quantity = 2
        )
        val item2 = CartItem(
            productId = "P2",
            productCode = "JEANS-BLU-32",
            name = "Jeans",
            size = "32",
            color = "Black",
            unitPrice = 5000.0,
            quantity = 1
        )

        val cart = Cart(
            items = listOf(item1, item2)
        )

        assertEquals(9000.0, cart.subtotal, 0.01)
    }

    @Test
    fun cart_rewardDiscountCalculation_isCorrect() {
        val item = CartItem(
            productId = "P1",
            productCode = "TSHIRT-BLK-M",
            name = "Shirt",
            size = "M",
            color = "Blue",
            unitPrice = 2000.0,
            quantity = 1
        )

        val cart = Cart(
            items = listOf(item),
            rewardPointsToUse = 100 // 1 point = 0.5 Rs -> 50 Rs discount
        )

        assertEquals(50.0, cart.rewardsDiscount, 0.01)
        assertEquals(1950.0, cart.total, 0.01)
    }
}
