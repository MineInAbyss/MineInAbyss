import Com_mineinabyss_conventions_platform_gradle.Deps
import io.github.slimjar.func.slim

val idofrontVersion: String by project
val gearyVersion: String by project
val gearyAddonsVersion: String by project
val lootyVersion: String by project
val exposedVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.slimjar")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    kotlin("plugin.serialization")
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.mineinabyss.conventions.slimjar")

    repositories {
        mavenCentral()
        maven("https://erethon.de/repo/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/maven-snapshots/")
    }
    dependencies {
        implementation("com.mineinabyss:idofront:$idofrontVersion")
        implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")

        // Database
        slim("org.jetbrains.exposed:exposed-core:$exposedVersion") { isTransitive = false }
        slim("org.jetbrains.exposed:exposed-dao:$exposedVersion") { isTransitive = false }
        slim("org.jetbrains.exposed:exposed-jdbc:$exposedVersion") { isTransitive = false }
        slim("org.jetbrains.exposed:exposed-java-time:$exposedVersion") { isTransitive = false }

        // Sqlite
        slim("org.xerial:sqlite-jdbc:3.30.1")
    }
}



repositories {
    maven("https://erethon.de/repo/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    slim(kotlin("stdlib-jdk8"))

    // Plugin deps
    compileOnly("com.mineinabyss:geary-platform-papermc:$gearyVersion")
    compileOnly("com.mineinabyss:geary-commons-papermc:$gearyAddonsVersion")
    compileOnly("com.mineinabyss:looty:$lootyVersion")
    compileOnly("com.derongan.minecraft:deeperworld:0.3.70")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit") }
    compileOnly("com.mineinabyss:guiy-compose:0.1.2")

    // From Geary
    slim(Deps.kotlinx.serialization.json)
    slim(Deps.kotlinx.serialization.kaml)
    slim(Deps.kotlinx.coroutines)
    slim(Deps.minecraft.skedule)
    // Shaded
//    implementation("com.github.DRE2N.HeadLib:headlib-core:7e2d443678")
    implementation("com.mineinabyss:idofront:$idofrontVersion")

    implementation(project(":mineinabyss-core"))
    implementation(project(":mineinabyss-systems"))

    implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")

    // Database
    slim("org.jetbrains.exposed:exposed-core:$exposedVersion") { isTransitive = false }
    slim("org.jetbrains.exposed:exposed-dao:$exposedVersion") { isTransitive = false }
    slim("org.jetbrains.exposed:exposed-jdbc:$exposedVersion") { isTransitive = false }
    slim("org.jetbrains.exposed:exposed-java-time:$exposedVersion") { isTransitive = false }

    // Sqlite
    slim("org.xerial:sqlite-jdbc:3.30.1")
}
