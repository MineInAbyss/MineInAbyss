plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.autoversion")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":mineinabyss-components"))
    compileOnly(libs.kotlin.reflect)
}
