import Com_mineinabyss_conventions_platform_gradle.Deps

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":mineinabyss-components"))
    compileOnly(Deps.kotlin.reflect)
}
