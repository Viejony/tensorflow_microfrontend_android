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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.experimental.microfrontend.Microfrontend

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
            for (i in 0 until 10) {
                //testAAR("Test ${i + 1}")
                testJMicrofrontendJNI("Test ${i + 1}")
                delay(200)
            }
            isRunning.value = false
        }
    }

    private fun testJMicrofrontendJNI(preamb: String) {

        // Test JNI
        println("MainActivity: JNI: $preamb")

        // With microfrontend native lib
        try {
            // Load audio from assets
            val file = Utils.getAssetAsFile(this, "common_voice_en_114475.wav")

            // Convert WAV file to FloatArray
            //val audioArray = Utils.decodeMonoWaveFileAsFloatArray(file)
            val audioArray = Utils.decodeMonoWaveFileAsShortArray(file)

            // Create a NEW instance of FeatureExtractor for each test
            // (This is important for proper testing)
            val featureExtractor = FeatureExtractor()
            featureExtractor.init(16000)

            // Make a copy of the audio data for each test to ensure isolation
            val audioArrayCopy = audioArray.copyOf()

            // Get features array from the copy
            //val featuresArray: FloatArray = featureExtractor.computeFeaturesFromShortArray(audioArrayCopy)
            val featuresArray: ShortArray = featureExtractor.process(audioArrayCopy)
            println("MainActivity: JNI: featuresFromFloatArray.size = ${featuresArray.size}")

            // Optional: pass data tu state
            //spectrogramData.value = featuresArray.copyOf()
            spectrogramData.value = FloatArray(featuresArray.size){ i -> featuresArray[i].toFloat() }

            // Only print the first 20 values for clarity
            /*
            printFloatArray(
                featuresArray, //.sliceArray(0..19),
                lineJump = 49
            )
             */

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

    private fun testAAR(preamb: String) {

        // Test JNI
        println("MainActivity: JNI: $preamb")

        // With Tensorflow AAR lib
        try {
            // Load audio from assets
            val file = Utils.getAssetAsFile(this, "common_voice_en_114475.wav")

            // Convert WAV file to ShortArray
            val audioArray = Utils.decodeMonoWaveFileAsShortArray(file)

            // Create a NEW instance of FeatureExtractor for each test
            val featureExtractor = Microfrontend()
            featureExtractor.init(16000)

            // Make a copy of the audio data for each test to ensure isolation
            val audioArrayCopy = audioArray.copyOf()

            // Get features array from the copy
            val featuresArray: ShortArray = featureExtractor.process(audioArrayCopy)
            println("MainActivity: JNI: featuresFromFloatArray.size = ${featuresArray.size}")

            // Optional: pass data tu state
            spectrogramData.value = FloatArray(featuresArray.size) { i -> featuresArray[i].toFloat() }

            // Only print the first 20 values for clarity
            printShortArray(
                featuresArray,
                lineJump = 49
            )
        } catch (e: Exception) {
            println("ERROR in test: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun printFloatArray(x: FloatArray, lineJump: Int? = null) {
        print("[")
        for(i in x.indices) {
            print("${x[i]}${ if(i != (x.size - 1)) "," else "" }${ if( lineJump != null && (i+1) % lineJump == 0) "\n" else "" }")
        }
        print("]\n")
    }

    private fun printShortArray(x: ShortArray, lineJump: Int? = null) {
        print("[")
        for(i in x.indices) {
            print("${x[i]}${ if(i != (x.size - 1)) "," else "" }${ if( lineJump != null && (i+1) % lineJump == 0) "\n" else "" }")
        }
        print("]\n")
    }
}