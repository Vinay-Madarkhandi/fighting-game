package com.vinay.minimilitia

import com.badlogic.gdx.Game
import com.vinay.minimilitia.audio.AudioManager
import com.vinay.minimilitia.screens.MainMenuScreen

class MiniMilitiaGame : Game() {
    lateinit var audioManager: AudioManager
        private set

    override fun create() {
        audioManager = AudioManager()
        setScreen(MainMenuScreen(this))
    }

    override fun dispose() {
        super.dispose()
        audioManager.dispose()
        screen?.dispose()
    }
}
