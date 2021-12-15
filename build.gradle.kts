import Com_mineinabyss_conventions_platform_gradle.Deps

val idofrontVersion: String by project
val gearyVersion: String by project
val gearyAddonsVersion: String by project
val lootyVersion: String by project
val deeperWorldVersion: String by project
val exposedVersion: String by project

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
        maven("https://repo.codemc.io/repository/maven-snapshots/")
    }

    dependencies {
        // MineInAbyss platform
        compileOnly(Deps.kotlin.stdlib)
        compileOnly(Deps.kotlinx.serialization.json)
        compileOnly(Deps.kotlinx.serialization.kaml)
        compileOnly(Deps.kotlinx.coroutines)
        compileOnly(Deps.minecraft.skedule)

        compileOnly("org.jetbrains.exposed:exposed-core:$exposedVersion") { isTransitive = false }
        compileOnly("org.jetbrains.exposed:exposed-dao:$exposedVersion") { isTransitive = false }
        compileOnly("org.jetbrains.exposed:exposed-jdbc:$exposedVersion") { isTransitive = false }
        compileOnly("org.jetbrains.exposed:exposed-java-time:$exposedVersion") { isTransitive = false }

        compileOnly("org.xerial:sqlite-jdbc:3.30.1")

        // Plugin deps
        compileOnly("com.mineinabyss:deeperworld:$deeperWorldVersion")
        compileOnly("com.mineinabyss:geary-platform-papermc:$gearyVersion")
        compileOnly("com.mineinabyss:geary-commons-papermc:$gearyAddonsVersion")
        compileOnly("com.mineinabyss:looty:$lootyVersion")
        compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit") }
        compileOnly("com.mineinabyss:guiy-compose:0.1.4")
        compileOnly("nl.rutgerkok:blocklocker:1.10.2-SNAPSHOT")

        implementation("com.mineinabyss:idofront:$idofrontVersion")
        // TODO probably worth putting into idofront
        implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")
    }
}

dependencies {

    // Shaded
    implementation(project(":mineinabyss-core"))
    implementation(project(":mineinabyss-systems"))
}
