package com.jamstudios.amf_example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.jamstudios.amf_example.ui.screens.MainScreen
import com.jamstudios.amf_example.ui.theme.AudioMicrofrontendExampleTheme
import com.jamstudios.microfrontend.FeatureExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val specWidth = 40
    private val specHeight = 49
    private var spectrogramData = MutableStateFlow(FloatArray(specWidth*specHeight){ 0f } )
    private var isRunning = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AudioMicrofrontendExampleTheme {
                MainScreen(
                    data = spectrogramData.collectAsState().value,
                    width = specWidth,
                    height = specHeight,
                    isRunning = isRunning.collectAsState().value,
                    onAppear = { runMultipleTests() },
                    onTryAgainClicked = { runMultipleTests() }
                )
            }
        }
    }

    private fun runMultipleTests() {
        lifecycleScope.launch {
            isRunning.value = true
            val initTime = System.currentTimeMillis()
            for (i in 0 until 100) {
                testJMicrofrontendJNI("Test ${i + 1}")
                //delay(200)
            }
            val endTime = System.currentTimeMillis()
            val duration = endTime - initTime
            println("MainActivity: runMultipleTests: duration = $duration")
            isRunning.value = false
        }
    }

    private fun testJMicrofrontendJNI(preamb: String, print: Boolean = false) {

        // Test JNI
        println("MainActivity: JNI: $preamb")

        // With microfrontend native lib
        try {
            // Load audio from assets
            val file = Utils.getAssetAsFile(this, "common_voice_en_114475.wav")

            // Convert WAV file to FloatArray
            val audioArray = Utils.decodeMonoWaveFileAsShortArray(file)

            // Create a NEW instance of FeatureExtractor for each test
            // (This is important for proper testing)
            val featureExtractor = FeatureExtractor()
            featureExtractor.init(16000)

            // Make a copy of the audio data for each test to ensure isolation
            val audioArrayCopy = audioArray.copyOf()

            // Get features array from the copy
            val featuresArray: ShortArray = featureExtractor.process(audioArrayCopy)
            println("MainActivity: JNI: featuresFromFloatArray.size = ${featuresArray.size}")

            // Optional: pass data tu state
            spectrogramData.value = FloatArray(featuresArray.size){ i -> featuresArray[i].toFloat() }

            // Print
            if (print) {
                Utils.printShortArray(
                    featuresArray, //.sliceArray(0..19),
                    lineJump = 49
                )
            }

            // Check if the result is all zeros or very small values
            val hasSignificantValues = featuresArray.any { it > 1.0f }
            if (!hasSignificantValues) {
                println("WARNING: Features may be too small - check audio input or scaling!")
            }

        } catch (e: Exception) {
            println("ERROR in test: ${e.message}")
            e.printStackTrace()
        }
    }
}