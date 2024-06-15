plugins {
    alias(idofrontLibs.plugins.mia.kotlin.jvm)
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.nms")
    kotlin("plugin.serialization")
    alias(idofrontLibs.plugins.compose.compiler)
    alias(idofrontLibs.plugins.mia.publication)
}

dependencies {
    api(project(":mineinabyss-components"))
    compileOnly(idofrontLibs.minecraft.plugin.worldguard)
}
