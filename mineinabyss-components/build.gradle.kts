import Com_mineinabyss_conventions_platform_gradle.Deps

val gearyVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    kotlin("plugin.serialization")
}

dependencies {
    compileOnly("com.mineinabyss:geary-platform-papermc:$gearyVersion")
    compileOnly(Deps.kotlinx.serialization.json)
    compileOnly("com.derongan.minecraft:deeperworld:0.3.58")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit") }
}
