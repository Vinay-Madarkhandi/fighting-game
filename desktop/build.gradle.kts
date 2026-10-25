import java.io.File

plugins {
    kotlin("jvm")
    application
}

val gdxVersion: String by rootProject.extra
val box2dVersion: String by rootProject.extra

application {
    mainClass.set("com.vinay.minimilitia.DesktopLauncher")
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-box2d-platform:$box2dVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
}

kotlin {
    jvmToolchain(21)
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.file("assets")
    environment(System.getenv())

    val runtimeDir = File("/run/user/1000")
    if (runtimeDir.exists()) {
        if (System.getenv("XDG_RUNTIME_DIR").isNullOrEmpty()) {
            environment("XDG_RUNTIME_DIR", "/run/user/1000")
        }
        val waylandSocket = File(runtimeDir, "wayland-0")
        if (System.getenv("WAYLAND_DISPLAY").isNullOrEmpty() && waylandSocket.exists()) {
            environment("WAYLAND_DISPLAY", "wayland-0")
        }
        val authFile = runtimeDir.listFiles()?.firstOrNull { it.name.startsWith(".mutter-Xwaylandauth") }
        if (System.getenv("DISPLAY").isNullOrEmpty()) {
            environment("DISPLAY", ":1")
            if (authFile != null && System.getenv("XAUTHORITY").isNullOrEmpty()) {
                environment("XAUTHORITY", authFile.absolutePath)
            }
        }
    }
}


