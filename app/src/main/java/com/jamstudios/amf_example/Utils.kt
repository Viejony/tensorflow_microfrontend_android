package com.jamstudios.amf_example

import android.content.Context
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Utils {

    fun printLog(log: String) {
        Log.d("MICROFRONTEND", log)
    }

    fun getAssetAsFile(context: Context, assetName: String): File {
        // Create a file in the app's internal storage
        val outputFile = File(context.filesDir, assetName.split("/").last())

        // If the file already exists, just return it
        if (outputFile.exists()) {
            return outputFile
        }

        // Make sure parent directories exist
        outputFile.parentFile?.mkdirs()

        // Copy the asset to the file
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return outputFile
    }

    fun decodeMonoWaveFileAsFloatArray(file: File): FloatArray {
        val shortArray = decodeMonoWaveFileAsShortArray(file)
        return FloatArray(shortArray.size) { index ->
            (shortArray[index] / 32767.0f).coerceIn(-1f..1f)
        }
    }

    fun decodeMonoWaveFileAsShortArray(file: File): ShortArray {
        val baos = ByteArrayOutputStream()
        file.inputStream().use { it.copyTo(baos) }
        val buffer = ByteBuffer.wrap(baos.toByteArray())
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(44)
        val shortBuffer = buffer.asShortBuffer()
        val shortArray = ShortArray(shortBuffer.limit())
        shortBuffer.get(shortArray)
        return shortArray
    }
}