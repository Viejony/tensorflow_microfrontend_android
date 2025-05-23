package io.viejony.amf_example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

@Composable
fun SpectrogramView(
    modifier: Modifier,
    data: FloatArray,
    width: Int,
    height: Int
) {
    val bitmap = remember(data, width, height) {
        makeViridisSpectrogram(data, width, height)
    }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.None
    )
}

fun makeViridisSpectrogram(
    data: FloatArray,
    width: Int,
    height: Int,
    lut: IntArray = VIRIDIS_LUT
): Bitmap {
    require(data.size == width * height) { "Data size must be width × height" }

    // Find min and max for normalization
    var min = Float.POSITIVE_INFINITY
    var max = Float.NEGATIVE_INFINITY
    for (v in data) {
        if (v < min) min = v
        if (v > max) max = v
    }
    val range = max - min

    // Create the Bitmap
    val bitmap = createBitmap(width, height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val value = data[y * width + x]
            val normalized = if (range > 0f) ((value - min) / range).coerceIn(0f, 1f) else 0f
            val index = (normalized * (lut.size - 1)).toInt().coerceIn(0, lut.size - 1)
            bitmap[x, y] = lut[index]
        }
    }

    return bitmap
}