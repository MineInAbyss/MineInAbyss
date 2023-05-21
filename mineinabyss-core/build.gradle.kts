plugins {
    alias(libs.plugins.mia.kotlin.jvm)
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":mineinabyss-components"))
    compileOnly(libs.kotlin.reflect)
}
