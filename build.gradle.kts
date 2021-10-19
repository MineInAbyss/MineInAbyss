import Com_mineinabyss_conventions_platform_gradle.Deps

val idofrontVersion: String by project
val gearyVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.slimjar")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    kotlin("plugin.serialization")
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    // Plugin deps
    compileOnly("com.mineinabyss:geary-platform-papermc:$gearyVersion")
    compileOnly("com.mineinabyss:geary-commons-papermc:0.1.10")
    compileOnly("com.mineinabyss:looty:0.3.32")
    compileOnly("com.derongan.minecraft:deeperworld:0.3.58")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit") }

    // From Geary
    slim(Deps.kotlinx.serialization.json)
    slim(Deps.kotlinx.serialization.kaml)
    slim(Deps.kotlinx.coroutines)
    slim(Deps.minecraft.skedule)

    // Shaded
    implementation("com.mineinabyss:idofront:$idofrontVersion")
}
