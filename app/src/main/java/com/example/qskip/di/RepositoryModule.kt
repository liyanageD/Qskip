package com.example.qskip.di

import com.example.qskip.data.repository.AuthRepositoryImpl
import com.example.qskip.data.repository.CartRepositoryImpl
import com.example.qskip.data.repository.CheckoutRepositoryImpl
import com.example.qskip.data.repository.ProductRepositoryImpl
import com.example.qskip.data.repository.WishlistRepositoryImpl
import com.example.qskip.domain.repository.AuthRepository
import com.example.qskip.domain.repository.CartRepository
import com.example.qskip.domain.repository.CheckoutRepository
import com.example.qskip.domain.repository.ProductRepository
import com.example.qskip.domain.repository.WishlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.example.qskip.data.repository.OrderRepositoryImpl
import com.example.qskip.domain.repository.OrderRepository

import com.example.qskip.data.repository.ImageStorageRepositoryImpl
import com.example.qskip.domain.repository.ImageStorageRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository
    
    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindWishlistRepository(
        wishlistRepositoryImpl: WishlistRepositoryImpl
    ): WishlistRepository

    @Binds
    @Singleton
    abstract fun bindCheckoutRepository(
        checkoutRepositoryImpl: CheckoutRepositoryImpl
    ): CheckoutRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindImageStorageRepository(
        imageStorageRepositoryImpl: ImageStorageRepositoryImpl
    ): ImageStorageRepository
}
