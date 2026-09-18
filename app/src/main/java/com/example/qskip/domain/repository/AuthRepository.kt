package com.example.qskip.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUserId(): String?
    fun isUserLoggedIn(): Boolean
    
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    
    fun getAuthState(): Flow<Boolean>
}
