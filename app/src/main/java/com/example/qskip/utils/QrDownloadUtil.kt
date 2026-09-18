package com.example.qskip.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object QrDownloadUtil {

    fun saveQrCodeToDownloads(
        context: Context,
        qrBitmap: Bitmap,
        productName: String,
        productCode: String
    ): Boolean {
        // Create printable compound bitmap with Product Name & Code
        val printableBitmap = createPrintableQrBitmap(qrBitmap, productName, productCode)

        val filename = "${productCode.replace(" ", "_")}-QR.png"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return false

                resolver.openOutputStream(uri)?.use { outputStream ->
                    printableBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, filename)
                FileOutputStream(file).use { outputStream ->
                    printableBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            }
            Toast.makeText(context, "QR code saved to Downloads folder", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save QR code: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun createPrintableQrBitmap(
        qrBitmap: Bitmap,
        productName: String,
        productCode: String
    ): Bitmap {
        val width = qrBitmap.width
        val extraHeight = 120
        val height = qrBitmap.height + extraHeight

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawColor(Color.WHITE)

        // Draw QR
        canvas.drawBitmap(qrBitmap, 0f, 0f, null)

        // Draw Text
        val paintName = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val paintCode = Paint().apply {
            color = Color.DKGRAY
            textSize = 22f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val centerX = width / 2f
        val startY = qrBitmap.height + 45f

        canvas.drawText(productName, centerX, startY, paintName)
        canvas.drawText("Code: $productCode", centerX, startY + 40f, paintCode)

        return resultBitmap
    }
}
