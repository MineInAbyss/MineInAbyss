plugins {
    alias(miaLibs.plugins.kotlin.jvm)
    alias(miaLibs.plugins.mia.papermc)
    alias(miaLibs.plugins.mia.nms)
    alias(miaLibs.plugins.mia.publication)
    kotlin("plugin.serialization")
}


dependencies {
    compileOnly(libs.geary.papermc)
    compileOnly(libs.deeperworld)
    compileOnly(miaLibs.bundles.idofront.core)
    compileOnly(miaLibs.idofront.nms)
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
}