# OpenCV Retinal Scan Matching

Java/OpenCV command-line prototype for comparing two retinal images and returning a binary match decision.

## Project status (as of 2026-10-04)

- University assignment prototype
- Single Java source file (`RetinalMatch.java`)
- No build system (`pom.xml`, `gradle`, etc.) or automated tests in this repository

## Current features

- Reads two image paths from CLI arguments
- Preprocesses both images (grayscale, masking, contrast, blur, Laplacian edges, thresholding, inversion, denoising)
- Performs template matching (`TM_CCOEFF_NORMED`) in both directions
- Returns:
  - `1` for match
  - `0` for non-match

## Technology stack

- Java (CLI application)
- OpenCV Java API (`org.opencv.*`)

## Repository structure

```text
.
├── RetinalMatch.java              # Main program
├── README.md                      # This documentation
└── Compx301 - Retina Matching.pdf # Assignment/project report
```

## Prerequisites

You need:

- A Java compiler/runtime (JDK)
- OpenCV Java bindings (JAR)
- OpenCV native library available to `System.loadLibrary(Core.NATIVE_LIBRARY_NAME)`

> The repository does not pin a Java or OpenCV version. The previous usage example referenced `opencv-420.jar`.

## Build and run

The project is compiled directly with `javac`:

```bash
export CLASSPATH="/usr/share/java/opencv-420.jar:."
javac -d . RetinalMatch.java
java RetinalMatch <path-to-image-1>.jpg <path-to-image-2>.jpg
```

Example:

```bash
export CLASSPATH="/usr/share/java/opencv-420.jar:."
javac -d . RetinalMatch.java
java RetinalMatch RIDB/IM000001_2.jpg RIDB/IM000002_2.jpg
```

## How matching works (implemented pipeline)

1. Convert both images to grayscale
2. Build masks using binary thresholding (to suppress border/background effects)
3. Increase contrast and apply Gaussian blur
4. Detect edges using Laplacian, then blur again
5. Threshold and apply the masks
6. Invert image colors and denoise with median blur
7. Apply morphological opening (erode then dilate)
8. Crop template image to largest contour region from mask
9. Run normalized cross-correlation template match
10. Accept if `maxVal > 0.123`
11. Repeat with images swapped; match succeeds if either direction succeeds

## Testing

There is no automated test suite in this repository. Validation is manual:

1. Compile the program successfully
2. Run known matching and non-matching image pairs
3. Confirm output is `1` for matches and `0` for non-matches

## Configuration details

Current tunables are hard-coded in `RetinalMatch.java`, including:

- Match threshold: `0.123`
- Blur kernel sizes
- Threshold constants
- Morphological kernel settings

Changing behavior currently requires editing source code.

## Limitations

- Assumes exactly two valid image file arguments
- Exits on image load failure
- No package/dependency manager or reproducible build config
- No automated tests/benchmarks in-repo
- Reported accuracy/performance claims are from assignment experimentation, not continuously verified CI

## Attribution and licensing

- Authors listed in source/README: Alexander Stokes, Rowan Thorley
- This repository currently does not include an explicit `LICENSE` file
