package com.example.qskip.domain.model

data class Flyer(
    val flyerId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val active: Boolean = true,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
