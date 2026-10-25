plugins {
    id("com.android.application") version "8.3.2" apply false
    kotlin("android") version "1.9.22" apply false
    kotlin("jvm") version "1.9.22" apply false
}

val gdxVersion = "1.12.1"
val box2dVersion = "1.12.1"
val ktorVersion = "2.3.9"

extra["gdxVersion"] = gdxVersion
extra["box2dVersion"] = box2dVersion
extra["ktorVersion"] = ktorVersion
