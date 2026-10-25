package com.example.snakegame

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

// ==========================================
// HAPTIC FEEDBACK SUBSYSTEM
// ==========================================

enum class HapticType {
    LIGHT,
    MEDIUM,
    DOUBLE,
    HEAVY
}

fun performHaptic(context: Context, type: HapticType) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (type) {
                HapticType.LIGHT -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(18L, 60))
                }
                HapticType.MEDIUM -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(35L, 140))
                }
                HapticType.DOUBLE -> {
                    val timings = longArrayOf(0, 25, 40, 30)
                    val amplitudes = intArrayOf(0, 160, 0, 200)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                }
                HapticType.HEAVY -> {
                    val timings = longArrayOf(0, 60, 50, 120)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            when (type) {
                HapticType.LIGHT -> vibrator.vibrate(20L)
                HapticType.MEDIUM -> vibrator.vibrate(40L)
                HapticType.DOUBLE -> vibrator.vibrate(longArrayOf(0, 30, 40, 30), -1)
                HapticType.HEAVY -> vibrator.vibrate(180L)
            }
        }
    } catch (_: Exception) {
        // Silently catch in environments lacking vibrator hardware or permissions
    }
}

// ==========================================
// PROCEDURAL AUDIO SYNTH (BGM & SFX)
// ==========================================

private const val SAMPLE_RATE = 22050

private fun midiToFreq(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

/**
 * Builds a seamless 4-bar loop (Am - F - C - G, 128 BPM): triangle bass, pulse-wave arpeggio, soft hi-hat.
 */
private fun synthLoop(): ShortArray {
    val stepFrames = (SAMPLE_RATE * 60.0 / 128.0 / 2.0).toInt() // one eighth note
    val chords = arrayOf(
        intArrayOf(45, 57, 60, 64), // Am
        intArrayOf(41, 53, 57, 60), // F
        intArrayOf(48, 60, 64, 67), // C
        intArrayOf(43, 55, 59, 62)  // G
    )
    val arpPattern = intArrayOf(1, 2, 3, 2, 1, 3, 2, 3)
    val totalSteps = chords.size * 8
    val out = ShortArray(totalSteps * stepFrames)
    val noise = Random(7)

    for (step in 0 until totalSteps) {
        val chord = chords[step / 8]
        val inBar = step % 8
        val arpMidi = chord[arpPattern[inBar]] + (if (inBar % 4 == 3) 12 else 0)
        val bassMidi = chord[0] + (if (inBar == 4) 12 else 0)
        val arpFreq = midiToFreq(arpMidi)
        val bassFreq = midiToFreq(bassMidi)
        val base = step * stepFrames

        for (i in 0 until stepFrames) {
            val t = i.toDouble() / SAMPLE_RATE
            val attack = (t / 0.004).coerceAtMost(1.0)
            var s = 0.0

            // Arpeggio: 25% duty pulse wave
            val arpPhase = (arpFreq * t) % 1.0
            s += (if (arpPhase < 0.25) 1.0 else -1.0) * 0.16 * attack * exp(-5.0 * i / stepFrames)

            // Bass: triangle wave on even steps
            if (inBar % 2 == 0) {
                val bassPhase = (bassFreq * t) % 1.0
                s += (4.0 * abs(bassPhase - 0.5) - 1.0) * 0.32 * attack * exp(-3.0 * i / stepFrames)
            }

            // Hi-hat: short noise burst on odd steps
            if (inBar % 2 == 1 && t < 0.035) {
                s += (noise.nextDouble() * 2.0 - 1.0) * 0.05 * (1.0 - t / 0.035)
            }

            out[base + i] = (s.coerceIn(-1.0, 1.0) * 32767.0 * 0.8).toInt().toShort()
        }
    }
    return out
}

class MusicPlayer {
    private var track: AudioTrack? = null
    private var volume: Float = 0.6f

    @Synchronized
    fun prepare() {
        if (track != null) return
        try {
            val pcm = synthLoop()
            val t = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            t.write(pcm, 0, pcm.size)
            t.setLoopPoints(0, pcm.size, -1)
            t.setVolume(volume)
            track = t
        } catch (_: Exception) {
            track = null
        }
    }

    @Synchronized
    fun start() {
        prepare()
        try {
            track?.play()
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun pause() {
        try {
            val t = track ?: return
            if (t.playState == AudioTrack.PLAYSTATE_PLAYING) t.pause()
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun setVolume(v: Float) {
        volume = v.coerceIn(0f, 1f)
        try {
            track?.setVolume(volume)
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun release() {
        try {
            track?.release()
        } catch (_: Exception) {
        }
        track = null
    }
}

/**
 * Procedural retro sound effects synthesizer powered by AudioTrack.
 */
class SoundEffectsPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playEat() {
        playToneSequence(
            freqs = doubleArrayOf(523.25, 659.25, 783.99), // C5 -> E5 -> G5
            durationsMs = doubleArrayOf(30.0, 30.0, 50.0),
            volume = 0.45f
        )
    }

    fun playGoldenChime() {
        playToneSequence(
            freqs = doubleArrayOf(659.25, 830.61, 987.77, 1318.51), // E5, G#5, B5, E6
            durationsMs = doubleArrayOf(40.0, 40.0, 50.0, 90.0),
            volume = 0.55f
        )
    }

    fun playSpeedBoost() {
        playToneSequence(
            freqs = doubleArrayOf(440.0, 587.33, 739.99, 880.0),
            durationsMs = doubleArrayOf(25.0, 25.0, 30.0, 70.0),
            volume = 0.5f
        )
    }

    fun playCrash() {
        scope.launch {
            try {
                val totalFrames = (SAMPLE_RATE * 0.18).toInt()
                val pcm = ShortArray(totalFrames)
                val noise = Random(12)
                for (i in 0 until totalFrames) {
                    val progress = i.toDouble() / totalFrames
                    val amp = (1.0 - progress) * 0.6
                    val sample = (noise.nextDouble() * 2.0 - 1.0) * amp
                    pcm[i] = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                }
                playPcmDirect(pcm)
            } catch (_: Exception) {
            }
        }
    }

    fun playButtonClick() {
        playToneSequence(
            freqs = doubleArrayOf(880.0),
            durationsMs = doubleArrayOf(15.0),
            volume = 0.25f
        )
    }

    fun playCoin() {
        playToneSequence(
            freqs = doubleArrayOf(987.77, 1318.51), // B5 -> E6
            durationsMs = doubleArrayOf(45.0, 110.0),
            volume = 0.55f
        )
    }

    private fun playToneSequence(freqs: DoubleArray, durationsMs: DoubleArray, volume: Float) {
        scope.launch {
            try {
                var totalFrames = 0
                val framesPerNote = IntArray(freqs.size)
                for (i in freqs.indices) {
                    val f = (SAMPLE_RATE * (durationsMs[i] / 1000.0)).toInt()
                    framesPerNote[i] = f
                    totalFrames += f
                }

                val pcm = ShortArray(totalFrames)
                var offset = 0
                for (n in freqs.indices) {
                    val count = framesPerNote[n]
                    val freq = freqs[n]
                    for (i in 0 until count) {
                        val t = i.toDouble() / SAMPLE_RATE
                        val decay = 1.0 - (i.toDouble() / count) * 0.4
                        val s = sin(2.0 * Math.PI * freq * t) * decay * volume
                        pcm[offset + i] = (s.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                    }
                    offset += count
                }

                playPcmDirect(pcm)
            } catch (_: Exception) {
            }
        }
    }

    private fun playPcmDirect(pcm: ShortArray) {
        var track: AudioTrack? = null
        try {
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep((pcm.size.toDouble() / SAMPLE_RATE * 1000.0).toLong() + 20)
        } catch (_: Exception) {
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {
            }
        }
    }
}
