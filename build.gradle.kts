import Com_mineinabyss_conventions_platform_gradle.Deps

val idofrontVersion: String by project
val gearyVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.slimjar")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    id("org.jetbrains.compose") version "1.0.0-alpha4-build398"
    kotlin("plugin.serialization")
}

repositories {
    maven("https://erethon.de/repo/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
}

dependencies {
    slim(kotlin("stdlib-jdk8"))

    // Plugin deps
    compileOnly("com.mineinabyss:geary-platform-papermc:$gearyVersion")
    compileOnly("com.mineinabyss:geary-commons-papermc:0.1.11")
    compileOnly("com.mineinabyss:looty:0.3.32")
    compileOnly("com.derongan.minecraft:deeperworld:0.3.58")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit") }
    compileOnly("com.mineinabyss:guiy-compose:0.1.2")
    // From Geary
    slim(Deps.kotlinx.serialization.json)
    slim(Deps.kotlinx.serialization.kaml)
    slim(Deps.kotlinx.coroutines)
    slim(Deps.minecraft.skedule)
    implementation("${Deps.minecraft.headlib}:3.0.7")

    // Shaded
//    implementation("com.github.DRE2N.HeadLib:headlib-core:7e2d443678")
    implementation("com.mineinabyss:idofront:$idofrontVersion")
}
