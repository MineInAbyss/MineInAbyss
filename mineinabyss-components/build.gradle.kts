plugins {
    alias(miaLibs.plugins.kotlin.jvm)
    alias(miaLibs.plugins.mia.papermc)
    alias(miaLibs.plugins.mia.nms)
    alias(miaLibs.plugins.mia.publication)
    kotlin("plugin.serialization")
}


dependencies {
    compileOnly(libs.deeperworld)
    compileOnly(miaLibs.geary.papermc)
    compileOnly(miaLibs.bundles.idofront.core)
    compileOnly(miaLibs.sqlite.kt)
    compileOnly(miaLibs.idofront.nms)
    api("com.mineinabyss:idofront-datastore")
    compileOnly(miaLibs.minecraft.mccoroutine)
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
}