package com.jamstudios.microfrontend

class FeatureExtractor {

    // Season 1
    external fun computeFeatures(audioData: FloatArray): FloatArray
    external fun computeFeaturesFromShortArray(audioData: ShortArray): FloatArray

    // Season 2: with nudes!
    external fun init(sampleRate: Int)
    external fun process(audioData: ShortArray): ShortArray

    companion object {
        init {
            System.loadLibrary("microfrontend")
        }
    }
}