val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.publication")
    kotlin("plugin.serialization")
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    // Plugin deps
    compileOnly("com.mineinabyss:geary-platform-papermc:0.7.57")
    compileOnly("com.mineinabyss:geary-commons-papermc:0.1.2")
    compileOnly("com.mineinabyss:looty:0.3.17")
    compileOnly("com.derongan.minecraft:deeperworld:0.3.58")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group ="org.bukkit") }

    // From plugin deps
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json")
    compileOnly("com.charleskorn.kaml:kaml")
    compileOnly("com.github.okkero:skedule")

    // Shaded
    implementation("com.github.DRE2N:HeadLib:7e2d443678")
    implementation("com.derongan.minecraft:guiy:0.1.10")
    implementation("com.mineinabyss:idofront:$idofrontVersion")
}


tasks {
    shadowJar {
//        minimize()
//        relocate("com.derongan.minecraft.guiy", "${project.group}.${project.name}.guiy".toLowerCase())
//        relocate("com.mineinabyss.idofront", "${project.group}.${project.name}.idofront".toLowerCase())
    }

    build {
        dependsOn(shadowJar)
    }
}