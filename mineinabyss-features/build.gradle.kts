plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.nms")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

dependencies {
    compileOnly(project(":mineinabyss-core"))

    compileOnly(mialibs.guiy)
    compileOnly(libs.minecraft.headlib)
}
