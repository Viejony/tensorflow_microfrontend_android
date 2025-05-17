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

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_jamstudios_microfrontend_FeatureExtractor_computeFeatures
(
    JNIEnv *env, jobject, jfloatArray j_input
) {
    LOGI("Starting computeFeatures");

    // === 1. Convert input from Java ===
    jsize num_samples = env->GetArrayLength(j_input);
    LOGI("Input array length: %d", num_samples);

    if (num_samples <= 0) {
        LOGE("Empty input array");
        return nullptr;
    }

    std::vector<int16_t> pcm_input(num_samples);

    // Get float data and convert to int16
    {
        std::vector<jfloat> input_floats(num_samples);
        env->GetFloatArrayRegion(j_input, 0, num_samples, input_floats.data());

        for (int i = 0; i < num_samples; ++i) {
            float sample = input_floats[i];
            sample = std::max(-1.0f, std::min(1.0f, sample)); // Clamp to [-1, 1]
            pcm_input[i] = static_cast<int16_t>(std::round(sample * 32767.0f));
        }
    }

    // === 2. Configure Microfrontend ===
    const int sample_rate = 16000;

    // Create a fresh config with defaults
    struct FrontendConfig config;
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

    LOGI("Frontend configuration complete");

    // Create and initialize state
    struct FrontendState state;
    memset(&state, 0, sizeof(state));

    if (!FrontendPopulateState(&config, &state, sample_rate)) {
        LOGE("Failed to populate frontend state");
        return nullptr;
    }

    LOGI("State populated successfully");

    // === 3. Run Frontend ===
    std::vector<float> output_floats;
    size_t cursor = 0;
    int frame_count = 0;

    while (cursor < pcm_input.size()) {
        size_t num_samples_read = 0;

        FrontendOutput output = FrontendProcessSamples(
                &state,
                pcm_input.data() + cursor,
                pcm_input.size() - cursor,
                &num_samples_read
        );

        if (num_samples_read == 0) {
            LOGE("No samples processed, breaking loop");
            break;  // Avoid infinite loop
        }

        cursor += num_samples_read;

        if (output.values != nullptr && output.size > 0) {
            frame_count++;
            for (int i = 0; i < output.size; ++i) {
                float value = static_cast<float>(output.values[i]) * (10.0f / 256.0f);
                output_floats.push_back(value);
            }
        }
    }

    LOGI("Processing complete. Generated %d frames, %zu total features",
         frame_count, output_floats.size());

    // Print first few values for debugging
    if (!output_floats.empty()) {
        std::string debug_values = "First 5 values: ";
        for (int i = 0; i < std::min(5, static_cast<int>(output_floats.size())); i++) {
            debug_values += std::to_string(output_floats[i]) + ", ";
        }
        LOGI("%s", debug_values.c_str());
    }

    // === 4. Return as jfloatArray ===
    jfloatArray j_output = env->NewFloatArray(static_cast<jsize>(output_floats.size()));
    if (j_output == nullptr) {
        LOGE("Failed to allocate output array");
        FrontendFreeStateContents(&state);
        return nullptr;
    }

    env->SetFloatArrayRegion(
            j_output,
            0,
            static_cast<jsize>(output_floats.size()),
         output_floats.data()
         );

    // Clean up resources
    FrontendFreeStateContents(&state);
    LOGI("State freed, returning %zu features", output_floats.size());

    return j_output;
}


extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_jamstudios_microfrontend_FeatureExtractor_computeFeaturesFromShortArray
(
    JNIEnv *env, jobject, jshortArray j_input
) {
    LOGI("Starting computeFeatures");

    // === 1. Convert input from Java ===
    jsize num_samples = env->GetArrayLength(j_input);
    LOGI("Input array length: %d", num_samples);

    if (num_samples <= 0) {
        LOGE("Empty input array");
        return nullptr;
    }

    // Get int16 data
    std::vector<int16_t> pcm_input(num_samples);
    env->GetShortArrayRegion(j_input, 0, num_samples, pcm_input.data());

    // === 2. Configure Microfrontend ===
    const int sample_rate = 16000;

    // Create a fresh config with defaults
    struct FrontendConfig config;
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

    LOGI("Frontend configuration complete");

    // Create and initialize state
    struct FrontendState state;
    memset(&state, 0, sizeof(state));

    if (!FrontendPopulateState(&config, &state, sample_rate)) {
        LOGE("Failed to populate frontend state");
        return nullptr;
    }

    LOGI("State populated successfully");

    // === 3. Run Frontend ===
    std::vector<float> output_floats;
    size_t cursor = 0;
    int frame_count = 0;

    while (cursor < pcm_input.size()) {
        size_t num_samples_read = 0;

        FrontendOutput output = FrontendProcessSamples(
                &state,
                pcm_input.data() + cursor,
                pcm_input.size() - cursor,
                &num_samples_read
        );

        if (num_samples_read == 0) {
            LOGE("No samples processed, breaking loop");
            break;  // Avoid infinite loop
        }

        cursor += num_samples_read;

        if (output.values != nullptr && output.size > 0) {
            frame_count++;
            for (int i = 0; i < output.size; ++i) {
                float value = static_cast<float>(output.values[i]) * (10.0f / 256.0f);
                output_floats.push_back(value);
            }
        }
    }

    LOGI("Processing complete. Generated %d frames, %zu total features",
         frame_count, output_floats.size());

    // Print first few values for debugging
    if (!output_floats.empty()) {
        std::string debug_values = "First 5 values: ";
        for (int i = 0; i < std::min(5, static_cast<int>(output_floats.size())); i++) {
            debug_values += std::to_string(output_floats[i]) + ", ";
        }
        LOGI("%s", debug_values.c_str());
    }

    // === 4. Return as jfloatArray ===
    jfloatArray j_output = env->NewFloatArray(static_cast<jsize>(output_floats.size()));
    if (j_output == nullptr) {
        LOGE("Failed to allocate output array");
        FrontendFreeStateContents(&state);
        return nullptr;
    }

    env->SetFloatArrayRegion(
            j_output,
            0,
            static_cast<jsize>(output_floats.size()),
            output_floats.data()
    );

    // Clean up resources
    FrontendFreeStateContents(&state);
    LOGI("State freed, returning %zu features", output_floats.size());

    return j_output;
}






////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

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