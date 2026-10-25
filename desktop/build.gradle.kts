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
}
