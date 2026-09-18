package com.example.qskip.domain.repository

import com.example.qskip.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUserId(): String?
    fun isUserLoggedIn(): Boolean
    
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun adminLogin(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    
    fun getAuthState(): Flow<Boolean>
    fun getUserProfile(): Flow<User?>
    suspend fun getUserProfileOnce(): Result<User?>
    suspend fun updateUserBudget(budget: Double): Result<Unit>
}
