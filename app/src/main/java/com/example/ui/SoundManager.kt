package com.example.ui

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null
    var isMusicPlaying = false
        private set

    // User audio setting states
    var isMusicMuted by mutableStateOf(false)
    var isSfxMuted by mutableStateOf(false)

    // Simple Synth Tone Generator
    fun playTone(frequency: Double, durationMs: Int, type: String = "sine", isSfx: Boolean = false) {
        if (isSfx && isSfxMuted) return
        scope.launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = FloatArray(numSamples)
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    samples[i] = when (type) {
                        "sine" -> sin(2.0 * Math.PI * frequency * t).toFloat()
                        "square" -> if (sin(2.0 * Math.PI * frequency * t) >= 0) 0.3f else -0.3f
                        "sawtooth" -> (2.0 * (t * frequency - Math.floor(t * frequency + 0.5))).toFloat() * 0.3f
                        else -> sin(2.0 * Math.PI * frequency * t).toFloat()
                    }
                    
                    // Apply ADSR envelope fade-out to prevent pops
                    val remaining = numSamples - i
                    if (remaining < 1000) {
                        samples[i] *= (remaining.toFloat() / 1000f)
                    }
                    if (i < 500) {
                        samples[i] *= (i.toFloat() / 500f)
                    }
                }

                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    buffer[i] = (samples[i] * Short.MAX_VALUE).toInt().toShort()
                }

                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    AudioTrack.MODE_STATIC
                )
                
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay(durationMs.toLong() + 50)
                audioTrack.release()
            } catch (e: Exception) {
                Log.e("SoundManager", "Error playing synth tone: ${e.message}")
            }
        }
    }

    // Play complex retro sound paths
    fun playDiceRoll() {
        scope.launch {
            for (i in 1..4) {
                playTone(300.0 + (i * 150), 60, "square", isSfx = true)
                delay(70)
            }
        }
    }

    fun playPieceMove() {
        playTone(600.0, 100, "sine", isSfx = true)
    }

    fun playChessMove() {
        playTone(220.0, 80, "sine", isSfx = true)
    }

    fun playChessCapture() {
        scope.launch {
            playTone(150.0, 120, "sawtooth", isSfx = true)
            delay(50)
            playTone(100.0, 200, "sawtooth", isSfx = true)
        }
    }

    fun playCricketBowl() {
        // Swoosh sound
        scope.launch {
            for (freq in generateSequence(800.0) { it - 40.0 }.take(10)) {
                playTone(freq, 30, "sine", isSfx = true)
                delay(20)
            }
        }
    }

    fun playCricketBatClick() {
        playTone(1200.0, 90, "square", isSfx = true)
    }

    fun playCricketSix() {
        scope.launch {
            playTone(880.0, 150, "sine", isSfx = true)
            delay(100)
            playTone(1320.0, 150, "sine", isSfx = true)
            delay(100)
            playTone(1760.0, 300, "square", isSfx = true)
        }
    }

    fun playCricketOut() {
        scope.launch {
            playTone(300.0, 150, "sawtooth", isSfx = true)
            delay(120)
            playTone(180.0, 400, "sawtooth", isSfx = true)
        }
    }

    // Ambient Cyberpunk Synth-wave background track!
    fun startBackgroundMusic() {
        if (isMusicPlaying) return
        isMusicPlaying = true
        
        musicJob = scope.launch {
            // Cool bassline sequence looping
            val bassNotes = doubleArrayOf(55.0, 55.0, 65.41, 65.41, 73.42, 73.42, 82.41, 82.41)
            var index = 0
            while (isActive) {
                if (isMusicPlaying && !isMusicMuted) {
                    // Play a quick, mellow ambient bass beat
                    playTone(bassNotes[index % bassNotes.size], 220, "sine", isSfx = false)
                    index++
                }
                delay(400) // 150 BPM feel
            }
        }
    }

    fun stopBackgroundMusic() {
        isMusicPlaying = false
        musicJob?.cancel()
        musicJob = null
    }
}
