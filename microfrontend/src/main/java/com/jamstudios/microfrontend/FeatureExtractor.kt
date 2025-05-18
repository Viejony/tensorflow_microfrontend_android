package com.jamstudios.microfrontend

class FeatureExtractor {

    external fun init(sampleRate: Int)

    external fun process(audioData: ShortArray): ShortArray

    companion object {
        init {
            System.loadLibrary("microfrontend")
        }
    }
}