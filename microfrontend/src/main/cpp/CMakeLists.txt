cmake_minimum_required(VERSION 3.22.1)
project("microfrontend")

# Paths
set(TENSORFLOW_DIR ${CMAKE_SOURCE_DIR}/../../../../third_party/tensorflow)
set(KISSFFT_DIR ${CMAKE_SOURCE_DIR}/../../../../third_party/kissfft)
set(FLATBUFFERS_DIR ${CMAKE_SOURCE_DIR}/../../../../third_party/flatbuffers)
set(KISSFFT_BINARY_DIR ${CMAKE_BINARY_DIR}/kissfft_build)



# Flatbuffers
include_directories(${FLATBUFFERS_DIR}/include)

set(FlatBuffers_Library_SRCS
        ${FLATBUFFERS_DIR}/include/flatbuffers/allocator.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/array.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/base.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/buffer.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/buffer_ref.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/default_allocator.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/detached_buffer.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/flatbuffer_builder.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/flatbuffers.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/flexbuffers.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/flex_flat_util.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/hash.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/idl.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/minireflect.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/reflection.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/reflection_generated.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/registry.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/stl_emulation.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/string.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/struct.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/table.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/util.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/vector.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/vector_downward.h
        ${FLATBUFFERS_DIR}/include/flatbuffers/verifier.h
        ${FLATBUFFERS_DIR}/src/idl_parser.cpp
        ${FLATBUFFERS_DIR}/src/idl_gen_text.cpp
        ${FLATBUFFERS_DIR}/src/reflection.cpp
        ${FLATBUFFERS_DIR}/src/util.cpp
        ${FLATBUFFERS_DIR}/src/idl_gen_fbs.cpp
        ${FLATBUFFERS_DIR}/src/code_generators.cpp
)
add_library(flatbuffers STATIC ${FlatBuffers_Library_SRCS})



# Kissfft

## Standard options used by upstream KISSFFT
set(KISSFFT_OPENMP OFF CACHE BOOL "Disable OpenMP support" FORCE)
set(KISSFFT_STATIC ON CACHE BOOL "Build as a static library" FORCE)
set(KISSFFT_TEST OFF CACHE BOOL "Disable building tests" FORCE)
set(KISSFFT_TOOLS OFF CACHE BOOL "Disable building tools" FORCE)
set(KISSFFT_USE_ALLOCA OFF CACHE BOOL "Avoid alloca() for portability" FORCE)

## Match TensorFlow's Microfrontend build config
add_definitions(-DKISSFFT_DATATYPE=int16_t)

## Include kissfft as a subproject
add_subdirectory(${KISSFFT_DIR} ${KISSFFT_BINARY_DIR})
include_directories(${KISSFFT_DIR})



# Microfrontend

# Include headers
include_directories(
        ${TENSORFLOW_DIR}
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/bits.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/fft_util.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/filterbank.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/filterbank_util.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/frontend.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/frontend_util.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/log_scale_util.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/log_lut.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/noise_reduction.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/noise_reduction_util.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/pcan_gain_control.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/pcan_gain_control_util.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/window.h
        ${TENSORFLOW_DIR}/tensorflow/lite/experimental/microfrontend/lib/window_util.h
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

# Link math library if needed
target_link_libraries(audio_microfrontend m)
target_link_libraries(${CMAKE_PROJECT_NAME} audio_microfrontend kissfft flatbuffers)
target_link_libraries(${CMAKE_PROJECT_NAME} android log)




