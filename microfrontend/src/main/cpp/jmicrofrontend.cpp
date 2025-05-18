#include <jni.h>
#include <vector>
#include <cmath>
#include <cstring>
#include <android/log.h>
#include "tensorflow/lite/experimental/microfrontend/lib/frontend.h"
#include "tensorflow/lite/experimental/microfrontend/lib/frontend_util.h"

#define LOG_TAG "MicrofrontendJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// -------------------------------------------------------------------------------------------------
// Optional global state if you want one‑shot init + reuse.
static FrontendState g_frontend_state;
static bool g_state_ready = false;

extern "C" JNIEXPORT void JNICALL
Java_com_jamstudios_microfrontend_FeatureExtractor_init
(
    JNIEnv*,
    jobject /* this */,
    jint sample_rate
) {
    //la concha
    LOGI("Microfrontend init started");
    FrontendConfig config;
    FrontendFillConfigWithDefaults(&config);

    // Override specific settings
    config.window.size_ms = 30;
    config.window.step_size_ms = 20;
    config.noise_reduction.smoothing_bits = 10;
    config.noise_reduction.even_smoothing = 0.025f;
    config.noise_reduction.odd_smoothing = 0.06f;
    config.noise_reduction.min_signal_remaining = 0.05f;
    config.filterbank.num_channels = 40;
    config.filterbank.lower_band_limit = 125.0f;
    config.filterbank.upper_band_limit = 7500.0f;
    config.pcan_gain_control.enable_pcan = true;
    config.pcan_gain_control.strength = 0.95f;
    config.pcan_gain_control.offset = 80.0f;
    config.pcan_gain_control.gain_bits = 21;
    config.log_scale.enable_log = true;
    config.log_scale.scale_shift = 6;

    if (!FrontendPopulateState(&config, &g_frontend_state, sample_rate)) {
        LOGE("FrontendPopulateState failed");
        g_state_ready = false;
        return;
    }
    g_state_ready = true;
    LOGI("Microfrontend init complete (sr=%d)", sample_rate);
}

// ---------------------------------------------------------
// Main feature‑extraction entry point.
extern "C" JNIEXPORT jshortArray JNICALL
Java_com_jamstudios_microfrontend_FeatureExtractor_process
(
    JNIEnv* env,
    jobject /* this */,
    jshortArray input
) {

    if (!g_state_ready) {
        LOGE("Microfrontend state not initialised – call init() first");
        return nullptr;
    }

    // ---------- 1. Validate & copy input ----------
    if (input == nullptr) {
        LOGE("Null input array");
        return nullptr;
    }
    const jsize num_samples = env->GetArrayLength(input);
    if (num_samples <= 0) {
        LOGE("Empty input array");
        return nullptr;
    }
    LOGI("Processing %d PCM samples", num_samples);

    std::vector<int16_t> pcm(num_samples);
    env->GetShortArrayRegion(input, 0, num_samples, pcm.data());

    // ---------- 2. Run the Microfrontend ----------
    std::vector<jshort> features;  // store raw log‑magnitude bins (0‑255)
    size_t cursor = 0;
    int frame_cnt = 0;
    while (cursor < pcm.size()) {
        size_t samples_read = 0;
        FrontendOutput out = FrontendProcessSamples(&g_frontend_state,
                                                    pcm.data() + cursor,
                                                    pcm.size() - cursor,
                                                    &samples_read);
        if (samples_read == 0) {
            LOGE("FrontendProcessSamples returned 0 – aborting loop");
            break;
        }
        cursor += samples_read;

        if (out.size > 0 && out.values) {
            ++frame_cnt;
            features.insert(features.end(), out.values, out.values + out.size);
        }
    }

    LOGI("Generated %d frames, %zu feature values", frame_cnt, features.size());

    // ---------- 3. Convert to jshortArray ----------
    jshortArray j_out = env->NewShortArray(static_cast<jsize>(features.size()));
    if (!j_out) {
        LOGE("Failed to allocate jshortArray");
        return nullptr;
    }
    env->SetShortArrayRegion(j_out, 0, static_cast<jsize>(features.size()),
                             reinterpret_cast<const jshort*>(features.data()));
    return j_out;
}