plugins {
    id("com.android.application")
    kotlin("android")
}

val gdxVersion: String by rootProject.extra
val box2dVersion: String by rootProject.extra

android {
    namespace = "com.vinay.minimilitia"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vinay.minimilitia"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            java.srcDirs("src/main/kotlin", "src/main/java")
            aidl.srcDirs("src/main/aidl")
            res.srcDirs("src/main/res")
            assets.srcDirs("../assets")
            jniLibs.srcDirs("libs")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

configurations {
    create("natives")
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    
    val platforms = listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
    for (p in platforms) {
        "natives"("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-$p")
        "natives"("com.badlogicgames.gdx:gdx-box2d-platform:$box2dVersion:natives-$p")
        "natives"("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-$p")
    }
    
    // Coroutines Android
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}

// Task to extract native libraries for android jniLibs
tasks.register("copyAndroidNatives") {
    doFirst {
        file("libs/armeabi-v7a/").mkdirs()
        file("libs/arm64-v8a/").mkdirs()
        file("libs/x86_64/").mkdirs()
        file("libs/x86/").mkdirs()

        configurations.getByName("natives").copy().files.forEach { jar ->
            val outputDir = when {
                jar.name.endsWith("natives-arm64-v8a.jar") -> file("libs/arm64-v8a")
                jar.name.endsWith("natives-armeabi-v7a.jar") -> file("libs/armeabi-v7a")
                jar.name.endsWith("natives-x86_64.jar") -> file("libs/x86_64")
                jar.name.endsWith("natives-x86.jar") -> file("libs/x86")
                else -> null
            }
            if (outputDir != null) {
                copy {
                    from(zipTree(jar))
                    into(outputDir)
                    include("*.so")
                }
            }
        }
    }
}

tasks.matching { it.name.contains("merge") && it.name.contains("JniLibFolders") }.configureEach {
    dependsOn("copyAndroidNatives")
}
