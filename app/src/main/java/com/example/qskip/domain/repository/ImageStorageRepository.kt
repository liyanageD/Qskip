package com.example.qskip.domain.repository

interface ImageStorageRepository {
    suspend fun uploadImage(byteArray: ByteArray, fileName: String): Result<String>
    suspend fun deleteImage(fileName: String): Result<Unit>
}
