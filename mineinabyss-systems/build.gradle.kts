import Com_mineinabyss_conventions_platform_gradle.Deps

val idofrontVersion: String by project
val gearyVersion: String by project
val lootyVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose") version "1.0.0-alpha4-build398"
}

dependencies {
    compileOnly("com.mineinabyss:geary-platform-papermc:$gearyVersion")
    compileOnly("com.mineinabyss:geary-commons-papermc:0.1.11")
    compileOnly("com.mineinabyss:looty:$lootyVersion")
    compileOnly("com.mineinabyss:guiy-compose:0.1.2")
    compileOnly(Deps.kotlinx.coroutines)

    compileOnly(Deps.kotlinx.serialization.json)
    compileOnly(Deps.minecraft.skedule)
    compileOnly("com.derongan.minecraft:deeperworld:0.3.58")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit") }
    compileOnly(project(":mineinabyss-core"))
    implementation("${Deps.minecraft.headlib}:3.0.7")
}
