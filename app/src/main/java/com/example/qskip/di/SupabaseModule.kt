package com.example.qskip.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    // For a real app, load these from BuildConfig or secrets
    private const val SUPABASE_URL = "https://iclqdrjrhkdihkqkcckb.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_iBAWjtMLGHHjKC5xM1HxUw_6OsvFA99"

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Storage)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseStorage(client: SupabaseClient): Storage {
        return client.storage
    }
}
