plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.nms")
    id("com.mineinabyss.conventions.autoversion")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

dependencies {
    compileOnly(project(":mineinabyss-core"))

    compileOnly(miaLibs.guiy)
    compileOnly(miaLibs.chatty)
    compileOnly(libs.minecraft.headlib)
}
