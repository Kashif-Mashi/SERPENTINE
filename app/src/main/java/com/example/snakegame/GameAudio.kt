package com.example.snakegame

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tanh
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

// =================================================================================================
// AUDIO ENGINE
//  - Background music: bright 8-bit arcade loop (C major, 132 BPM) with lead, bass, chord stabs
//    and drums, synthesised in code and looped by AudioTrack. No audio files.
//  - Sound effects: eat (pitch rises with combo), golden apple, speed boost, crash, UI click.
//    Generated once, written as tiny WAV files to the cache folder and played by SoundPool
//    (low latency, overlapping sounds).
// =================================================================================================

private const val AUDIO_SR = 32000

class GameAudio(private val context: Context) {

    private var musicTrack: AudioTrack? = null
    private var soundPool: SoundPool? = null
    private var idEat = 0
    private var idGolden = 0
    private var idBoost = 0
    private var idCrash = 0
    private var idClick = 0
    private var ready = false
    private var musicVolume = 0.5f
    private var boosted = false

    @Volatile
    var sfxEnabled: Boolean = true

    // ---------------------------------------------------------------------------------------------
    // Setup (call from a background thread while the splash screen is showing)
    // ---------------------------------------------------------------------------------------------

    @Synchronized
    fun prepare() {
        if (ready) return
        try {
            buildMusic()
        } catch (_: Exception) {
            musicTrack = null
        }
        try {
            buildSfx()
        } catch (_: Exception) {
            soundPool = null
        }
        ready = true
    }

    private fun buildMusic() {
        val pcm = AudioSynth.musicLoop()
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(AUDIO_SR)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(pcm, 0, pcm.size)
        track.setLoopPoints(0, pcm.size, -1) // -1 = loop forever
        track.setVolume(musicVolume)
        musicTrack = track
    }

    private fun buildSfx() {
        val pool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        idEat = loadWav(pool, "sfx_eat.wav", AudioSynth.eat())
        idGolden = loadWav(pool, "sfx_golden.wav", AudioSynth.golden())
        idBoost = loadWav(pool, "sfx_boost.wav", AudioSynth.boost())
        idCrash = loadWav(pool, "sfx_crash.wav", AudioSynth.crash())
        idClick = loadWav(pool, "sfx_click.wav", AudioSynth.click())
        soundPool = pool
    }

    private fun loadWav(pool: SoundPool, name: String, pcm: ShortArray): Int {
        val file = File(context.cacheDir, name)
        file.writeBytes(wavBytes(pcm))
        return pool.load(file.absolutePath, 1)
    }

    private fun wavBytes(pcm: ShortArray): ByteArray {
        val dataLen = pcm.size * 2
        val buf = ByteBuffer.allocate(44 + dataLen).order(ByteOrder.LITTLE_ENDIAN)
        buf.put("RIFF".toByteArray(Charsets.US_ASCII))
        buf.putInt(36 + dataLen)
        buf.put("WAVE".toByteArray(Charsets.US_ASCII))
        buf.put("fmt ".toByteArray(Charsets.US_ASCII))
        buf.putInt(16)
        buf.putShort(1.toShort())              // PCM
        buf.putShort(1.toShort())              // mono
        buf.putInt(AUDIO_SR)
        buf.putInt(AUDIO_SR * 2)               // byte rate
        buf.putShort(2.toShort())              // block align
        buf.putShort(16.toShort())             // bits per sample
        buf.put("data".toByteArray(Charsets.US_ASCII))
        buf.putInt(dataLen)
        for (s in pcm) buf.putShort(s)
        return buf.array()
    }

    // ---------------------------------------------------------------------------------------------
    // Music controls
    // ---------------------------------------------------------------------------------------------

    @Synchronized
    fun startMusic() {
        if (!ready) prepare()
        try {
            musicTrack?.play()
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun pauseMusic() {
        try {
            val t = musicTrack ?: return
            if (t.playState == AudioTrack.PLAYSTATE_PLAYING) t.pause()
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun setMusicVolume(v: Float) {
        musicVolume = v.coerceIn(0f, 1f)
        try {
            musicTrack?.setVolume(musicVolume)
        } catch (_: Exception) {
        }
    }

    /** While a speed boost is active the music plays ~12% faster (slightly higher pitch) for extra thrill. */
    @Synchronized
    fun setMusicBoost(on: Boolean) {
        if (on == boosted) return
        boosted = on
        try {
            val rate = if (on) (AUDIO_SR * 1.12f).toInt() else AUDIO_SR
            musicTrack?.setPlaybackRate(rate)
        } catch (_: Exception) {
        }
    }

    // Compatibility aliases
    fun start() = startMusic()
    fun pause() = pauseMusic()
    fun setVolume(v: Float) = setMusicVolume(v)

    // ---------------------------------------------------------------------------------------------
    // Sound effects
    // ---------------------------------------------------------------------------------------------

    /** combo 0..5: each quick consecutive apple climbs the major scale (do re mi sol la do). */
    fun playEat(combo: Int = 0) {
        if (!sfxEnabled || idEat == 0) return
        val semitones = intArrayOf(0, 2, 4, 7, 9, 12)[combo.coerceIn(0, 5)]
        val rate = 2.0.pow(semitones / 12.0).toFloat()
        soundPool?.play(idEat, 1f, 1f, 1, 0, rate)
    }

    fun playGolden() {
        play(idGolden, 1f)
    }

    fun playBoost() {
        play(idBoost, 1f)
    }

    fun playCrash() {
        play(idCrash, 1f)
    }

    fun playClick() {
        play(idClick, 0.8f)
    }

    // Compatibility aliases for previous SFX calls
    fun playButtonClick() = playClick()
    fun playGoldenChime() = playGolden()
    fun playSpeedBoost() = playBoost()
    fun playCoin() = playGolden()

    private fun play(id: Int, volume: Float) {
        if (!sfxEnabled || id == 0) return
        soundPool?.play(id, volume, volume, 1, 0, 1f)
    }

    @Synchronized
    fun release() {
        try {
            musicTrack?.release()
        } catch (_: Exception) {
        }
        try {
            soundPool?.release()
        } catch (_: Exception) {
        }
        musicTrack = null
        soundPool = null
        ready = false
    }
}

// =================================================================================================
// SYNTHESIS
// =================================================================================================

private object AudioSynth {
    private const val SR = AUDIO_SR

    fun midiToFreq(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

    private fun pulse(phase: Double, duty: Double): Double {
        val p = phase - floor(phase)
        return if (p < duty) 1.0 else -1.0
    }

    private fun tri(phase: Double): Double {
        val p = phase - floor(phase)
        return 4.0 * abs(p - 0.5) - 1.0
    }

    /** Soft clipper + convert to 16-bit. */
    private fun clip16(v: Double): Short = (tanh(v) * 0.9 * 32767.0).toInt().toShort()

    private inline fun render(durSec: Double, sample: (Double) -> Double): ShortArray {
        val n = (durSec * SR).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            out[i] = clip16(sample(i.toDouble() / SR))
        }
        // 6 ms fade-out so there is never a click at the end of a sound
        val fade = (0.006 * SR).toInt()
        for (k in 0 until min(fade, n)) {
            val idx = n - 1 - k
            out[idx] = (out[idx] * (k.toFloat() / fade)).toInt().toShort()
        }
        return out
    }

    // ---------------------------------------------------------------------------------------------
    // Background music: 8 bars of C - G - Am - F - C - G - F - G, 16 sixteenth-note steps per bar
    // ---------------------------------------------------------------------------------------------

    fun musicLoop(): ShortArray {
        val bpm = 132.0
        val stepSec = 60.0 / bpm / 4.0
        val stepFrames = (SR * stepSec).toInt()
        val bars = 8
        val total = bars * 16 * stepFrames
        val mix = FloatArray(total)
        val rng = Random(42)

        // Notes that run past the end wrap around to the start, so the loop is seamless.
        fun add(pos: Int, v: Double) {
            val i = pos % total
            mix[i] = mix[i] + v.toFloat()
        }

        fun at(step: Int, bar: Int): Int = (bar * 16 + step) * stepFrames

        fun lead(startFrame: Int, durSec: Double, midi: Int, amp: Double) {
            val f = midiToFreq(midi)
            val n = ((durSec + 0.12) * SR).toInt()
            val w = 2.0 * PI * 5.5 // gentle vibrato
            for (i in 0 until n) {
                val t = i.toDouble() / SR
                val phase = f * t + (f * 0.004 / w) * (1.0 - cos(w * t))
                val att = min(1.0, t / 0.003)
                val dec = 0.72 + 0.28 * exp(-t * 22.0)
                val rel = if (t > durSec) exp(-(t - durSec) * 45.0) else 1.0
                val wave = 0.55 * pulse(phase, 0.5) + 0.45 * pulse(phase * 1.005, 0.25)
                add(startFrame + i, wave * att * dec * rel * amp)
            }
        }

        fun stab(startFrame: Int, midi: Int, amp: Double) {
            val f = midiToFreq(midi)
            val n = (0.12 * SR).toInt()
            for (i in 0 until n) {
                val t = i.toDouble() / SR
                val env = min(1.0, t / 0.002) * exp(-t * 30.0)
                add(startFrame + i, pulse(f * t, 0.25) * env * amp)
            }
        }

        fun bass(startFrame: Int, durSec: Double, midi: Int, amp: Double) {
            val f = midiToFreq(midi)
            val n = ((durSec + 0.06) * SR).toInt()
            for (i in 0 until n) {
                val t = i.toDouble() / SR
                val att = min(1.0, t / 0.002)
                val body = 0.25 + 0.75 * exp(-t * 7.0)
                val rel = if (t > durSec) exp(-(t - durSec) * 70.0) else 1.0
                val p = f * t
                add(startFrame + i, (0.65 * tri(p) + 0.2 * pulse(p, 0.5)) * att * body * rel * amp)
            }
        }

        fun kick(startFrame: Int, amp: Double) {
            val n = (0.22 * SR).toInt()
            for (i in 0 until n) {
                val t = i.toDouble() / SR
                val phase = 45.0 * t + (95.0 / 30.0) * (1.0 - exp(-30.0 * t)) // pitch drops 140 -> 45 Hz
                val env = min(1.0, t / 0.001) * exp(-t * 16.0)
                add(startFrame + i, sin(2.0 * PI * phase) * env * amp)
            }
        }

        fun snare(startFrame: Int, amp: Double) {
            val n = (0.18 * SR).toInt()
            for (i in 0 until n) {
                val t = i.toDouble() / SR
                val noise = rng.nextDouble() * 2.0 - 1.0
                val v = 0.7 * noise * exp(-t * 26.0) + 0.4 * sin(2.0 * PI * 185.0 * t) * exp(-t * 38.0)
                add(startFrame + i, v * amp)
            }
        }

        fun hat(startFrame: Int, amp: Double) {
            val n = (0.05 * SR).toInt()
            var prev = 0.0
            for (i in 0 until n) {
                val t = i.toDouble() / SR
                val noise = rng.nextDouble() * 2.0 - 1.0
                val v = (noise - prev) * 0.5 * exp(-t * 110.0) // difference = crude high-pass
                prev = noise
                add(startFrame + i, v * amp)
            }
        }

        // Bass roots per bar: C G A F C G F G
        val roots = intArrayOf(48, 43, 45, 41, 48, 43, 41, 43)
        val minorBar = booleanArrayOf(false, false, true, false, false, false, false, false)
        // Eighth-note bass pattern (semitone offsets from the root): bouncy octave jumps
        val bassOffsets = intArrayOf(0, 0, 12, 0, 0, 0, 12, 7)

        // Lead melody, 16 steps per bar. Positive = MIDI note, 0 = rest, -1 = hold previous note.
        val melody = arrayOf(
            intArrayOf(76, -1, 0, 79, -1, 0, 76, 0, 72, 0, 76, 0, 79, -1, -1, 0),     // C
            intArrayOf(74, -1, 0, 79, -1, 0, 83, 0, 81, 0, 79, 0, 74, -1, -1, 0),     // G
            intArrayOf(76, -1, 0, 81, -1, 0, 84, 0, 83, 0, 81, 0, 76, -1, -1, 0),     // Am
            intArrayOf(77, -1, 0, 81, -1, 0, 84, 0, 81, 0, 77, 0, 81, 0, 79, 0),      // F
            intArrayOf(76, 79, 84, -1, 79, -1, 76, -1, 79, 0, 76, 0, 72, -1, -1, 0),  // C
            intArrayOf(74, 79, 83, -1, 79, -1, 74, -1, 83, 0, 79, 0, 74, -1, -1, 0),  // G
            intArrayOf(72, 77, 81, -1, 77, -1, 84, -1, 81, 0, 77, 0, 81, 0, 84, 0),   // F
            intArrayOf(74, 0, 79, 0, 83, 0, 86, 0, 83, 79, -1, 0, 74, -1, -1, 0)      // G (turnaround)
        )

        for (bar in 0 until bars) {
            // Lead melody
            val m = melody[bar]
            var s = 0
            while (s < 16) {
                val note = m[s]
                if (note > 0) {
                    var len = 1
                    while (s + len < 16 && m[s + len] == -1) len++
                    lead(at(s, bar), len * stepSec * 0.9, note, 0.17)
                    s += len
                } else {
                    s++
                }
            }

            // Bass: eighth notes
            for (e in 0 until 8) {
                bass(at(e * 2, bar), stepSec * 2.0 * 0.85, roots[bar] + bassOffsets[e], 0.30)
            }

            // Off-beat chord stabs for bounce
            val third = if (minorBar[bar]) 3 else 4
            for (st in intArrayOf(6, 14)) {
                val base = roots[bar] + 12
                stab(at(st, bar), base, 0.04)
                stab(at(st, bar), base + third, 0.04)
                stab(at(st, bar), base + 7, 0.04)
            }

            // Drums
            val kicks = if (bar % 2 == 1) intArrayOf(0, 8, 10) else intArrayOf(0, 8)
            for (k in kicks) kick(at(k, bar), 0.55)

            val snares = mutableSetOf(4, 12)
            if (bar == 3) snares.add(15)
            if (bar == 7) {
                snares.add(13)
                snares.add(14)
                snares.add(15)
            }
            for (sn in snares) snare(at(sn, bar), 0.30)

            for (h in 0 until 16 step 2) hat(at(h, bar), 0.10)
        }

        val out = ShortArray(total)
        for (i in 0 until total) {
            out[i] = clip16(mix[i].toDouble() * 1.1)
        }
        return out
    }

    // ---------------------------------------------------------------------------------------------
    // Sound effects
    // ---------------------------------------------------------------------------------------------

    /** Crunch + rising two-note "ba-ding". Played at higher rates for combo pitch climbs. */
    fun eat(): ShortArray {
        val rng = Random(11)
        var lp = 0.0
        return render(0.30) { t ->
            var v = 0.0
            if (t < 0.05) { // crunch
                val noise = rng.nextDouble() * 2.0 - 1.0
                lp = lp * 0.55 + noise * 0.45
                v += lp * exp(-t * 90.0) * 0.55
            }
            if (t < 0.14) { // first note (C5)
                v += pulse(523.25 * t, 0.25) * min(1.0, t / 0.002) * exp(-t * 20.0) * 0.30
            }
            if (t >= 0.055) { // second note (G5) + sparkle
                val tt = t - 0.055
                v += pulse(783.99 * tt, 0.5) * min(1.0, tt / 0.002) * exp(-tt * 13.0) * 0.30
                v += sin(2.0 * PI * 1567.98 * tt) * exp(-tt * 20.0) * 0.12
            }
            v
        }
    }

    /** Fast sparkling arpeggio up to a bell. */
    fun golden(): ShortArray {
        val notes = intArrayOf(84, 88, 91, 96)
        return render(0.65) { t ->
            var v = 0.0
            for (k in notes.indices) {
                val tt = t - k * 0.055
                if (tt >= 0.0) {
                    val f = midiToFreq(notes[k])
                    val env = min(1.0, tt / 0.002) * exp(-tt * 8.0)
                    v += (0.55 * pulse(f * tt, 0.25) + 0.45 * sin(2.0 * PI * f * tt)) * env * 0.20
                }
            }
            v += sin(2.0 * PI * 3520.0 * t) * (0.5 + 0.5 * sin(2.0 * PI * 18.0 * t)) * exp(-t * 6.0) * 0.05
            v
        }
    }

    /** Power-up: rising whoosh then a bright ding. */
    fun boost(): ShortArray {
        val rng = Random(21)
        return render(0.65) { t ->
            var v = 0.0
            if (t < 0.4) {
                val phase = 300.0 * t + 1500.0 * t * t * t / (3.0 * 0.16) // 300 -> 1800 Hz sweep
                val taper = if (t > 0.38) (0.4 - t) / 0.02 else 1.0
                val env = min(1.0, t / 0.01) * (0.4 + 0.6 * (t / 0.4)) * taper
                v += pulse(phase, 0.5) * env * 0.16
                v += (rng.nextDouble() * 2.0 - 1.0) * (t / 0.4) * 0.10 * taper
            } else {
                val tt = t - 0.4
                v += sin(2.0 * PI * 1760.0 * tt) * exp(-tt * 12.0) * 0.30
                v += sin(2.0 * PI * 2349.3 * tt) * exp(-tt * 14.0) * 0.18
            }
            v
        }
    }

    /** Crash: noise burst, falling square tone and a low thump. */
    fun crash(): ShortArray {
        val rng = Random(31)
        var lp = 0.0
        return render(0.75) { t ->
            val noise = rng.nextDouble() * 2.0 - 1.0
            lp = lp * 0.82 + noise * 0.18
            val phase = 380.0 * (1.0 - exp(-3.2 * t)) / 3.2 + 60.0 * t // glides ~380 -> 60 Hz
            var v = lp * exp(-t * 7.0) * 0.9
            v += pulse(phase, 0.5) * min(1.0, t / 0.002) * exp(-t * 4.0) * 0.22
            v += sin(2.0 * PI * 55.0 * t) * exp(-t * 9.0) * 0.5
            v
        }
    }

    /** Small UI tick. */
    fun click(): ShortArray = render(0.07) { t ->
        (sin(2.0 * PI * 1200.0 * t) * 0.6 + sin(2.0 * PI * 1800.0 * t) * 0.4) *
            min(1.0, t / 0.001) * exp(-t * 55.0) * 0.35
    }
}

// ==========================================
// COMPATIBILITY SHIMS
// ==========================================

class MusicPlayer(private val audio: GameAudio? = null) {
    fun prepare() { audio?.prepare() }
    fun start() { audio?.startMusic() }
    fun pause() { audio?.pauseMusic() }
    fun setVolume(v: Float) { audio?.setMusicVolume(v) }
    fun release() { audio?.release() }
}

class SoundEffectsPlayer(private val audio: GameAudio? = null) {
    fun playEat() { audio?.playEat() }
    fun playGoldenChime() { audio?.playGolden() }
    fun playSpeedBoost() { audio?.playBoost() }
    fun playCrash() { audio?.playCrash() }
    fun playButtonClick() { audio?.playClick() }
    fun playCoin() { audio?.playGolden() }
}
