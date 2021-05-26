import com.mineinabyss.kotlinSpice
import com.mineinabyss.mineInAbyss
import com.mineinabyss.sharedSetup

plugins {
    java
    idea
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.mineinabyss.shared-gradle") version "0.0.6"
}

sharedSetup()

val kotlinVersion: String by project
val serverVersion: String by project

repositories {
    mavenCentral()
    jcenter()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.io/repository/nms/")
    mineInAbyss()
    maven("https://erethon.de/repo/")
    maven("https://jitpack.io")
//    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$serverVersion")
    compileOnly(kotlin("stdlib-jdk8"))

    kotlinSpice("$kotlinVersion+")
    compileOnly("com.github.okkero:skedule")
    compileOnly("de.erethon:headlib")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group ="org.bukkit")
    }

    compileOnly("com.mineinabyss:geary-spigot:0.4.42")
    compileOnly("com.mineinabyss:looty:0.3")
    compileOnly("com.derongan.minecraft:deeperworld:0.3.47")

    implementation("com.derongan.minecraft:guiy:0.1.0-alpha")
    implementation("com.mineinabyss:idofront:0.6.13")
}


tasks {
    shadowJar {
        minimize {
            exclude(dependency("de.erethon:headlib:."))
        }

        relocate("com.derongan.minecraft.guiy", "${project.group}.${project.name}.guiy".toLowerCase())
        relocate("com.mineinabyss.idofront", "${project.group}.${project.name}.idofront".toLowerCase())
    }

    build {
        dependsOn(shadowJar)
    }
}

publishing {
    mineInAbyss(project) {
        from(components["java"])
    }
}
