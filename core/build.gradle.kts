plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.22"
}

val gdxVersion = "1.12.1"
val box2dVersion = "1.12.1"
val ktorVersion = "2.3.9"

dependencies {
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
    api("com.badlogicgames.gdx:gdx-box2d:$box2dVersion")
    api("com.badlogicgames.gdx:gdx-freetype:$gdxVersion")
    
    // Coroutines & Serialization
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Ktor for networking (WebSockets for LAN & Online)
    api("io.ktor:ktor-client-core:$ktorVersion")
    api("io.ktor:ktor-client-cio:$ktorVersion")
    api("io.ktor:ktor-client-websockets:$ktorVersion")
    api("io.ktor:ktor-server-core:$ktorVersion")
    api("io.ktor:ktor-server-cio:$ktorVersion")
    api("io.ktor:ktor-server-websockets:$ktorVersion")
    
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(21)
}
