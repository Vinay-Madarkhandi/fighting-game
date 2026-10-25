pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = java.net.URI("https://oss.sonatype.org/content/repositories/releases/") }
    }
}

rootProject.name = "MiniMilitia"
include(":core")
include(":desktop")
include(":android")
