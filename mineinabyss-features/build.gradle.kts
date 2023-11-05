plugins {
    alias(libs.plugins.mia.kotlin.jvm)
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.nms")
    kotlin("plugin.serialization")
    alias(libs.plugins.compose)
}

dependencies {
    api(project(":mineinabyss-components"))
    compileOnly(libs.minecraft.plugin.worldguard)
}
