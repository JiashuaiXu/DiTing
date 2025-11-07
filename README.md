# DiTing

DiTing: 24/7 noise-filtering audio recorder for Android, built with Kotlin.

## Features

- **Continuous Recording**: 24/7 audio recording capability with background service
- **Noise Filtering**: Threshold-based noise gating to filter out low-amplitude background noise
- **Foreground Service**: Runs as a foreground service with notification for uninterrupted recording
- **Wake Lock**: Keeps CPU active for continuous operation
- **Modern Architecture**: Built with Kotlin using Android best practices
- **Traditional UI**: Uses XML layouts with Material Design components (no Jetpack Compose)

## Technical Details

### Architecture Components

1. **MainActivity.kt**
   - Main UI with recording controls
   - Permission handling for runtime permissions (RECORD_AUDIO, POST_NOTIFICATIONS)
   - Service binding for real-time status updates
   - Start/Stop recording controls

2. **RecordingService.kt**
   - Foreground service for background recording
   - Manages wake lock to keep CPU active
   - Creates and manages notification channel
   - Returns START_STICKY for automatic restart on termination

3. **AudioRecorder.kt**
   - Uses AudioRecord API for low-level audio capture
   - Implements threshold-based noise gating (configurable threshold: 500)
   - Records audio in 44.1kHz sample rate, mono, PCM 16-bit format
   - Saves recordings as PCM files with timestamp

### Permissions

The app requires the following permissions:
- `RECORD_AUDIO` - Required for audio recording
- `WAKE_LOCK` - Keeps CPU running during recording
- `FOREGROUND_SERVICE` - Enables foreground service
- `FOREGROUND_SERVICE_MICROPHONE` - Specifies microphone usage (Android 14+)
- `POST_NOTIFICATIONS` - Shows foreground service notification (Android 13+)

### Audio Configuration

- **Sample Rate**: 44,100 Hz
- **Channel**: Mono (single channel)
- **Encoding**: PCM 16-bit
- **Noise Threshold**: 500 (amplitude threshold for noise gating)
- **Output Format**: Raw PCM files (.pcm)
- **Storage Location**: App's external files directory (`recordings/`)

### Noise Filtering

The app implements basic threshold-based noise gating:
- Samples with amplitude below threshold (500) are replaced with silence (0)
- Samples above threshold pass through unmodified
- Threshold can be adjusted in `AudioRecorder.kt`

## Project Structure

```
DiTing/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/jiashuaixu/diting/
│       │   ├── MainActivity.kt
│       │   ├── RecordingService.kt
│       │   └── AudioRecorder.kt
│       └── res/
│           ├── layout/
│           │   └── activity_main.xml
│           ├── values/
│           │   ├── strings.xml
│           │   ├── colors.xml
│           │   └── themes.xml
│           └── drawable/
│               └── ic_launcher_foreground.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Building the Project

### Requirements

- Android Studio Arctic Fox or later
- Android SDK API Level 34
- Gradle 8.2+
- Kotlin 1.8.20+

### Build Instructions

1. Clone the repository:
```bash
git clone https://github.com/JiashuaiXu/DiTing.git
cd DiTing
```

2. Open in Android Studio or build via command line:
```bash
./gradlew assembleDebug
```

3. Install on device:
```bash
./gradlew installDebug
```

## Usage

1. Launch the app on your Android device (API 26+)
2. Grant microphone permission when prompted
3. Tap "Start Recording" to begin continuous recording
4. The app will show a foreground notification while recording
5. Tap "Stop Recording" to end the session
6. Recorded files are saved in `/Android/data/com.jiashuaixu.diting/files/recordings/`

## Configuration

### Adjusting Noise Threshold

Edit `AudioRecorder.kt` line 25:
```kotlin
private val noiseThreshold = 500 // Adjust this value (0-32767)
```

- Lower values: More sensitive, captures quieter sounds
- Higher values: Less sensitive, filters more noise

### Audio Settings

Edit audio configuration in `AudioRecorder.kt` lines 19-22:
```kotlin
private val sampleRate = 44100        // Sample rate in Hz
private val channelConfig = AudioFormat.CHANNEL_IN_MONO  // Mono/Stereo
private val audioFormat = AudioFormat.ENCODING_PCM_16BIT  // Bit depth
```

## Converting PCM Files

Recorded PCM files can be converted to common audio formats using FFmpeg:

```bash
# Convert to WAV
ffmpeg -f s16le -ar 44100 -ac 1 -i recording_timestamp.pcm output.wav

# Convert to MP3
ffmpeg -f s16le -ar 44100 -ac 1 -i recording_timestamp.pcm output.mp3
```

## License

This project is open source. Please check the repository for license information.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## Notes

- The app requires Android 8.0 (API 26) or higher
- Recording continues even when the screen is off (wake lock)
- Battery usage may be higher during continuous recording
- Ensure sufficient storage space for long recordings
- PCM files are uncompressed and will be large in size
