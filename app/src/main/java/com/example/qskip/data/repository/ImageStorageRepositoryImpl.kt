package com.example.qskip.data.repository

import com.example.qskip.domain.repository.ImageStorageRepository
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorageRepositoryImpl @Inject constructor(
    private val supabaseStorage: Storage
) : ImageStorageRepository {

    private val bucketName = "product-images"

    override suspend fun uploadImage(byteArray: ByteArray, fileName: String): Result<String> {
        return try {
            val bucket = supabaseStorage.from(bucketName)
            bucket.upload(fileName, byteArray, upsert = true)
            val publicUrl = bucket.publicUrl(fileName)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteImage(fileName: String): Result<Unit> {
        return try {
            val bucket = supabaseStorage.from(bucketName)
            bucket.delete(fileName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
