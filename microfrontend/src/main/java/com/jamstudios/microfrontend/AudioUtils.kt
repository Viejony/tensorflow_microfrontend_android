package com.jamstudios.microfrontend

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioUtils {

    /**
     * Obtains a file from assets folder, using its name.
     * @param context: Context
     * @param assetName: File name in assets folder
     * @return a File instance
     */
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

    /**
     * Converts a WAV audio file into a FloatArray
     * @param file: File instance, must be a WAV file
     * @return FloatArray with every sample coerced between -1.0 and 1.0.
     */
    fun decodeMonoWaveFileAsFloatArray(file: File): FloatArray {
        val shortArray = decodeMonoWaveFileAsShortArray(file)
        return FloatArray(shortArray.size) { index ->
            (shortArray[index] / 32767.0f).coerceIn(-1f..1f)
        }
    }

    /**
     * Converts a WAV audio file into a ShortArray
     * @param file: File instance, must be a WAV file
     * @return ShortArray with every sample converted into a Short.
     */
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

    /**
     * Converts a PCM audio byte array into a ShortArray. Audio must mono, 16000 Hz, 16 bits per sample.
     * @param pcmBytes: ByteArray with PCM audio data
     * @param littleEndian: Flag that indicates if the audio is little-endian or big-endian
     * @return ShortArray with every sample converted into a Short.
     */
    fun pcm16ByteArrayToShortArray(
        pcmBytes: ByteArray,
        littleEndian: Boolean = true
    ): ShortArray {

        // Check if the byte array length is even
        require(pcmBytes.size % 2 == 0) { "PCM byte array length must be even" }

        val shortArray = ShortArray(pcmBytes.size / 2)

        for (i in shortArray.indices) {
            val byte1 = pcmBytes[i * 2]
            val byte2 = pcmBytes[i * 2 + 1]

            shortArray[i] = if (littleEndian) {
                // Little-endian: byte1 is the least significant byte, byte2 is the most significant byte
                ((byte2.toInt() shl 8) or (byte1.toInt() and 0xFF)).toShort()
            } else {
                // Big-endian: byte1 is the most significant byte, byte2 is the least significant byte
                ((byte1.toInt() shl 8) or (byte2.toInt() and 0xFF)).toShort()
            }
        }

        return shortArray
    }
}