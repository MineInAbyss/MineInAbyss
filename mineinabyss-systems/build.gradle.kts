import Com_mineinabyss_conventions_platform_gradle.Deps

val idofrontVersion: String by project
val gearyVersion: String by project
val gearyAddonsVersion: String by project
val lootyVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose") version "1.0.1-rc2"
}

dependencies {
    compileOnly("com.mineinabyss:guiy-compose:0.1.2")
    compileOnly(project(":mineinabyss-core"))

    implementation(Deps.minecraft.headlib)
}
