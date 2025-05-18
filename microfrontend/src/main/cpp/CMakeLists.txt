cmake_minimum_required(VERSION 3.22.1)
project("microfrontend")

# Paths
set(TENSORFLOW_DIR ${CMAKE_SOURCE_DIR}/../../../../third_party/tensorflow)
set(KISSFFT_DIR ${CMAKE_SOURCE_DIR}/../../../../third_party/kissfft)
set(KISSFFT_BINARY_DIR ${CMAKE_BINARY_DIR}/kissfft_build)
set(FLATBUFFERS_DIR ${CMAKE_SOURCE_DIR}/../../../../third_party/flatbuffers)
set(FLATBUFFERS_BINARY_DIR ${CMAKE_BINARY_DIR}/flatbuffers_build)



# Flatbuffers
set(FLATBUFFERS_BUILD_TESTS OFF CACHE BOOL "Disable FlatBuffers tests" FORCE)
set(FLATBUFFERS_BUILD_FLATC OFF CACHE BOOL "Disable FlatBuffers compiler (flatc)" FORCE)
set(FLATBUFFERS_BUILD_SHAREDLIB OFF CACHE BOOL "Build FlatBuffers as a static library only" FORCE)

# Add the FlatBuffers CMake project (this defines the 'flatbuffers' target)
add_subdirectory(${FLATBUFFERS_DIR} ${FLATBUFFERS_BINARY_DIR} EXCLUDE_FROM_ALL)


# Kissfft

## Standard options used by upstream KISSFFT
set(KISSFFT_DATATYPE int16_t CACHE STRING "Fixed-point data type" FORCE)
set(KISSFFT_OPENMP OFF CACHE BOOL "Disable OpenMP support" FORCE)
set(KISSFFT_STATIC ON CACHE BOOL "Build as a static library" FORCE)
set(KISSFFT_TEST OFF CACHE BOOL "Disable building tests" FORCE)
set(KISSFFT_TOOLS OFF CACHE BOOL "Disable building tools" FORCE)
set(KISSFFT_USE_ALLOCA OFF CACHE BOOL "Avoid alloca() for portability" FORCE)
#add_definitions(-DKISSFFT_DATATYPE=int16_t)
add_subdirectory(${KISSFFT_DIR} ${KISSFFT_BINARY_DIR})
include_directories(${KISSFFT_DIR})



# Microfrontend

# Include headers
include_directories(
        ${TENSORFLOW_DIR}
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib
        ${TENSORFLOW_DIR}/tensorflow/lite/kernels
        ${TENSORFLOW_DIR}/tensorflow/lite/kernels/internal
        ${TENSORFLOW_DIR}/tensorflow/lite/tools/make/downloads/flatbuffers/include
)

# Define sources
set(FRONTEND_SRCS
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/audio_microfrontend.cc
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/fft.cc
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/fft_util.cc
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/filterbank.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/filterbank_util.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/frontend.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/frontend_util.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/log_lut.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/log_scale_util.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/log_scale.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/noise_reduction.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/noise_reduction_util.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/pcan_gain_control.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/pcan_gain_control_util.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/window.c
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/window_util.c
)
message(WARNING "FRONTEND_SRCS = ${FRONTEND_SRCS}")



# Create static library
add_library(audio_microfrontend STATIC ${FRONTEND_SRCS})

# Shared library (this is what Android uses)
add_library(${CMAKE_PROJECT_NAME} SHARED jmicrofrontend.cpp)

# Link libraries to the microfrontend targets:
# - Link FlatBuffers and KissFFT to the shared library (as these provide functionality needed at runtime)
# - Also ensure the static lib gets any includes/flags it needs from FlatBuffers
target_link_libraries(audio_microfrontend
        PRIVATE flatbuffers             # FlatBuffers static lib (for header usage if needed by audio_microfrontend.cc)
        PUBLIC kissfft                  # FFT library (already added via add_subdirectory)
        PUBLIC m                        # math library for FFT (if required on this platform)
)
target_link_libraries(${CMAKE_PROJECT_NAME}
        PUBLIC audio_microfrontend      # Microfrontend DSP code
        PUBLIC kissfft                  # FFT library
        PUBLIC flatbuffers              # FlatBuffers library for model parsing support
        PUBLIC android log              # Android NDK libraries (if building for Android)
)



