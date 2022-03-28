import Com_mineinabyss_conventions_platform_gradle.Deps

val guiyVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.nms")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose") version "1.1.1"
}

dependencies {
    compileOnly("com.mineinabyss:guiy-compose:$guiyVersion")
    compileOnly(project(":mineinabyss-core"))

    compileOnly(Deps.minecraft.headlib)
}
