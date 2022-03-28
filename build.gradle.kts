import Com_mineinabyss_conventions_platform_gradle.Deps

val idofrontVersion: String by project
val gearyPlatformVersion: String by project
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
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.dmulloy2.net/nexus/repository/public/") //ProtocolLib
        maven("https://jitpack.io")
    }

    dependencies {
        // Geary platform
        compileOnly(platform("com.mineinabyss:geary-platform:$gearyPlatformVersion"))
        compileOnly("com.mineinabyss:geary-papermc-core")
        compileOnly("com.mineinabyss:geary-commons-papermc")
        compileOnly("com.mineinabyss:looty")

        // MineInAbyss platform
        compileOnly(Deps.kotlin.stdlib)
        compileOnly(Deps.kotlinx.serialization.json)
        compileOnly(Deps.kotlinx.serialization.kaml)
        compileOnly(Deps.kotlinx.coroutines)
        compileOnly(Deps.minecraft.skedule)

        compileOnly(Deps.exposed.core) { isTransitive = false }
        compileOnly(Deps.exposed.dao) { isTransitive = false }
        compileOnly(Deps.exposed.jdbc) { isTransitive = false }
        compileOnly(Deps.exposed.`java-time`) { isTransitive = false }
        compileOnly(Deps.`sqlite-jdbc`)
        compileOnly(Deps.minecraft.anvilgui)

        // Plugin deps
        compileOnly("com.mineinabyss:deeperworld:$deeperWorldVersion")
        compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit") }
        compileOnly("nl.rutgerkok:blocklocker:1.10.2-SNAPSHOT")
        compileOnly("com.gecolay:gsit:1.0.6")
        compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.1.0")
        compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.1.0") { isTransitive = false }

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

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
