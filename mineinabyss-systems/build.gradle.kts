import Com_mineinabyss_conventions_platform_gradle.Deps

val guiyVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose") version "1.0.1-rc2"
}

dependencies {
    compileOnly("com.mineinabyss:guiy-compose:$guiyVersion")
    compileOnly(project(":mineinabyss-core"))

    compileOnly(Deps.minecraft.headlib)
}
