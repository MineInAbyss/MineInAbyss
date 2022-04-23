val guiyVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.nms")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

dependencies {
    compileOnly("com.mineinabyss:guiy-compose:$guiyVersion")
    compileOnly(project(":mineinabyss-core"))

    compileOnly(libs.minecraft.headlib)
}
