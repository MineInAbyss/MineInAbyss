plugins {
    alias(libs.plugins.mia.kotlin.jvm)
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.nms")
    kotlin("plugin.serialization")
    alias(libs.plugins.compose)
}

dependencies {
    compileOnly(project(":mineinabyss-core"))

    compileOnly(miaLibs.guiy)
    compileOnly(miaLibs.chatty)
//    compileOnly(libs.minecraft.headlib)
}
