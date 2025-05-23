package io.viejony.microfrontend

class FeatureExtractor {

    external fun init(sampleRate: Int, enableLog: Boolean = false)

    external fun process(audioData: ShortArray): ShortArray

    companion object {
        init {
            System.loadLibrary("microfrontend")
        }
    }
}