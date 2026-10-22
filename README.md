# Serpentine 🐍
> **Eat. Grow. Survive.**

A modern, cyberpunk-inspired retro Snake game built from the ground up for Android using **Jetpack Compose**, **Kotlin**, and **Canvas** graphics. Featuring real-time procedural chiptune audio, customizable themes, tactile haptic feedback, and fluid animations.

---

## ✨ Features

- 🎨 **4 Visual Themes:**
  - **Matrix Neon** (Classic hacker green glow)
  - **Cyberpunk Pink** (Vibrant synthwave aesthetic)
  - **Retro GameBoy** (Nostalgic 90s monochrome LCD)
  - **Midnight Blue** (Sleek dark cyber space)
- 🎵 **Procedural Retro BGM Engine:**
  - Built-in 8-bit synthesizer running on Android's `AudioTrack`.
  - Zero heavy audio files; melodies and sound effects are generated in real-time.
- 📳 **Haptic Feedback:**
  - Distinct vibration patterns for turns, eating food, powerups, and game-over crashes.
  - Full toggle in Settings to turn vibration on/off anytime.
- 🕹️ **Dual Control System:**
  - Ergonomic virtual D-Pad for precise retro input.
  - Full-screen touch gestures and swipe navigation.
- 🍎 **Dynamic Food & Power-ups:**
  - **Normal Food (+1 pt)**: Classic growth pellet.
  - **Golden Apple (+5 pts)**: Timed bonus food for quick score surges.
  - **Speed Boost (+2 pts)**: High-speed adrenaline rush.
- 💥 **Particle Effects:**
  - Custom Canvas particle explosions upon eating food or crashing.
- 📊 **Local Score Tracking:**
  - Persistent high-score tracking across sessions.

---

## 🛠️ Tech Stack & Architecture

- **Language:** [Kotlin](https://kotlinlang.org/) (JVM 17 / 21)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Graphics:** Android Jetpack Compose `Canvas` with hardware acceleration
- **Sound Generation:** Low-latency procedural synth via `android.media.AudioTrack`
- **Build System:** Gradle (Kotlin DSL) with Android Gradle Plugin 8.7+

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2+) or newer.
- **JDK 17** or **JDK 21** configured as Gradle JVM.
- Android Device or Emulator running **Android 7.0 (API 24)** or higher.

### Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Serpentine.git
   ```
2. **Open in Android Studio:**
   - Select **Open an Existing Project** and browse to the cloned folder.
3. **Sync & Build:**
   - Allow Gradle to sync dependencies.
   - Run on an emulator or connect your Android device via USB debugging.

---

## 🎮 How to Play

1. Use the **Virtual D-Pad** or **Swipe** on screen to steer your snake.
2. Collect normal pellets, timed golden apples, and speed boosts to increase your score and snake length.
3. Avoid colliding with walls or your own tail.
4. Tap the **Gear** ⚙️ icon to toggle Sound, Vibration, Difficulty, or switch Themes.

---

## 📄 License
This project is open source and available under the [MIT License](LICENSE).
