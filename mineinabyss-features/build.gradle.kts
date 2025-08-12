plugins {
    alias(idofrontLibs.plugins.mia.kotlin.jvm)
    alias(idofrontLibs.plugins.mia.papermc)
    alias(idofrontLibs.plugins.mia.nms)
    kotlin("plugin.serialization")
    alias(idofrontLibs.plugins.compose.compiler)
    alias(idofrontLibs.plugins.mia.publication)
}

dependencies {
    api(project(":mineinabyss-components"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=kotlin.ExperimentalUnsignedTypes",
            "-Xcontext-receivers"
        )
    }
}
