val idofrontVersion: String by project
val gearyPlatformVersion: String by project
val deeperWorldVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
//    kotlin("plugin.serialization") apply false
    kotlin("jvm") apply false
}

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.dmulloy2.net/nexus/repository/public/") //ProtocolLib
        maven("https://jitpack.io")
    }

    dependencies {
        val libs = rootProject.libs

        // Geary platform
        compileOnly(platform("com.mineinabyss:geary-platform:$gearyPlatformVersion"))
        compileOnly("com.mineinabyss:geary-papermc-core")
        compileOnly("com.mineinabyss:geary-commons-papermc")
        compileOnly("com.mineinabyss:looty")

        // MineInAbyss platform
        compileOnly(libs.kotlin.stdlib)
        compileOnly(libs.kotlinx.serialization.json)
        compileOnly(libs.kotlinx.serialization.kaml)
        compileOnly(libs.kotlinx.coroutines)
        compileOnly(libs.minecraft.skedule)
        compileOnly(libs.reflections)
        implementation(libs.idofront.autoscan) {
            exclude("org.reflections")
        }

        compileOnly(libs.exposed.core) { isTransitive = false }
        compileOnly(libs.exposed.dao) { isTransitive = false }
        compileOnly(libs.exposed.jdbc) { isTransitive = false }
        compileOnly(libs.exposed.javatime) { isTransitive = false }
        compileOnly(libs.sqlite.jdbc)
        compileOnly(libs.minecraft.anvilgui)

        // Plugin libs
        compileOnly("com.mineinabyss:deeperworld:$deeperWorldVersion")
        compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit") }
        compileOnly("nl.rutgerkok:blocklocker:1.10.2-SNAPSHOT")
        compileOnly("com.gecolay:gsit:1.0.6")
        compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.1.1")
        compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.1.1") { isTransitive = false }

        compileOnly("com.mineinabyss:protocolburrito:0.2.25")
        compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
        implementation("com.mineinabyss:idofront:$idofrontVersion")
    }
}

dependencies {

    // Shaded
    implementation(project(":mineinabyss-core"))
    implementation(project(":mineinabyss-features"))
}
