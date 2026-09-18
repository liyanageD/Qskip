package com.example.qskip.data.repository

import com.example.qskip.domain.model.User
import com.example.qskip.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val message = e.localizedMessage ?: "Login failed"
            val error = if (message.contains("Configuration not found", ignoreCase = true)) {
                "Firebase Auth is not enabled in Firebase Console for 'qskip-ae2bb'. Please enable Email/Password provider under Authentication -> Sign-in method."
            } else {
                message
            }
            Result.failure(Exception(error))
        }
    }

    override suspend fun adminLogin(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("Authentication failed")

            val snapshot = firestore.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)

            if (user == null || (user.role != "ADMIN" && user.role != "STAFF")) {
                auth.signOut() // Sign back out if not an admin/staff
                return Result.failure(Exception("Access denied. This account does not have administrator privileges."))
            }

            Result.success(user)
        } catch (e: Exception) {
            val message = e.localizedMessage ?: "Admin login failed"
            val error = if (message.contains("Configuration not found", ignoreCase = true)) {
                "Firebase Auth is not enabled in Firebase Console for 'qskip-ae2bb'. Please enable Email/Password provider under Authentication -> Sign-in method."
            } else {
                message
            }
            Result.failure(Exception(error))
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Force role = "CUSTOMER" for all registrations
                val userData = User(
                    uid = user.uid,
                    name = name,
                    email = email,
                    role = "CUSTOMER",
                    rewardPoints = 0,
                    budget = 0.0,
                    notificationEnabled = true,
                    accountStatus = "ACTIVE",
                    createdAt = System.currentTimeMillis()
                )
                firestore.collection("users").document(user.uid).set(userData).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val message = e.localizedMessage ?: "Registration failed"
            val error = if (message.contains("Configuration not found", ignoreCase = true)) {
                "Firebase Auth is not enabled in Firebase Console for 'qskip-ae2bb'. Please enable Email/Password provider under Authentication -> Sign-in method."
            } else {
                message
            }
            Result.failure(Exception(error))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    override fun getUserProfile(): Flow<User?> = callbackFlow {
        val uid = getCurrentUserId()
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                trySend(user)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getUserProfileOnce(): Result<User?> {
        val uid = getCurrentUserId() ?: return Result.success(null)
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserBudget(budget: Double): Result<Unit> {
        val uid = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("users").document(uid).update("budget", budget).await()
            // Also sync to cart document for session consistency
            firestore.collection("carts").document(uid).update("budget", budget).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
