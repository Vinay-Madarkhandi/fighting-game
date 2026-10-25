package com.vinay.minimilitia

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import org.lwjgl.system.JNI
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Platform
import org.lwjgl.system.linux.DynamicLinkLoader
import java.io.File

object DesktopLauncher {

    private fun configureLinuxDisplay() {
        if (Platform.get() != Platform.LINUX) return

        try {
            val libc = DynamicLinkLoader.dlopen("libc.so.6", DynamicLinkLoader.RTLD_LAZY)
            if (libc == 0L) return
            val setenvFunc = DynamicLinkLoader.dlsym(libc, "setenv")
            if (setenvFunc == 0L) return

            fun setenvC(key: String, value: String) {
                MemoryStack.stackPush().use { stack ->
                    val k = MemoryUtil.memAddress(stack.UTF8(key))
                    val v = MemoryUtil.memAddress(stack.UTF8(value))
                    JNI.callPPI(k, v, 0, setenvFunc) // 0 = do not overwrite if already set by user
                }
            }

            // Determine user runtime dir
            val runtimeDir = File("/run/user/1000")
            if (runtimeDir.exists()) {
                if (System.getenv("XDG_RUNTIME_DIR").isNullOrEmpty()) {
                    setenvC("XDG_RUNTIME_DIR", "/run/user/1000")
                }

                // Check Wayland socket
                val waylandSocket = File(runtimeDir, "wayland-0")
                if (waylandSocket.exists()) {
                    setenvC("WAYLAND_DISPLAY", "wayland-0")
                }

                // Check Xwayland auth
                val authFile = runtimeDir.listFiles()?.firstOrNull { it.name.startsWith(".mutter-Xwaylandauth") }
                if (authFile != null) {
                    setenvC("XAUTHORITY", authFile.absolutePath)
                }

                // Default X11 display for Xwayland
                setenvC("DISPLAY", ":1")
            } else {
                setenvC("DISPLAY", ":0")
            }
        } catch (_: Throwable) {
            // Non-critical: let GLFW use system defaults if dlopen fails
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        configureLinuxDisplay()

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

