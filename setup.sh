#!/bin/bash

# Set variables for KISS FFT
KISSFFT_URL="https://github.com/mborgerding/kissfft/archive/refs/tags/131.1.0.tar.gz"
KISSFFT_FOLDER="third_party/kissfft"
KISSFFT_ARCHIVE="131.1.0.tar.gz"

# Create destination folder for KISS FFT
mkdir -p "$KISSFFT_FOLDER"

# Download the KISS FFT file
curl -L "$KISSFFT_URL" -o "$KISSFFT_ARCHIVE"

# Extract into the desired folder
tar -xzf "$KISSFFT_ARCHIVE" --strip-components=1 -C "$KISSFFT_FOLDER"

# Remove the archive
rm "$KISSFFT_ARCHIVE"

echo "KISS FFT downloaded and extracted into $KISSFFT_FOLDER"

# Set variables for FlatBuffers
FLATBUFFERS_URL="https://github.com/google/flatbuffers/archive/e6463926479bd6b330cbcf673f7e917803fd5831.tar.gz"
FLATBUFFERS_FOLDER="third_party/flatbuffers"
FLATBUFFERS_ARCHIVE="flatbuffers.tar.gz"

# Create destination folder for FlatBuffers
mkdir -p "$FLATBUFFERS_FOLDER"

# Download the FlatBuffers file
curl -L "$FLATBUFFERS_URL" -o "$FLATBUFFERS_ARCHIVE"

# Extract into the desired folder
tar -xzf "$FLATBUFFERS_ARCHIVE" --strip-components=1 -C "$FLATBUFFERS_FOLDER"

# Remove the archive
rm "$FLATBUFFERS_ARCHIVE"

echo "FlatBuffers downloaded and extracted into $FLATBUFFERS_FOLDER"

