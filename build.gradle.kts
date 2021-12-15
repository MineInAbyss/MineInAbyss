import Com_mineinabyss_conventions_platform_gradle.Deps

val idofrontVersion: String by project
val gearyVersion: String by project
val gearyAddonsVersion: String by project
val lootyVersion: String by project
val deeperWorldVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    kotlin("plugin.serialization")
}

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://erethon.de/repo/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
    }

    dependencies {
        // MineInAbyss platform
        compileOnly(Deps.kotlin.stdlib)
        compileOnly(Deps.kotlinx.serialization.json)
        compileOnly(Deps.kotlinx.serialization.kaml)
        compileOnly(Deps.kotlinx.coroutines)
        compileOnly(Deps.minecraft.skedule)

        // Plugin deps
        compileOnly("com.mineinabyss:deeperworld:$deeperWorldVersion")
        compileOnly("com.mineinabyss:geary-platform-papermc:$gearyVersion")
        compileOnly("com.mineinabyss:geary-commons-papermc:$gearyAddonsVersion")
        compileOnly("com.mineinabyss:looty:$lootyVersion")
        compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit") }
        compileOnly("com.mineinabyss:guiy-compose:0.1.4")

        implementation("com.mineinabyss:idofront:$idofrontVersion")
    }
}

dependencies {

    // Shaded
    implementation(project(":mineinabyss-core"))
    implementation(project(":mineinabyss-systems"))
}
