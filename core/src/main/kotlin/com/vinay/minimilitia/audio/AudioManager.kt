package com.vinay.minimilitia.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class AudioManager {
    private val sounds = mutableMapOf<String, Sound>()
    private var jetpackSoundId: Long = -1L
    private var isJetpackPlaying = false

    var sfxVolume = 0.8f
    var voiceVolume = 0.9f
    var isMuted = false

    fun getSound(name: String): Sound? {
        if (sounds.containsKey(name)) return sounds[name]

        val file = if (Gdx.files.internal("audio/sfx/$name").exists()) {
            Gdx.files.internal("audio/sfx/$name")
        } else if (Gdx.files.internal("audio/voice/$name").exists()) {
            Gdx.files.internal("audio/voice/$name")
        } else {
            null
        }

        return if (file != null) {
            try {
                val sound = Gdx.audio.newSound(file)
                sounds[name] = sound
                sound
            } catch (e: Exception) {
                Gdx.app.error("AudioManager", "Failed to load sound: $name", e)
                null
            }
        } else {
            null
        }
    }

    fun play(name: String, volumeScale: Float = 1.0f, pitch: Float = 1.0f): Long {
        if (isMuted) return -1
        val sound = getSound(name) ?: return -1
        return sound.play(sfxVolume * volumeScale, pitch, 0f)
    }

    fun playSpatial(name: String, listenerPos: Vector2, emitterPos: Vector2, maxDistance: Float = 25f): Long {
        if (isMuted) return -1
        val sound = getSound(name) ?: return -1
        val distance = listenerPos.dst(emitterPos)
        if (distance > maxDistance) return -1

        val volume = (1f - (distance / maxDistance).coerceIn(0f, 1f)) * sfxVolume
        val pan = ((emitterPos.x - listenerPos.x) / maxDistance).coerceIn(-1f, 1f)
        return sound.play(volume, 1f, pan)
    }

    fun startJetpackThrust() {
        if (isMuted || isJetpackPlaying) return
        val sound = getSound("thrust.wav") ?: return
        jetpackSoundId = sound.loop(sfxVolume * 0.7f)
        isJetpackPlaying = true
    }

    fun stopJetpackThrust() {
        if (!isJetpackPlaying) return
        val sound = getSound("thrust.wav") ?: return
        if (jetpackSoundId != -1L) {
            sound.stop(jetpackSoundId)
            jetpackSoundId = -1L
        }
        isJetpackPlaying = false
    }

    fun playDeathScream(listenerPos: Vector2, deathPos: Vector2) {
        val deathIndex = MathUtils.random(1, 9)
        playSpatial("death$deathIndex.wav", listenerPos, deathPos)
    }

    fun playVoiceLine(name: String) {
        if (isMuted) return
        val sound = getSound(name) ?: return
        sound.play(voiceVolume)
    }

    fun dispose() {
        stopJetpackThrust()
        sounds.values.forEach { it.dispose() }
        sounds.clear()
    }
}
