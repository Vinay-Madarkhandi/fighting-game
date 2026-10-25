package com.vinay.minimilitia

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

object DesktopLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setTitle("Mini Militia (Doodle Army 2 Replica)")
            setWindowedMode(1280, 720)
            useVsync(true)
            setForegroundFPS(60)
            setWindowIcon("textures/particles/shell_new.png")
        }
        Lwjgl3Application(MiniMilitiaGame(), config)
    }
}
