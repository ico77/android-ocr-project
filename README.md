## Synopsis

android-ocr-project is a text detection and recognition application for Android devices. First, the devices built-in camera is used to capture images. The captured image is then
scanned for text regions. Lastly, the found text regions in the image are fed to an ocr engine which recognizes the text.

## Installation

The application is currently work in progress and not yet published. It is fully functional, so those interested in trying it out can build it from source.

**Installing from source:**

```
export ANDROID_HOME=/path/to/your/android-sdk
git clone https://github.com/ico77/android-ocr-project.git android-ocr-project  
cd android-ocr-project
./gradlew assemble
```

or import the project in Android Studio using File->New->Project From Version Control->GitHub
with this repository url: `https://github.com/ico77/android-ocr-project.git`

**Dependencies:**

The application uses the following open source libraries which will be resolved automatically by gradle:

- https://github.com/rmtheis/tess-two - a fork of Tesseract Tools for Android
- http://opencv.org/platforms/android.html - opencv for Android  

## License

```
Copyright 2016 Ivica Gunjaca

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```