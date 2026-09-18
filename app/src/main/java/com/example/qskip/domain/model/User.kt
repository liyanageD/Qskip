package com.example.qskip.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "CUSTOMER", // CUSTOMER, ADMIN, STAFF
    val rewardPoints: Int = 0,
    val budget: Double = 0.0,
    val notificationEnabled: Boolean = true,
    val accountStatus: String = "ACTIVE",
    val createdAt: Long = 0L
) {
    val isAdmin: Boolean
        get() = role == "ADMIN" || role == "STAFF"
}
