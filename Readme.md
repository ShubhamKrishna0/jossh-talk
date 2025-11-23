# 📱 Jossh Talk — Android Intern Task

Kotlin Multiplatform (KMM) + Compose Multiplatform prototype that allows users to perform three sample tasks — Text Reading, Image Description, and Photo Capture — and store everything locally in a Task History list.

---

## 🔗 Repository

https://github.com/ShubhamKrishna0/jossh-talk.git

---
## 🔗 Apk Link

https://github.com/ShubhamKrishna0/jossh-talk/releases/download/v1.0.0/app-debug.apk

---

## ▶️ Build Command

```sh
./gradlew :app:compileDebugKotlin
```

---

# 🧰 Tech Stack

- ⚡ Kotlin
- 🎨 Compose Multiplatform UI
- 🌐 Kotlinx Serialization
- 🎤 Custom Audio Recorder
- 📷 Camera Capture
- 📦 Local Storage Repository Pattern
- 🧭 Compose Navigation

---

# 📁 Project Folder Structure (Android)

Based on your actual project:

```
main/
└── java/com/example/josh
    ├── data
    │   ├── ApiClient.kt
    │   ├── Models.kt
    │   ├── ProductsApi.kt
    │   ├── ProductsRepository.kt
    │   └── TaskRepository.kt
    │
    ├── recorder
    │   └── AudioRecorder.kt
    │
    └── ui
        ├── Screens.kt
        └── MainActivity.kt

res/
AndroidManifest.xml
build.gradle.kts
settings.gradle.kts
```

---

# 🚀 App Flow

## 🟦 Step 1 — Start Screen

- Heading: “Let’s start with a Sample Task for practice.”
- Sub-text: “Pehele hum ek sample task karte hain.”
- Button: **Start Sample Task**
- → Navigates to **Noise Test Screen**

---

## 🔊 Step 2 — Noise Test Screen

- Decibel meter (0–60 dB)
- Button: **Start Test**
- Logic:
  - `< 40 dB` → Good to proceed
  - `≥ 40 dB` → Move to a quieter place
- → Navigates to Task Selection screen

---

## 📝 Step 3 — Task Selection Screen

User chooses between:

- 📖 Text Reading
- 🖼 Image Description
- 📷 Photo Capture

---

## 📖 Step 4 — Text Reading Task

API: `https://dummyjson.com/products`

Includes:

- API text displayed
- Mic: **Press & Hold**
- Recording duration validation (10–20 seconds)
- Error messages for short/long recordings
- Playback preview
- Checkboxes:
  - No background noise
  - No mistakes
  - Beech me koi galti nahi hai
- Buttons: Record Again, Submit

### Saved JSON Sample

```json
{
  "task_type": "text_reading",
  "text": "Mega long lasting fragrance...",
  "audio_path": "/local/path/audio.mp3",
  "duration_sec": 15,
  "timestamp": "2025-11-12T10:00:00"
}
```

---

## 🖼 Step 5 — Image Description Task

- Show sample image
- Mic press & hold
- Validate 10–20 seconds
- Playback
- Submit

### Saved JSON Sample

```json
{
  "task_type": "image_description",
  "image_url": "https://cdn.dummyjson.com/product-images/14/2.jpg",
  "audio_path": "/local/path/desc_audio.mp3",
  "duration_sec": 12,
  "timestamp": "2025-11-12T10:10:00"
}
```

---

## 📷 Step 6 — Photo Capture Task

- Camera permission
- Capture + Preview
- Text description
- Optional audio recording
- Retake / Submit buttons

### Saved JSON Sample

```json
{
  "task_type": "photo_capture",
  "image_path": "/local/path/photo.jpg",
  "audio_path": "/local/path/photo_audio.mp3",
  "duration_sec": 18,
  "timestamp": "2025-11-12T10:15:00"
}
```

---

## 📚 Step 7 — Task History Screen

Shows:

- Total tasks
- Total recording duration
- Task list with:
  - ID
  - Type
  - Duration
  - Timestamp
  - Text snippet or image thumbnail

---

# 📦 Local Storage Format

```json
[
  {
    "task_type": "text_reading",
    "text": "description here...",
    "audio_path": "/path/audio.mp3",
    "duration_sec": 15,
    "timestamp": "2025-11-12T10:00:00"
  }
]
```

---

# ✔ Completed Features

- KMM + CMP architecture
- Decibel noise test
- Press-and-hold audio recording
- Duration validation
- API text & image fetching
- Camera capture
- Local task storage
- History UI with previews

---
